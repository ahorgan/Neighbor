package edu.csuchico.ecst.ahorgan.neighbor.p2pWorld;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by annika on 4/3/16.
 */
public class World {
    private static String TAG = "p2pWorld/World";
    private static String serviceName = "_neighbor";
    private static String serviceType = "_presence._tcp";
    private static long BROADCAST_INTERVAL = 10;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Context mContext;
    private ConnectionManager mConnection;
    private GroupManager mGroup;
    private PeerManager mPeers;
    private ServerSocketManager mServer;
    private ClientSocketManager mClient;
    private Collection<WifiP2pDevice> clientList;
    private WifiP2pDevice thisDevice;
    private Map trustedPeersInfo;
    private List<WifiP2pDevice> trustedPeers;
    private DiscoverLoop discoverRunnable;
    private ScheduledThreadPoolExecutor threadExecutor;
    private boolean discovering;
    private boolean groupFormed;
    private boolean groupOwner;
    private boolean connecting;
    private boolean initialized;
    private static World ourInstance;

    public static World getInstance() {
        if(ourInstance == null)
            ourInstance = new World();
        return ourInstance;
    }

    private World() {
        initialized = false;
    }

    private class ConnectionManager implements WifiP2pManager.ConnectionInfoListener {
        public ConnectionManager() {
            Log.d(TAG, "ConnectionManager()");
        }
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            connecting = false;
            groupFormed = info.groupFormed;
            groupOwner = info.isGroupOwner;
            if(groupFormed) {
                mPeers.addGroupOwnerAddrToServiceDiscovery(String.valueOf(info.groupOwnerAddress));
                if(groupOwner) {
                    Log.d(TAG, "I'm Group Owner!");
                    mManager.requestGroupInfo(mChannel, mGroup);
                }
                else {
                    Log.d(TAG, "I'm a Peer in the Group!");
                    mServer.close();
                    mClient = new ClientSocketManager(info.groupOwnerAddress);
                    mClient.run();
                }
            }
            else
                mPeers.discover();
        }
    }

    private class GroupManager implements WifiP2pManager.GroupInfoListener {
        public GroupManager() {
            Log.d(TAG, "GroupManager()");
        }
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            clientList = group.getClientList();
        }
    }

    private class PeerManager implements WifiP2pManager.PeerListListener {
        private Map record;
        private int missCount;
        private WifiP2pDnsSdServiceInfo serviceInfo;
        private WifiP2pManager.DnsSdServiceResponseListener serviceListener;
        private WifiP2pManager.DnsSdTxtRecordListener txtListener;
        private WifiP2pDnsSdServiceRequest serviceRequest;

        public PeerManager() {
            Log.d(TAG, "PeerManager()");
            trustedPeersInfo = new HashMap();
            trustedPeers = new ArrayList<>();
            record = new HashMap();
            record.put("listenport", String.valueOf(mServer.getPort()));
            record.put("groupowner", "none");
            record.put("status", "available");
            serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceName, serviceType, record);
            missCount = 0;
            discovering = false;
        }
        public void discover() {
            Log.d(TAG, "discover()");
            threadExecutor.scheduleAtFixedRate(new DiscoverLoop(), 0, BROADCAST_INTERVAL, TimeUnit.SECONDS);
            setUpServiceDiscovery();
        }
        public void undiscover() {
            record.put("status", "unavailable");
            threadExecutor.shutdownNow();
            serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_neighbor", "_presence._tcp", record);
            setUpServiceDiscovery();
        }
        private void setUpServiceDiscovery() {
            Log.d(TAG, "setUpServiceDiscovery()");
            if(discovering) {
                mManager.removeLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                       addLocalService();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "setUpServiceDiscovery: Failed Removing Local Service");
                    }
                });
            }
            else {
                addLocalService();
            }
        }
        private void addLocalService() {
            Log.d(TAG, "addLocalService()");
            mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    setUpServiceDiscoveryListeners();
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "setUpServiceDiscovery: Failed Adding Local Service");
                    setUpServiceDiscovery();
                }
            });
        }
        private void setUpServiceDiscoveryListeners() {
            Log.d(TAG, "setUpServiceDiscoveryListeners()");
            if(Build.VERSION.SDK_INT >= 16) {
                txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomain, Map record, WifiP2pDevice device) {
                        Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());
                        Log.d(TAG, "Full Domain: " + fullDomain);
                        Log.d(TAG, fullDomain.split("\\.")[0]);
                        if(fullDomain.split("\\.")[0].matches("_neighbor")) {
                            if(record.get("status").equals("available")) {
                                Log.d(TAG, "Adding port " + record.get("listenport") + " to Neighbors");
                                trustedPeersInfo.put(device.deviceAddress, Integer.getInteger(record.get("listenport").toString()));

                                if (record.get("groupowner").equals("none")) {
                                    connect((String) record.get("groupowner"));
                                }
                            }
                            else {
                                trustedPeersInfo.remove(device.deviceAddress);
                            }
                        }
                    }
                };
                serviceListener = new WifiP2pManager.DnsSdServiceResponseListener() {
                    @Override
                    public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                        WifiP2pDevice resourceType) {
                        Log.d(TAG, "onBonjourServiceAvailable " + instanceName);
                        if(!groupFormed && !connecting) {
                            connect(resourceType.deviceAddress);
                        }

                    }
                };
                requestService();
            }
        }
        public void requestService() {
            Log.d(TAG, "requestService()");
            if(serviceRequest != null) {
                mManager.removeServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        addServiceRequest();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "requestService: remove service request failed");
                    }
                });
            }
            else
                addServiceRequest();
        }
        private void addServiceRequest() {
            Log.d(TAG, "addServiceRequest()");
            serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(serviceName);
            mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            mServer.startListening();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d(TAG, "Service Discovery Failed");
                        }
                    });
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "addServiceRequest: Failed");
                }
            });
        }
        public void addGroupOwnerAddrToServiceDiscovery(String deviceAddress) {
            record.put("groupowner", deviceAddress);
            serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_neighbor", "_presence._tcp", record);
            setUpServiceDiscovery();
        }
        public void connect(String deviceAddress) {
            final WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = deviceAddress;
            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    connecting = true;
                    mManager.requestConnectionInfo(mChannel, mConnection);
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "serviceListener: Connect Failed");
                }
            });
        }
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            if(trustedPeers.size() == 0) {
                missCount++;
            }
            if(missCount == 10) {
                missCount = 0;
                discover();
            }
        }
    }
    private class DiscoverLoop implements Runnable {
        private boolean loop;
        @Override
        public void run() {
            Log.d(TAG, "DiscoverLoop.doInBackground");
            loop = true;
            while(loop) {
                try {
                    Thread.sleep(BROADCAST_INTERVAL);
                    mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Discover Peers Success");
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d(TAG, "Discover Peers Failed");
                            loop = false;
                        }
                    });
                }
                catch(InterruptedException e) {
                    Log.d(TAG, "Discover loop interrupted");
                }
            }
        }
    }
    private class ServerSocketManager {
        private ServerSocket serverSocket;
        boolean listening;
        public ServerSocketManager() {
            listening = false;
            try {
                serverSocket = new ServerSocket(0);
                Log.d(TAG, "Initialized serverSocket on port " + String.valueOf(serverSocket.getLocalPort()));
            }
            catch(IOException e) {
                Log.d(TAG, "ServerSocketManager: " + e.getMessage());
            }
        }
        private class listenLoop implements Runnable {
            @Override
            public void run() {
                try {
                    while (listening) {
                        Socket client = serverSocket.accept();
                        new Thread(new CommunicationHandler(client)).run();
                    }
                }
                catch(IOException e) {
                    Log.d(TAG, "ServerSocketManager.listenLoop: " + e.getMessage());
                }
            }
        }
        private class CommunicationHandler implements Runnable {
            private Socket socket;
            public CommunicationHandler(Socket socket) {
                this.socket = socket;
            }
            @Override
            public void run() {
                try {
                    OutputStream outStream = socket.getOutputStream();
                    outStream.write(("Hello from " + thisDevice.deviceName).getBytes());
                    /*outStream.write(clientList.size());
                    for(WifiP2pDevice peer : clientList) {
                        outStream.write(peer.deviceAddress.getBytes());
                        outStream.write(peer.deviceName.getBytes());
                    }*/
                    outStream.close();
                    socket.close();
                }
                catch(IOException e) {
                    Log.d(TAG, "ServerSocketManager.CommunicationHandler: " + e.getMessage());
                }
            }
        }
        public int getPort() {
            return serverSocket.getLocalPort();
        }
        public void startListening() {
            Log.d(TAG, "ServerSocketManager.startListening()");
            if(!listening)
                new Thread(new listenLoop()).run();
        }
        public void close() {
            listening = false;
            try {
                serverSocket.close();
            }
            catch(IOException e) {
                Log.d(TAG, "ServerSocketManager.close(): " + e.getMessage());
            }
        }
    }

    private class ClientSocketManager {
        InetAddress serverAddress;
        int serverPort;
        ClientSocketManager(InetAddress ownerAddress) {
            serverAddress = ownerAddress;
            serverPort = 2468;
            for(WifiP2pDevice peer : trustedPeers) {
                if(peer.isGroupOwner()) {
                    serverPort = (int)trustedPeersInfo.get(peer.deviceAddress);
                }
            }
        }
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Socket socket = new Socket();
                    try {
                        socket.bind(null);
                        socket.connect(new InetSocketAddress(serverAddress, serverPort), 500);
                        InputStream inStream = socket.getInputStream();
                        String msg = new String();
                        inStream.read(msg.getBytes());
                        Log.d(TAG, "ClientSocketManager: " + msg);
                        inStream.close();
                        socket.close();
                    }
                    catch(IOException e) {
                        Log.d(TAG, "ClientSocketManager: " + e.getMessage());
                    }
                }
            }).run();
        }
    }

    public void setup(Context context) {
        Log.d(TAG, "Setting Up");
        mContext = context;
        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Channel Disconnected");
            }
        });
        mServer = new ServerSocketManager();
        mConnection = new ConnectionManager();
        mPeers = new PeerManager();
        mGroup = new GroupManager();
        groupFormed = false;
        groupOwner = false;
        connecting = false;
        initialized = true;
        threadExecutor = new ScheduledThreadPoolExecutor(1);
    }

    public void teardown() {
        mPeers.undiscover();
    }

    public void setThisDevice(WifiP2pDevice device) {
        thisDevice = device;
    }

    public void requestPeers() {
        mManager.requestPeers(mChannel, mPeers);
    }

    public void requestConnectionInfo() {
        mManager.requestConnectionInfo(mChannel, mConnection);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void discover() {
        mPeers.discover();
    }
}

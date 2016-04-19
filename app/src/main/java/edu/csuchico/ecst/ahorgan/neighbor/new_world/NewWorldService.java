package edu.csuchico.ecst.ahorgan.neighbor.new_world;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NewWorldService extends Service {
    private static String TAG = "NewWorldService";
    private static final String serviceName = "_Neighbor";
    private static final String serviceType = "_presence._tcp";
    public static final String LISTEN_PORT = "listenport";
    public static final int SIG = 0;
    public static final int SIG_PEERS_CHANGED = 1;
    public static final int SIG_RESTART_DISCOVRY = 2;
    public static final int SIG_STOP_DISCOVERY = 3;
    private final NWBinder nwBinder = new NWBinder();
    private PeerManagerService pmService;
    private ConnectionManager cmService;
    private WifiP2pDnsSdServiceInfo serviceInfo;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Context mContext;
    private boolean registered = false;
    private boolean discovering = false;
    private ServerSocket mServerSocket;
    private Map<String, Integer> neighborPorts = new HashMap<>();
    private Map<String, WifiP2pDevice> trustedNeighbors = new HashMap<>();
    private int mPort = 0;
    private final NewWorldServListener nwServListener = new NewWorldServListener();
    private final NewWorldTxtListener nwTxtListener = new NewWorldTxtListener();
    private ServiceConnection pmConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected with Peer Manager");
            pmService = ((PeerManagerService.PMBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnected with " + name);
            pmService = null;
        }
    };
    private ServiceConnection cmConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected with Connection Manager");
            cmService = ((ConnectionManager.CMBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            cmService = null;
        }
    };

    class NewWorldTxtListener implements WifiP2pManager.DnsSdTxtRecordListener {
        @Override
        public void onDnsSdTxtRecordAvailable(String fullDomainName,
                                              Map<String, String> txtRecordMap,
                                              WifiP2pDevice srcDevice) {
            Log.d(TAG, "DNS Record Available for " +
                    srcDevice.deviceName + " " + srcDevice.deviceAddress);
            if(fullDomainName.split("\\.")[0].matches("_neighbor")) {
                for(String key : txtRecordMap.keySet()) {
                    Log.d(TAG, key + " : " + txtRecordMap.get(key));
                }
                if(txtRecordMap.containsKey(LISTEN_PORT)) {
                    String port = txtRecordMap.get(LISTEN_PORT).toString();
                    neighborPorts.put(srcDevice.deviceAddress, Integer.valueOf(port));
                    trustedNeighbors.put(srcDevice.deviceAddress, srcDevice);
                    Log.d(TAG, "DNS Record Available for " +
                            srcDevice.deviceName + " " +
                            srcDevice.deviceAddress + "listening on port" +
                            port);
                }
            }
        }
    }

    class NewWorldServListener implements WifiP2pManager.DnsSdServiceResponseListener {
        @Override
        public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
            Log.d(TAG, "DNS Service Available for " +
                    srcDevice.deviceName + " " + srcDevice.deviceAddress);
        }
    }

    public NewWorldService() {
    }

    class NWBinder extends Binder {
        NewWorldService getService() {
            return NewWorldService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Create");
        super.onCreate();

        Intent startPM = new Intent(NewWorldService.this, PeerManagerService.class);
        startService(startPM);
        Intent startCM = new Intent(NewWorldService.this, ConnectionManager.class);
        startService(startCM);
        mContext = getApplicationContext();
        mManager = (WifiP2pManager) mContext.getSystemService(mContext.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Channel Disconnected");
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start");
        if(pmService == null)
            bindService(new Intent(this, PeerManagerService.class), pmConnection, BIND_IMPORTANT);
        if(cmService == null)
            bindService(new Intent(this, ConnectionManager.class), cmConnection, BIND_IMPORTANT);
        if(!registered) {
            registerService();
        }
        switch(intent.getIntExtra("SIG", SIG)) {
            case SIG:
                Log.d(TAG, "SIG");
                break;
            case SIG_PEERS_CHANGED:
                Log.d(TAG, "SIG_PEERS_CHANGED");
                discoverServices();
                break;
            case SIG_STOP_DISCOVERY:
                if(trustedNeighbors.size() > 0) {
                    mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Cleared Service Requests");
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d(TAG, "Failed to clear service requests");
                        }
                    });
                }
                break;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroy");
        trustedNeighbors.clear();
        neighborPorts.clear();
        if(pmService != null)
            unbindService(pmConnection);
        if(cmService != null)
            unbindService(cmConnection);
        tearDown();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Binded");
        if(pmService == null)
            bindService(new Intent(this, PeerManagerService.class), pmConnection, BIND_IMPORTANT);
        return nwBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Unbind");
        return true;
    }

    private void initializeServer() {
        try {
            mServerSocket = new ServerSocket(mPort);
            mPort = mServerSocket.getLocalPort();
            if(cmService != null)
                cmService.setServerSocket(mServerSocket);
            Log.d(TAG, "Neighbor listening on port " + mPort);
        }
        catch(IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void registerService() {
        if(mServerSocket == null) {
            initializeServer();
        }
        Map record = new HashMap();
        record.put(LISTEN_PORT, String.valueOf(mPort));
        record.put("groupowner", "none");
        record.put("status", "available");
        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceName, serviceType, record);
        mManager.removeLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Added Local Service");
                        mManager.setDnsSdResponseListeners(mChannel, nwServListener, nwTxtListener);
                        registered = true;
                        if(!discovering) {
                            discoverServices();
                        }
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "Failed Adding Local Service");
                        //stopSelf();
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Failed to Remove Local Service");
            }
        });
    }

    private void discoverServices() {
        mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
                mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Added Service Request");
                        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Service Discovery Enabled");
                                discovering = true;
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "Service Discovery Failed");
                                //stopSelf();
                            }
                        });
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "Failed Adding Service Request");
                        //stopSelf();
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Failed Clear Service Requests");
            }
        });
    }

    public void tearDown() {
        try {
            mServerSocket.close();
        }
        catch(IOException e) {
            Log.d(TAG, e.getMessage());
        }
        mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "New World Tear Down Success");
                        discovering = false;
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "New World Tear Down Fail");
                    }
                });
            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    public Map<String, WifiP2pDevice> getTrustedNeighbors() {
        return trustedNeighbors;
    }

    public Map<String, Integer> getNeighborPorts() {
        return neighborPorts;
    }
}



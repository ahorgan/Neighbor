package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RunnableFuture;

/**
 * Created by annika on 3/20/16.
 */
public class World {
    private static String TAG = "World";
    private static String SERVER_PORT = "2468";
    private static long BROADCAST_INTERVAL = 10000;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WorldConnectionInfoListener worldConnectionListener;
    private WorldPeerListener worldPeerListener;
    private WorldGroupInfoListener worldGroupListener;
    private WifiP2pDnsSdServiceInfo serviceInfo;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private ServerSocket mServerSocket;
    private int mLocalPort;
    private HashMap<String, Integer> neighbors;
    private static World ourInstance = null;
    private boolean initialized = false;
    private boolean block_discovering = false;
    private boolean discovering = false;
    private WifiP2pManager.DnsSdTxtRecordListener txtListener;
    private WifiP2pManager.DnsSdServiceResponseListener servListener;
    private Context mContext;
    private Runnable broadcastRunnable = new Runnable() {
        @Override
        public void run() {
            while(discovering) {
                Log.d(TAG, "Calling turnDiscoverOn()");
                turnDiscoverOn();
                try {
                    Thread.sleep(BROADCAST_INTERVAL);
                }
                catch(InterruptedException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
    };
    private class BroadcastTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Thread.sleep(BROADCAST_INTERVAL);
            }
            catch(InterruptedException e) {
                Log.d(TAG, e.getMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Object result) {
            turnDiscoverOn();
        }
    }


    public static World getInstance() {
        if(ourInstance == null)
            ourInstance = new World();
        return ourInstance;
    }

    private World() {
    }

    public void cleanup() {
        Log.d(TAG, "Clean Up World");
        if(worldConnectionListener.isGroupFormed() && worldConnectionListener.isGroupOwner()) {
            mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Removed Group");
                }

                @Override
                public void onFailure(int reason) {
                    switch (reason) {
                        case WifiP2pManager.BUSY:
                            Log.d(TAG, "Remove Group Failed, Busy");
                            break;
                        case WifiP2pManager.ERROR:
                            Log.d(TAG, "Remove Group Failed, Error");
                            break;
                        case WifiP2pManager.P2P_UNSUPPORTED:
                            Log.d(TAG, "Remove Group Failed, P2P Unsupported");
                            break;
                    }
                }
            });
        }
        unDiscover();
        worldConnectionListener.turnOffListening();
        try {
            if (mServerSocket != null && !mServerSocket.isClosed())
                mServerSocket.close();
        }
        catch(IOException e) {
            Log.d(TAG, e.getMessage());
        }
        neighbors.clear();
        initialized = false;
        mChannel = null;
        mManager = null;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initialize(Context context) {
        Log.d(TAG, "Initializing");
        mContext = context;
        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Channel Disconnected");
            }
        });
        worldConnectionListener = new WorldConnectionInfoListener(this, mManager, mChannel);
        worldGroupListener = new WorldGroupInfoListener(mManager, mChannel, worldConnectionListener);
        worldConnectionListener.setGroupInfoListener(worldGroupListener);
        worldPeerListener = new WorldPeerListener(this, mManager, mChannel, worldGroupListener, worldConnectionListener);
        worldConnectionListener.setPeerListListener(worldPeerListener);
        initializeServerSocket();
        initialized = true;
        neighbors = new HashMap<>();
    }

    public void initializeServerSocket() {
        if(mServerSocket == null) {
            try {
                mServerSocket = new ServerSocket(0);
                mLocalPort = mServerSocket.getLocalPort();
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    public void turnServiceDiscoveryOn() {
        if(mManager != null && mChannel != null) {
            mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    startRegistration();
                }

                @Override
                public void onFailure(int reason) {
                    switch (reason) {
                        case WifiP2pManager.BUSY:
                            Log.d(TAG, "Clear Local Services Failed, Busy");
                            break;
                        case WifiP2pManager.P2P_UNSUPPORTED:
                            Log.d(TAG, "Clear Local Services Failed, P2P Unsupported");
                            break;
                        case WifiP2pManager.ERROR:
                            Log.d(TAG, "Clear Local Services Failed, Error");
                            break;
                    }
                }
            });
        }
    }

    public void startRegistration() {
        if(!initialized) {
            initialize(mContext);
        }
        //  Create a string map containing information about your service.
        Map record = new HashMap();
        record.put("listenport", String.valueOf(mLocalPort));
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_neighbor", "_presence._tcp", record);

        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Added Local Service");
                discover();
            }

            @Override
            public void onFailure(int reason) {
                switch (reason) {
                    case WifiP2pManager.BUSY:
                        Log.d(TAG, "Added Local Service Failed: Manager Busy");
                        break;
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.d(TAG, "Added Local Service Failed: P2P Unsupported");
                        break;
                    case WifiP2pManager.ERROR:
                        Log.d(TAG, "Added Local Service Failed: Error");
                        break;
                }
            }
        });
    }

    public void discover() {
        Log.d(TAG, "Discover");
        if(Build.VERSION.SDK_INT < 16) {
            turnDiscoverOn();
        }
        else {
            txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
                @Override
                /* Callback includes:
                 * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
                 * record: TXT record dta as a map of key/value pairs.
                 * device: The device running the advertised service.
                 */
                public void onDnsSdTxtRecordAvailable(
                        String fullDomain, Map record, WifiP2pDevice device) {
                    Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());
                    Log.d(TAG, "Full Domain: " + fullDomain);
                    Log.d(TAG, fullDomain.split("\\.")[0]);
                    if(fullDomain.split("\\.")[0].matches("_neighbor")) {
                        Log.d(TAG, "Adding port " + record.get("listenport") + " to Neighbors");
                        neighbors.put(device.deviceAddress, Integer.getInteger(record.get("listenport").toString()));
                    }
                }
            };

            servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
                @Override
                public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                    WifiP2pDevice resourceType) {

                    // Update the device name with the human-friendly version from
                    // the DnsTxtRecord, assuming one arrived.

                    // Add to the custom adapter defined specifically for showing
                    // wifi devices.
                    Log.d(TAG, "onBonjourServiceAvailable " + instanceName);
                    //mManager.requestConnectionInfo(mChannel, worldConnectionListener);
                    if(worldConnectionListener.isGroupFormed()) {
                        if (!worldConnectionListener.isGroupOwner())
                            worldConnectionListener.startClientSocket();
                    }
                    else {
                        block_discovering = true;
                        worldPeerListener.connectPeer(resourceType);
                    }
                }
            };

            mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

            serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

            mManager.removeServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    mManager.addServiceRequest(mChannel,
                            serviceRequest,
                            new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    // Success!
                                    Log.d(TAG, "Added Service Request");

                                    mManager.discoverServices(mChannel,
                                            new WifiP2pManager.ActionListener() {

                                                @Override
                                                public void onSuccess() {
                                                    // Success!
                                                    Log.d(TAG, "Discover Services Success");
                                                    if (!discovering) {
                                                        turnDiscoverOn();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(int code) {
                                                    // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                                                    Log.d(TAG, "Discover Services Failed");
                                                }

                                            });
                                }

                                @Override
                                public void onFailure(int code) {
                                    // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                                    Log.d(TAG, "Add Service Request Failed");
                                }
                            });
                }

                @Override
                public void onFailure(int reason) {

                }
            });
        }
    }

    public void unDiscover() {
        Log.d(TAG, "Stop Discover");
        discovering = false;
        if(Build.VERSION.SDK_INT < 16) {
            turnDiscoverOff();
        }
        else if(serviceInfo != null && mManager != null){
            mManager.removeLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Remove Local Service Succeeded");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "Remove Local Service Failed");
                }
            });

            mManager.removeServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Remove Service Request Succeeded");
                    discovering = false;
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "Remove Service Request Failed");
                }
            });
        }
    }


    public String getThisDeviceName() {
        return worldConnectionListener.getThisDevice().deviceName;
    }

    public String getThisDeviceAddress() {
        return worldConnectionListener.getThisDevice().deviceAddress;
    }

    public void turnDiscoverOn() {
        if(!initialized && mContext != null)
            initialize(mContext);
        initializeServerSocket();
        WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                discovering = true;
                if(!block_discovering) {
                    new BroadcastTask().execute();
                    Log.d(TAG, "Discover Peers Turned On");
                }
            }

            @Override
            public void onFailure(int reasonCode) {
                switch (reasonCode) {
                    case WifiP2pManager.BUSY:
                        Log.d(TAG, "Discover Peers: Manager Busy");
                        break;
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.d(TAG, "Discover Peers: P2P Unsupported");
                        break;
                    case WifiP2pManager.ERROR:
                        Log.d(TAG, "Discover Peers: Error");
                        break;

                }
                discovering = false;
            }
        };
        mManager.discoverPeers(mChannel, actionListener);
    }

    public void turnDiscoverOff() {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                discovering = false;
                Log.d(TAG, "Stopped Discover");
            }

            @Override
            public void onFailure(int reason) {
                switch (reason) {
                    case WifiP2pManager.BUSY:
                        Log.d(TAG, "Stopped Discover Failed: Busy");
                        break;
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.d(TAG, "Stopped Discover Failed: P2P Unsupported");
                        break;
                    case WifiP2pManager.ERROR:
                        Log.d(TAG, "Stopped Discover Failed: Error");
                        break;
                }
            }
        });
    }

    public void requestPeers() {
        mManager.requestPeers(mChannel, worldPeerListener);
    }

    public void requestConnectionInfo() {
        if(!worldConnectionListener.isGroupFormed())
            mManager.requestConnectionInfo(mChannel, worldConnectionListener);
    }

    public void setThisDevice(WifiP2pDevice device) {
        worldConnectionListener.setThisDevice(device);
    }

    public void cancelConnection() {
        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Cancelled Connection");
            }

            @Override
            public void onFailure(int reason) {
                switch (reason) {
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.d(TAG, "Cancel Connection Failed P2P Unsupported");
                        break;
                    case WifiP2pManager.BUSY:
                        Log.d(TAG, "Cancel Connection Failed Manager Busy");
                        break;
                    case WifiP2pManager.ERROR:
                        Log.d(TAG, "Cancel Connection Failed Error");
                        break;
                }
            }
        });
    }

    public ServerSocket getServerSocket() {
        if(mServerSocket == null)
            initializeServerSocket();
        return mServerSocket;
    }

    public HashMap<String, Integer> getNeighbors() {
        return neighbors;
    }

    public boolean isDiscovering() {
        return discovering;
    }

    public boolean isGroupFormed() {
        return worldConnectionListener.isGroupFormed();
    }

    public void unblock_discovering() {
        block_discovering = false;
    }
}

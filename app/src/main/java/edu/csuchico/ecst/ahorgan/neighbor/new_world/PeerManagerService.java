package edu.csuchico.ecst.ahorgan.neighbor.new_world;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PeerManagerService extends Service {
    private static String TAG = "PeerManagerService";
    public static final int SIG = 0;
    public static final int SIG_REQUEST_PEERS = 1;
    public static final int SIG_CONNECT_PEERS = 2;
    private NewWorldService nwService;
    private final PMBinder pmBinder = new PMBinder();
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    Context mContext;
    ScheduledThreadPoolExecutor discoverExecutor;
    private boolean connecting = false;
    private boolean groupFormed;
    private boolean groupOwner;
    private boolean executorDiscovering = false;
    private ConnectionManager cmService;
    private int count = 0;
    private Map<String, WifiP2pDevice> trustedNeighbors = new HashMap<>();
    private final PMPeerListener pmPeerListener = new PMPeerListener();
    private ServiceConnection nwConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected with " + name);
            nwService = ((NewWorldService.NWBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnected with " + name);
            nwService = null;
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
            Log.d(TAG, "Disconnected with Connection Manager");
            cmService = null;
        }
    };
    private Runnable discoverRunnable = new Runnable() {
        @Override
        public void run() {
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Discover Peers Success");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "Discover Peers Failed");
                    discoverExecutor.shutdownNow();
                    executorDiscovering = false;
                }
            });
        }
    };

    public PeerManagerService() {
    }

    class PMBinder extends Binder {
        PeerManagerService getService() {
            return PeerManagerService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Create");
        super.onCreate();
        startService(new Intent(this, NewWorldService.class));
        mContext = getApplicationContext();
        mManager = (WifiP2pManager) mContext.getSystemService(mContext.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Channel Disconnected");
            }
        });
        discoverExecutor = new ScheduledThreadPoolExecutor(1);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroy");
        discoverExecutor.shutdownNow();
        executorDiscovering = false;
        if(nwService != null) {
            unbindService(nwConnection);
        }
        if(cmService != null) {
            unbindService(cmConnection);
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start");
        if(nwService == null) {
            bindService(new Intent(this, NewWorldService.class), nwConnection, BIND_ABOVE_CLIENT);
        }
        if(cmService == null) {
            bindService(new Intent(this, ConnectionManager.class), cmConnection, BIND_IMPORTANT);
        }
        if(intent != null && intent.hasExtra("SIG")) {
            switch (intent.getIntExtra("SIG", SIG)) {
                case SIG:
                    Log.d(TAG, "SIG");
                    if (!executorDiscovering) {
                        //discoverExecutor.scheduleAtFixedRate(discoverRunnable, 0, 10000, TimeUnit.MILLISECONDS);
                        discoverExecutor.schedule(discoverRunnable, 0, TimeUnit.MILLISECONDS);
                        executorDiscovering = true;
                    }
                    break;
                case SIG_REQUEST_PEERS:
                    Log.d(TAG, "SIG_REQUEST_PEERS");
                    requestPeers();
                    break;
                case SIG_CONNECT_PEERS:
                    Log.d(TAG, "SIG_CONNECT_PEERS");
                    if (nwService != null && nwService.getTrustedNeighbors().size() > 0) {
                        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Discovery Stopped");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "Discovery Failed to Stop");
                            }
                        });
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Bind");
        if(nwService == null)
            bindService(new Intent(this, NewWorldService.class), nwConnection, BIND_ABOVE_CLIENT);
        if(cmService == null)
            bindService(new Intent(this, ConnectionManager.class), cmConnection, BIND_IMPORTANT);
        return pmBinder;
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

    public boolean requestPeers() {
        Log.d(TAG, "Requesting Peers");
        if(mManager != null) {
            mManager.requestPeers(mChannel, pmPeerListener);
            return true;
        }
        return false;
    }

    public void cancelConnecting() {
        connecting = false;
    }

    class PMPeerListener implements WifiP2pManager.PeerListListener {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            Log.d(TAG, "Peers Available");
            if(nwService != null) {
                Log.d(TAG, "Binded to New World Service");
                trustedNeighbors = nwService.getTrustedNeighbors();
                for (WifiP2pDevice peer : peers.getDeviceList()) {
                    Log.d(TAG, peer.deviceAddress + " " + peer.deviceName);
                    if(trustedNeighbors.containsKey(peer.deviceAddress)) {
                        nwService.stopDiscovery();
                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = peer.deviceAddress;
                        if(!connecting) {
                            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "Connect Success");
                                    connecting = true;
                                }

                                @Override
                                public void onFailure(int reason) {
                                    Log.d(TAG, "Connect Failed");
                                    connecting = false;
                                    nwService.unblockDiscovery();
                                }
                            });
                        }
                    }
                }
            }
            else {
                Log.d(TAG, "New World Service is null");
                startService(new Intent(PeerManagerService.this, NewWorldService.class));
                //bindService(new Intent(PeerManagerService.this, NewWorldService.class), nwConnection, BIND_IMPORTANT);
            }
        }
    }
}



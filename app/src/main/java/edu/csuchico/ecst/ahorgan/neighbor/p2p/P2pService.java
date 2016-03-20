package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


public class P2pService extends IntentService {
    private static final String TAG = "P2pService";
    private WorldConnectionInfoListener mWorld;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WorldGroupInfoListener mGroupListener;
    private  WorldPeerListener mPeerListener;
    private boolean initialized = false;


    public P2pService() {
        super("P2pService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        if(!initialized)
            initialize();
        Log.d(TAG, "Processing Action");
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(TAG, "Wifi P2P State Enabled");
                turnDiscoverOn();

            }
            else if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
                Log.d(TAG, "Wifi P2P State Disabled");
                turnDiscoverOff();
            }
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            Log.d(TAG, "Wifi P2P Peers Changed Action");
            //WifiP2pDeviceList peers = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
            mManager.requestPeers(mChannel, mPeerListener);
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P Connection Changed Action");
            // Connection state changed!  We should probably do something about
            // that.
            NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(info.isConnected()) {
                Log.d(TAG, "Connection Type: " + info.getTypeName());
                if(!mWorld.isGroupFormed())
                    mManager.requestConnectionInfo(mChannel, mWorld);
            }
            else {
                NetworkInfo.DetailedState state = info.getDetailedState();
                if(state == NetworkInfo.DetailedState.AUTHENTICATING) {
                    Log.d(TAG, "Authenticating");
                }
                else if(state == NetworkInfo.DetailedState.BLOCKED) {
                    Log.d(TAG, "Blocked");
                }
                else if(state == NetworkInfo.DetailedState.CONNECTING) {
                    Log.d(TAG, "Connecting");
                }
                else if(state == NetworkInfo.DetailedState.DISCONNECTED) {
                    Log.d(TAG, "Disconnected");
                }
                else if(state == NetworkInfo.DetailedState.DISCONNECTING) {
                    Log.d(TAG, "Disconnecting");
                }
                else if(state == NetworkInfo.DetailedState.FAILED) {
                    Log.d(TAG, "Failed");
                }
                else if(state == NetworkInfo.DetailedState.IDLE) {
                    Log.d(TAG, "Idle");
                }
                else if(state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    Log.d(TAG, "Obtaining IP Address");
                }
                else if(state == NetworkInfo.DetailedState.SCANNING) {
                    Log.d(TAG, "Scanning");
                }
                else if(state == NetworkInfo.DetailedState.SUSPENDED) {
                    Log.d(TAG, "Suspended");
                }
                else {
                    // Possibly captive portal, whatever that means
                    Log.d(TAG, "Disconnected: Unknown Reason");
                }
            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P This Device Changed Action");
            WifiP2pDevice thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            mWorld.setThisDevice(thisDevice);

        }
        else {
            Log.d(TAG, "Received Intent Without Matching Action");
            turnDiscoverOn();
        }
    }

    public void initialize() {
        Log.d(TAG, "Initializing");
        mManager = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(getApplicationContext(), Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Channel Disconnected");
            }
        });
        mWorld = new WorldConnectionInfoListener(mManager, mChannel);
        mGroupListener = new WorldGroupInfoListener(mManager, mChannel, mWorld);
        mWorld.setGroupInfoListener(mGroupListener);
        mPeerListener = new WorldPeerListener(this, mManager, mChannel, mGroupListener, mWorld);
        mWorld.setPeerListListener(mPeerListener);
        initialized = true;
    }

    public void turnDiscoverOn() {
        WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

                Log.d(TAG, "Discover Peers Turned On");
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
            }
        };
        mManager.discoverPeers(mChannel, actionListener);
    }

    public void turnDiscoverOff() {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Stopped Discover");
            }

            @Override
            public void onFailure(int reason) {
                switch(reason) {
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

    public void cancelConnection() {
        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Cancelled Connection");
            }

            @Override
            public void onFailure(int reason) {
                switch(reason) {
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

    public WorldPeerListener getPeerListener() {    return mPeerListener;   }
    public WorldConnectionInfoListener getConnectionInfoListener() {    return mWorld;  }
}

package edu.csuchico.ecst.ahorgan.neighbor.p2pWorld;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.util.Log;


public class P2pService extends IntentService {
    private static final String TAG = "P2pService";
    private WorldService mWorld;
    private Context mContext;
    private boolean mBound;
    private Thread worldThread;
    public P2pService() {
        super("P2pService");
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG, "onServiceConnected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            WorldService.LocalBinder binder = (WorldService.LocalBinder) service;
            mWorld = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDeconnected");
            mBound = false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mBound = false;

        Intent startintent = new Intent(this, WorldService.class);
        startService(startintent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    @Override
    public void onHandleIntent(Intent intent) {
        Log.d(TAG, "Processing Action");

        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(TAG, "Wifi P2P State Enabled");
                mWorld.discoverPeers();
            }
            else if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
                Log.d(TAG, "Wifi P2P State Disabled");
                if (mBound) {
                    unbindService(mConnection);
                    mBound = false;
                }
            }
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            Log.d(TAG, "Wifi P2P Peers Changed Action");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mWorld.requestPeers();
                }
            }).run();
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P Connection Changed Action");
            // Connection state changed!  We should probably do something about
            // that.
            NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(info.isConnected()) {
                if(info.getTypeName() == "WIFI_P2P") {
                    Log.d(TAG, "Requesting Connection Info");
                    mWorld.requestConnectionInfo();
                }
                //else
                  //  mWorld.cleanup();
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
            final WifiP2pDevice thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            mWorld.setThisDevice(thisDevice);

        }
        else {
            Log.d(TAG, "Received Intent Without Matching Action");
            mWorld.discoverPeers();
        }
    }
}

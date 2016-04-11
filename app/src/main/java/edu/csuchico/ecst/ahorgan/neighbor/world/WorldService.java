package edu.csuchico.ecst.ahorgan.neighbor.world;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class WorldService extends IntentService {
    private static String TAG = "WorldService";
    DiscoverService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mService = ((DiscoverService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mService = null;
        }
    };

    public WorldService() {
        super("WorldService");
    }


    public static void startDiscover(Context context) {
        Intent intent = new Intent(context, DiscoverService.class);
        intent.putExtra("MESSAGE", DiscoverService.MSG_DISCOVER);
        context.startService(intent);
    }

    public static void peersFound(Context context) {
        Intent intent = new Intent(context, DiscoverService.class);
        intent.putExtra("MESSAGE", DiscoverService.MSG_PEERS_FOUND);
        context.startService(intent);
    }

    public static void startServiceDiscovery(Context context) {
        Intent intent = new Intent(context, DiscoverService.class);
        intent.putExtra("MESSAGE", DiscoverService.MSG_SERVICE_DISCOVER);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //startDiscover(getApplicationContext());
        /*if(mService == null) {
            Intent bind_intent = new Intent(this, DiscoverService.class);
            bindService(bind_intent, mConnection, Context.BIND_AUTO_CREATE);
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unbindService(mConnection);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            //startDiscover(getApplicationContext());
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.d(TAG, "Wifi P2P State Enabled");
                }
                else if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
                    Log.d(TAG, "Wifi P2P State Disabled");
                }
            }
            else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // request available peers from the wifi p2p manager. This is an
                // asynchronous call and the calling activity is notified with a
                // callback on PeerListListener.onPeersAvailable()
                Log.d(TAG, "Wifi P2P Peers Changed Action");
                peersFound(getApplicationContext());
            }
            else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "Wifi P2P Connection Changed Action");
                // Connection state changed!  We should probably do something about
                // that.
                NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(info.isConnected()) {
                    if(info.getTypeName() == "WIFI_P2P") {
                        Log.d(TAG, "Requesting Connection Info");
                        //mWorld.requestConnectionInfo();
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
                //mWorld.setThisDevice(thisDevice);

            } else {
                Log.d(TAG, "Received Intent Without Matching Action");
                //mWorld.discoverPeers();
                //mService.startDiscover();
                //startServiceDiscovery(getApplicationContext());
                startDiscover(getApplicationContext());
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

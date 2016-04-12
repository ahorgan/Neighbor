package edu.csuchico.ecst.ahorgan.neighbor.new_world;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import edu.csuchico.ecst.ahorgan.neighbor.world.WorldService;

/**
 * Created by annika on 4/7/16.
 */
public class WifiP2pBcastReceiver extends BroadcastReceiver{
    private final static String TAG = "WifiP2pBcastReceiver";

    public WifiP2pBcastReceiver() {
        super();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            Log.d(TAG, "Wifi P2P State Changed Action");
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(TAG, "Wifi P2P State Enabled");
                Intent nwIntent = new Intent(context, NewWorldService.class);
                context.startService(nwIntent);
            }
            else if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                Log.d(TAG, "Wifi P2P State Disabled");
                Intent nwIntent = new Intent(context, NewWorldService.class);
                context.stopService(nwIntent);
            }
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P Peers Changed Action");

            Log.d(TAG, "Starting Peer Manager");
            Intent pmIntent = new Intent(context, PeerManagerService.class);
            pmIntent.putExtra("SIG", PeerManagerService.SIG_REQUEST_PEERS);
            context.startService(pmIntent);
            Log.d(TAG, "Starting New World");
            Intent nwIntent = new Intent(context, NewWorldService.class);
            nwIntent.putExtra("SIG", NewWorldService.SIG_PEERS_CHANGED);
            context.startService(nwIntent);
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P Connection Changed Action");
            // Connection state changed!  We should probably do something about
            // that.
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
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P This Device Changed Action");
            //new_intent.putExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE, intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }
    }

}


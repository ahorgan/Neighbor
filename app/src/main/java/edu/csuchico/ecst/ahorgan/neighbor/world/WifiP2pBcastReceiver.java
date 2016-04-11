package edu.csuchico.ecst.ahorgan.neighbor.world;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
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
            Intent new_intent = new Intent(context, WorldService.class);
            new_intent.setAction(action);
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                Log.d(TAG, "Wifi P2P State Changed Action");
                new_intent.putExtra(WifiP2pManager.EXTRA_WIFI_STATE, intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1));
            }
            else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "Wifi P2P Peers Changed Action");
                new_intent.putExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST, intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST));
            }
            else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "Wifi P2P Connection Changed Action");
                // Connection state changed!  We should probably do something about
                // that.
                new_intent.putExtra(WifiP2pManager.EXTRA_NETWORK_INFO, intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO));
            }
            else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "Wifi P2P This Device Changed Action");
                new_intent.putExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE, intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

            }
            //startWakefulService(context, new_intent);
            context.startService(new_intent);
        }

}

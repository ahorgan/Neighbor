package edu.csuchico.ecst.ahorgan.neighbor.Memeosphere;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import edu.csuchico.ecst.ahorgan.neighbor.new_world.ConnectionManager;
import edu.csuchico.ecst.ahorgan.neighbor.new_world.NewWorldService;
import edu.csuchico.ecst.ahorgan.neighbor.new_world.PeerManagerService;


/**
 * Created by annika on 4/7/16.
 */
public class WifiP2pBcastReceiver extends BroadcastReceiver{
    private final static String TAG = "MemeBcastReceiver";

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
                context.startService(new Intent(context, MemeosphereService.class));
            }
            else if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                Log.d(TAG, "Wifi P2P State Disabled");
                context.stopService(new Intent(context, MemeosphereService.class));
            }
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P Peers Changed Action");
            Intent memeIntent = new Intent(context, MemeosphereService.class);
            memeIntent.setAction(action);
            context.startService(memeIntent);
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P Connection Changed Action");

        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P This Device Changed Action");
            Intent memeIntent = new Intent(context, MemeosphereService.class);
            memeIntent.putExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE,
            memeIntent.setAction(action));
            context.startService(memeIntent);

        }
    }

}


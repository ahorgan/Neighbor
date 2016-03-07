package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.util.Log;

/**
 * Created by annika on 3/6/16.
 */
public class WifiP2pEnabledBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiP2pEnabledReceiver";
    private P2pService mService;

    public WifiP2pEnabledBroadcastReceiver() {
        super();
        Log.e(TAG, "Created");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(TAG, "Wifi P2P State Enabled");
                intent = new Intent(context, P2pService.class);
                context.startService(intent);

            }
            else if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
                Log.d(TAG, "Wifi P2P State Disabled");
                if(mService != null) {
                    intent = new Intent(context, P2pService.class);
                    context.stopService(intent);
                    mService = null;
                }
            }
        }
    }
}

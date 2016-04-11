package edu.csuchico.ecst.ahorgan.neighbor.world;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.util.Log;

/**
 * Created by annika on 4/9/16.
 */
public class AlarmReceiverForDiscoverLoop extends BroadcastReceiver {
    //final ServiceDiscovery servDiscovery = ServiceDiscovery.getInstance();

    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        Intent start_discovery = new Intent(mContext, DiscoverService.class);
        //start_discovery.putExtra("MESSAGE", DiscoverService.MSG_SERVICE_DISCOVER);
        //mContext.startService(start_discovery);
        start_discovery.putExtra("MESSAGE", DiscoverService.MSG_DISCOVER);
        mContext.startService(start_discovery);

    }
}

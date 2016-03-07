package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by annika on 3/3/16.
 */
public class WorldGroupInfoListener implements WifiP2pManager.GroupInfoListener {
    private final static String TAG = "WorldGroupInfoListener";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WorldConnectionInfoListener mConnectionListener;

    WorldGroupInfoListener(WifiP2pManager manager, WifiP2pManager.Channel channel, WorldConnectionInfoListener connectionListener) {
        mManager = manager;
        mChannel = channel;
        mConnectionListener = connectionListener;
    }
    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        Log.d(TAG, "Devices in Group:");
        for (WifiP2pDevice device : group.getClientList()) {
            String deviceName = device.deviceName;
            String deviceAddr = device.deviceAddress;
            Log.d(TAG, deviceName + '\t' + deviceAddr);

        }
        // Do Database work with peers
    }
}

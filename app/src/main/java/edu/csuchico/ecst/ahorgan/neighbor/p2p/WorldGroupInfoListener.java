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
    private WifiP2pGroup mGroup;

    WorldGroupInfoListener(WifiP2pManager manager, WifiP2pManager.Channel channel, WorldConnectionInfoListener connectionListener) {
        mManager = manager;
        mChannel = channel;
        mConnectionListener = connectionListener;
    }
    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        mGroup = group;
        mConnectionListener.setGroup(mGroup);
        Log.d(TAG, "Group Size: " + group.getClientList().size());
        Log.d(TAG, "Group Name: " + group.getNetworkName());
        Log.d(TAG, "Group Interface: " + group.getInterface());
        Log.d(TAG, "Group Passphrase: " + group.getPassphrase());
    }

    public WifiP2pGroup getGroup() {
        return mGroup;
    }
}

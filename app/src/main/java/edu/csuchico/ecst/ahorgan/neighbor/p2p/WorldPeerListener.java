package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by annika on 3/1/16.
 */
public class WorldPeerListener implements WifiP2pManager.PeerListListener {
    private static final String TAG = "WorldPeerListener";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WorldGroupInfoListener mGroupListener;

    WorldPeerListener(WifiP2pManager manager, WifiP2pManager.Channel channel, WorldGroupInfoListener groupListener) {
        super();
        mManager = manager;
        mChannel = channel;
        mGroupListener = groupListener;
    }

    public void setChannel(WifiP2pManager.Channel channel) {
        mChannel = channel;
    }

    public void setManager(WifiP2pManager manager) {
        mManager = manager;
    }

    public WifiP2pManager.Channel getChannel() {    return mChannel;    }
    public WifiP2pManager getManager() {    return mManager;    }

    public void onPeersAvailable(WifiP2pDeviceList peers) {
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Create Group: Success!");
                mManager.requestGroupInfo(mChannel, mGroupListener);
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Create Groupe: Error " + reason);
            }
        });
    }

    public void connectPeers(WifiP2pDevice device) {
        //obtain a peer from the WifiP2pDeviceList
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
            }
        });

    }
}

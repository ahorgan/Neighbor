package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.content.Context;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.util.Log;

import com.couchbase.lite.Manager;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by annika on 2/28/16.
 */
public class WorldConnectionInfoListener implements WifiP2pManager.ConnectionInfoListener {
    private final static String TAG = "World";
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private WifiP2pManager.ConnectionInfoListener mConnectionInfoListener;
    private WifiP2pManager.ActionListener mActionListener;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private WifiP2pDeviceList mDeviceList;
    private WifiP2pConfig mConfig;
    private WifiP2pManager.GroupInfoListener mGroupInfoListener;
    private WifiP2pDevice thisDevice;
    private List<WifiP2pDevice> peerList;
    private boolean groupFormed;
    private boolean groupOwner;

    public WorldConnectionInfoListener(WifiP2pManager manager, WifiP2pManager.Channel channel) {
        mManager = manager;
        mChannel = channel;
        peerList = new ArrayList<>();
        groupFormed = false;
        groupOwner = false;
    }

    public void setChannel(WifiP2pManager.Channel channel) {
        mChannel = channel;
    }

    public void setManager(WifiP2pManager manager) {
        mManager = manager;
    }

    public void setGroupInfoListener(WorldGroupInfoListener listener) {
        mGroupInfoListener = listener;
    }

    public void setPeerListListener(WifiP2pManager.PeerListListener mPeerListListener) {
        this.mPeerListListener = mPeerListListener;
    }

    public void setPeerList(List<WifiP2pDevice> peerList) {
        this.peerList = peerList;
    }

    public WifiP2pManager.Channel getChannel() {    return mChannel;    }
    public WifiP2pManager getManager() {    return mManager;    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        groupFormed = info.groupFormed;
        if(groupFormed) {
            Log.d(TAG, "onConnectionInfoAvailable: Group Formed");
            mManager.requestGroupInfo(mChannel, mGroupInfoListener);
            /*
                Start exchanging packets using group owner as router
             */
            groupOwner = info.isGroupOwner;
            if(groupOwner) {
                Log.d(TAG, "I'm Group Owner!");
                // Connected as 'router and peer'
            }
            else {
                Log.d(TAG, "I'm a Peer in the Group");
                Log.d(TAG, "Owner: " + info.groupOwnerAddress);
            }
        }
        else {
            Log.d(TAG, "onConnectionInfoAvailable: Group Not Formed");

        }
    }

    public void setThisDevice(WifiP2pDevice device) {
        thisDevice = device;
        Log.d(TAG, "This Device: Name=" + thisDevice.deviceName +
                " Addr=" + thisDevice.deviceAddress);
    }

    public boolean isGroupFormed() {
        return groupFormed;
    }

    public boolean isGroupOwner() {
        return groupOwner;
    }

}

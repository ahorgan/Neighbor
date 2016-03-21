package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by annika on 3/1/16.
 */
public class WorldPeerListener implements WifiP2pManager.PeerListListener {
    private static final String TAG = "WorldPeerListener";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WorldGroupInfoListener mGroupListener;
    private WorldConnectionInfoListener mConnectionListener;
    private List<WifiP2pDevice> peerList;
    private World mWorld;

    WorldPeerListener(World world,
                      WifiP2pManager manager,
                      WifiP2pManager.Channel channel,
                      WorldGroupInfoListener groupListener,
                      WorldConnectionInfoListener connectionListener) {
        super();
        mWorld = world;
        mManager = manager;
        mChannel = channel;
        mGroupListener = groupListener;
        mConnectionListener = connectionListener;
        peerList = new ArrayList<>();
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
        Log.d(TAG, "Peers Available");
        analyzeChange(peers);
    }

    public void analyzeChange(WifiP2pDeviceList peers) {
        // A previous peer list exists, look for added and deleted peers
        if(peerList.size() > 0 && mConnectionListener.isGroupFormed()) {
            Log.d(TAG, "Group Exists");
            if(mConnectionListener.isGroupOwner()) {
                // Connect new peers
                for (WifiP2pDevice peer : peers.getDeviceList()) {
                    if (!peerList.contains(peer)) {
                        HashMap<String, Integer> neighbors = mWorld.getNeighbors();
                        if(neighbors.size() > 0 && neighbors.containsKey(peer.deviceAddress)) {
                            Log.d(TAG, "Connecting to " + peer.deviceName);
                            connectPeer(peer);
                        }
                        else if(Build.VERSION.SDK_INT < 16){
                            Log.d(TAG, "Connecting to " + peer.deviceName);
                            connectPeer(peer);
                        }
                    }
                }
            }
            peerList.clear();
            peerList.addAll(peers.getDeviceList());
            mConnectionListener.setPeerList(peerList);
        }
        else {
            Log.d(TAG, "Group Does Not Exist");
            peerList.clear();
            peerList.addAll(peers.getDeviceList());
            mConnectionListener.setPeerList(peerList);
            if(peerList.size() > 0) {
                Log.d(TAG, "Found Peers");
                for(WifiP2pDevice device : peerList) {
                    Log.d(TAG, device.deviceName + " " + device.deviceAddress);
                }
                connectPeer(peerList.get(0));
            }
            else {
                Log.d(TAG, "No Peers Found");
            }
        }
    }

    public void connectPeer(WifiP2pDevice device) {
        //obtain a peer from the WifiP2pDeviceList
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
                Log.d(TAG, "Connect Peer Success");
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
                switch(reason) {
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.d(TAG, "Connect Peer P2P Unsupported");
                        break;
                    case WifiP2pManager.BUSY:
                        Log.d(TAG, "Connect Peer Manager Busy");
                        break;
                    case WifiP2pManager.ERROR:
                        Log.d(TAG, "Connect Peer Error");
                        break;
                }
            }
        });

    }
}

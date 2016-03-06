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

/**
 * Created by annika on 2/28/16.
 */
public class WorldConnectionInfoListener implements WifiP2pManager.ConnectionInfoListener {
    private final static String TAG = "World";
    private Context mContext;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private WifiP2pManager.ConnectionInfoListener mConnectionInfoListener;
    private WifiP2pManager.ActionListener mActionListener;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private WifiP2pDeviceList mDeviceList;
    private WifiP2pConfig mConfig;
    private WifiP2pManager.GroupInfoListener mGroupInfoListener;
    private WifiP2pDevice thisDevice;

    public WorldConnectionInfoListener(Context context) {
        mContext = context;
        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Channel Disconnected");
            }
        });
    }

    public void setChannel(WifiP2pManager.Channel channel) {
        mChannel = channel;
    }

    public void setManager(WifiP2pManager manager) {
        mManager = manager;
    }

    public WifiP2pManager.Channel getChannel() {    return mChannel;    }
    public WifiP2pManager getManager() {    return mManager;    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if(info.groupFormed) {
            /*
                Start exchanging packets using group owner as router
             */
            if(info.isGroupOwner) {
                Log.d(TAG, "I'm Group Owner!");
                // Connected as 'router and peer'
            }
            else {
                Log.d(TAG, "I'm a Peer in the Group");
                // Connected as 'peer'
            }
        }
        else {
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Discover Peers Turned On");
                }

                @Override
                public void onFailure(int reasonCode) {
                    switch (reasonCode) {
                        case WifiP2pManager.BUSY:
                            Log.d(TAG, "Discover Peers: Manager Busy");
                            break;
                        case WifiP2pManager.P2P_UNSUPPORTED:
                            Log.d(TAG, "Discover Peers: P2P Unsupported");
                            break;
                        case WifiP2pManager.ERROR:
                            Log.d(TAG, "Discover Peers: Error");
                            break;
                    }
                }
            });
        }
    }

    public void setThisDevice(WifiP2pDevice device) {
        thisDevice = device;
        Log.d(TAG, "This Device: Name=" + thisDevice.deviceName +
                " Addr=" + thisDevice.deviceAddress);
    }
}

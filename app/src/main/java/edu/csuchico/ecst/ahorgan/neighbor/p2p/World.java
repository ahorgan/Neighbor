package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.content.Context;
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

/**
 * Created by annika on 2/28/16.
 */
public class World implements WifiP2pManager.ConnectionInfoListener {
    private final static String TAG = "World";
    private Context mContext;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private WifiP2pManager.ActionListener mActionListener;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private WifiP2pDeviceList mDeviceList;
    private WifiP2pConfig mConfig;
    private WifiP2pManager.GroupInfoListener mGroupInfoListener;
    private WifiP2pDevice thisDevice;

    public World(Context context) {
        mContext = context;
        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.e(TAG, "Channel Disconnected");
            }
        });
    }

    public void requestConnectionInfo() {
        mManager.requestConnectionInfo(mChannel, this);
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
        Log.e("World", "onConnectionInfoAvailable");
    }
}

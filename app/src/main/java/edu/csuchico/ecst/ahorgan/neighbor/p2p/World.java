package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.util.Log;

/**
 * Created by annika on 3/20/16.
 */
public class World {
    private static String TAG = "World";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WorldConnectionInfoListener worldConnectionListener;
    private WorldPeerListener worldPeerListener;
    private WorldGroupInfoListener worldGroupListener;
    private static World ourInstance = new World();
    private boolean initialized = false;

    public static World getInstance() {
        return ourInstance;
    }

    private World() {
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initialize(Context context) {
        Log.d(TAG, "Initializing");
        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Channel Disconnected");
            }
        });
        worldConnectionListener = new WorldConnectionInfoListener(mManager, mChannel);
        worldGroupListener = new WorldGroupInfoListener(mManager, mChannel, worldConnectionListener);
        worldConnectionListener.setGroupInfoListener(worldGroupListener);
        worldPeerListener = new WorldPeerListener(mManager, mChannel, worldGroupListener, worldConnectionListener);
        worldConnectionListener.setPeerListListener(worldPeerListener);
        initialized = true;
    }

    public void turnDiscoverOn() {
        WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener() {
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
        };
        mManager.discoverPeers(mChannel, actionListener);
    }

    public void turnDiscoverOff() {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Stopped Discover");
            }

            @Override
            public void onFailure(int reason) {
                switch (reason) {
                    case WifiP2pManager.BUSY:
                        Log.d(TAG, "Stopped Discover Failed: Busy");
                        break;
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.d(TAG, "Stopped Discover Failed: P2P Unsupported");
                        break;
                    case WifiP2pManager.ERROR:
                        Log.d(TAG, "Stopped Discover Failed: Error");
                        break;
                }
            }
        });
    }

    public void requestPeers() {
        mManager.requestPeers(mChannel, worldPeerListener);
    }

    public void requestConnectionInfo() {
        if(!worldConnectionListener.isGroupFormed())
            mManager.requestConnectionInfo(mChannel, worldConnectionListener);
    }

    public void setThisDevice(WifiP2pDevice device) {
        worldConnectionListener.setThisDevice(device);
    }

    public void cancelConnection() {
        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Cancelled Connection");
            }

            @Override
            public void onFailure(int reason) {
                switch(reason) {
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.d(TAG, "Cancel Connection Failed P2P Unsupported");
                        break;
                    case WifiP2pManager.BUSY:
                        Log.d(TAG, "Cancel Connection Failed Manager Busy");
                        break;
                    case WifiP2pManager.ERROR:
                        Log.d(TAG, "Cancel Connection Failed Error");
                        break;
                }
            }
        });
    }
}

package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class P2pService extends Service {
    private static final String TAG = "P2pService";
    private WorldConnectionInfoListener mWorld;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pBroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private WorldGroupInfoListener mGroupListener;
    private  WorldPeerListener mPeerListener;
    private boolean mAllowRebind; // indicates whether onRebind should be used
    private boolean receiverRegistered;
    private boolean initialized = false;
    private int mStartMode;
    private final IBinder mBinder = new LocalBinder();


    public P2pService() {
    }

    public class LocalBinder extends Binder {
        public P2pService getService() {
            return P2pService.this;
        }
    }


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() called");
        if(!initialized)
            initialize();
    }

    public void initialize() {
        Log.d(TAG, "Initializing");
        mManager = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(getApplicationContext(), Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Channel Disconnected");
            }
        });
        mWorld = new WorldConnectionInfoListener(mManager, mChannel);
        mGroupListener = new WorldGroupInfoListener(mManager, mChannel, mWorld);
        mWorld.setGroupInfoListener(mGroupListener);
        mPeerListener = new WorldPeerListener(this, mManager, mChannel, mGroupListener, mWorld);
        mWorld.setPeerListListener(mPeerListener);
        mReceiver = new WifiP2pBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        //mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        receiverRegistered = false;
        initialized = true;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        if(!initialized)
            initialize();
        if(!receiverRegistered) {
            registerReceiver(mReceiver, mIntentFilter);
            receiverRegistered = true;
        }
        turnDiscoverOn();
        return mStartMode;
    }

    /* unregister the broadcast receiver */
    @Override
    public void onDestroy() {
        if(receiverRegistered) {
            unregisterReceiver(mReceiver);
            receiverRegistered = false;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        if(!initialized)
            initialize();
        Log.d(TAG, "onBind() Called");

        if(!receiverRegistered) {
            registerReceiver(mReceiver, mIntentFilter);
            receiverRegistered = true;
        }
        turnDiscoverOn();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        if(mReceiver != null && receiverRegistered) {
            unregisterReceiver(mReceiver);
            receiverRegistered = false;
        }
        return mAllowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
        //registerReceiver(mReceiver, mIntentFilter);
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

    public WorldPeerListener getPeerListener() {    return mPeerListener;   }
    public WorldConnectionInfoListener getConnectionInfoListener() {    return mWorld;  }
}

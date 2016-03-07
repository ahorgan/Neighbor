package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
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
        mWorld = new WorldConnectionInfoListener(getApplicationContext());
        mManager = mWorld.getManager();
        mChannel = mWorld.getChannel();
        mGroupListener = new WorldGroupInfoListener(mManager, mChannel, mWorld);
        mPeerListener = new WorldPeerListener(mManager, mChannel, mGroupListener, mWorld);
        mReceiver = new WifiP2pBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        //mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        registerReceiver(mReceiver, mIntentFilter);
        mManager.requestConnectionInfo(mChannel, mWorld);
        return mStartMode;
    }

    /* unregister the broadcast receiver */
    @Override
    public void onDestroy() {
        //if(mReceiver != null) {
            //unregisterReceiver(mReceiver);
        //}
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() Called");
        //registerReceiver(mReceiver, mIntentFilter);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        //if(mReceiver != null)
          //unregisterReceiver(mReceiver);
        return mAllowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
        //registerReceiver(mReceiver, mIntentFilter);
    }

    public WorldPeerListener getPeerListener() {    return mPeerListener;   }
    public WorldConnectionInfoListener getConnectionInfoListener() {    return mWorld;  }
}

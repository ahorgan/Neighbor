package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;

import edu.csuchico.ecst.ahorgan.neighbor.R;

/**
 * Created by annika on 3/1/16.
 */
public class WifiP2pBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = "P2pBroadcastReceiver";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private P2pService mService;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private WorldConnectionInfoListener mConnectionInfoListener;

    private ServiceConnection mConnection;

    public WifiP2pBroadcastReceiver(WifiP2pManager manager,
                                    WifiP2pManager.Channel channel,
                                    P2pService service) {
        mManager = manager;
        mChannel = channel;
        mService = service;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            Log.d(TAG, "Wifi P2P Peers Changed Action");
            if (mManager != null && mService != null) {
                Log.d(TAG, "Requesting Peers");
                mManager.requestPeers(mChannel, mService.getPeerListener());
            }


        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P Connection Changed Action");
            // Connection state changed!  We should probably do something about
            // that.
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected() && mService != null) {
                mManager.requestConnectionInfo(mChannel, mService.getConnectionInfoListener());
            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P This Device Changed Action");
            WifiP2pDevice thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            if(mService != null)
                mService.getConnectionInfoListener().setThisDevice(thisDevice);

        }
        else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                Log.d(TAG, "Wifi P2P Discovery Started");
            }
            else if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED){
                Log.d(TAG, "Wifi P2P Discovery Stopped");
            }
        }
    }
}

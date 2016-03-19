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

    public WifiP2pBroadcastReceiver() {
        super();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent new_intent = new Intent(context, P2pService.class);
        new_intent.setAction(action);
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            Log.d(TAG, "Wifi P2P State Changed Action");
            new_intent.putExtra(WifiP2pManager.EXTRA_WIFI_STATE, intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1));
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P Peers Changed Action");
            new_intent.putExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST, intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST));
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P Connection Changed Action");
            // Connection state changed!  We should probably do something about
            // that.
            new_intent.putExtra(WifiP2pManager.EXTRA_NETWORK_INFO, intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO));
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P This Device Changed Action");
            new_intent.putExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE, intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }
        IBinder binder;
        if((binder = peekService(context, new_intent)) == null) {
            Log.d(TAG, "Unable to bind, starting service");
            context.startService(new_intent);
        }
        else {
            Log.d(TAG, "Calling service's process action");
            ((P2pService.LocalBinder) binder).getService().processAction(new_intent);
        }
    }
}

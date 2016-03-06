package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
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

    public WifiP2pBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                    P2pService service) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mService = service;

    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(TAG, "Wifi P2P State Enabled");
                mManager.requestConnectionInfo(mChannel, mService.getConnectionInfoListener());
            }
            else if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
                Log.d(TAG, "Wifi P2P State Disabled");
            }
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                Log.d(TAG, "Wifi P2P Peers Changed Action");
                mManager.requestPeers(mChannel, mService.getPeerListener());
            }


        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P Connection Changed Action");
            // Connection state changed!  We should probably do something about
            // that.
            WifiP2pInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            if(info.groupFormed) {
                Log.d(TAG, "Formed Group");
                mManager.requestConnectionInfo(mChannel, mService.getConnectionInfoListener());
            }
            else {
                Log.d(TAG, "No Formed Group");
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Discover Peers Turned On: Success!");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "Discover Peers Turned On: Failed");
                    }
                });
            }

        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Wifi P2P This Device Changed Action");
            WifiP2pDevice thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            mService.getConnectionInfoListener().setThisDevice(thisDevice);
            /*DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));*/

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

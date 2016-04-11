package edu.csuchico.ecst.ahorgan.neighbor.world;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by annika on 4/11/16.
 */
public class PeerListener implements WifiP2pManager.PeerListListener {
    private static String TAG = "PeerListener";
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        Log.d(TAG, "onPeersAvailable()");
        for(WifiP2pDevice peer : peers.getDeviceList()) {
            Log.d(TAG, "Found " + peer.deviceAddress + " " + peer.deviceName);
        }
    }
}

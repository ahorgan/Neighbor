package edu.csuchico.ecst.ahorgan.neighbor.world;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by annika on 4/10/16.
 */
public class ServiceListeners {
    private static String TAG = "ServiceListeners";
    private Map trustedPeersInfo = new HashMap();
    class ServiceListener implements WifiP2pManager.DnsSdServiceResponseListener {
        @Override
        public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
            Log.d(TAG, "onBonjourServiceAvailable " + instanceName);
        }
    }

    class TxtListener implements WifiP2pManager.DnsSdTxtRecordListener {
        @Override
        public void onDnsSdTxtRecordAvailable(String fullDomain, Map<String, String> record, WifiP2pDevice device) {
            Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());
            Log.d(TAG, "Full Domain: " + fullDomain);
            Log.d(TAG, fullDomain.split("\\.")[0]);

            if(fullDomain.split("\\.")[0].matches("_neighbor")) {
                if(record.get("status").equals("available")) {
                    Log.d(TAG, "Adding port " + record.get("listenport") + " to Neighbors");
                    trustedPeersInfo.put(device.deviceAddress, Integer.getInteger(record.get("listenport").toString()));

                    /*if (record.get("groupowner").equals("none")) {
                        Log.d(TAG, "Fetching group owner from service record: " + record.get("groupowner"));
                        connect((String) record.get("groupowner"));
                    }*/
                }
                else {
                    trustedPeersInfo.remove(device.deviceAddress);
                }
            }
        }
    }
    public final ServiceListener serviceListener = new ServiceListener();
    public final TxtListener txtListener = new TxtListener();

    Map getTrustedPeersInfo() {
        return trustedPeersInfo;
    }
}

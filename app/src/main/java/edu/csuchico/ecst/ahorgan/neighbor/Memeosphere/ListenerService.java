package edu.csuchico.ecst.ahorgan.neighbor.Memeosphere;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.Map;

public class ListenerService extends Service {
    private String TAG = "ListenerService";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MemeosphereService.MemeophereBinder memeosphereBinder;

    class ResponseListener implements WifiP2pManager.DnsSdServiceResponseListener {
        @Override
        public void onDnsSdServiceAvailable(String instanceName,
                                            String registrationType,
                                            WifiP2pDevice srcDevice){
            Log.d(TAG, "Dns Service Available:");
            Log.d(TAG, instanceName + ' ' + registrationType);
            Log.d(TAG, srcDevice.deviceName + ' ' + srcDevice.deviceAddress);
        }
    }

    class RecordListener implements WifiP2pManager.DnsSdTxtRecordListener {
        @Override
        public void onDnsSdTxtRecordAvailable(String fullDomainName,
                                              Map<String, String> txtRecordMap,
                                              WifiP2pDevice srcDevice) {
            Log.d(TAG, "Dns Record Available:");
            Log.d(TAG, fullDomainName);
            Log.d(TAG, srcDevice.deviceName + ' ' + srcDevice.deviceAddress);
            for(String key : txtRecordMap.keySet()) {
                Log.d(TAG, key + " : " + txtRecordMap.get(key));
            }
        }
    }

    class ListenerBinder extends Binder {
        ListenerService getService() {
            return ListenerService.this;
        }
    }
    private ServiceConnection memeosphereConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "Connected with MemeospereService");
            memeosphereBinder = (MemeosphereService.MemeophereBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "Disconnected with MemeosphereService");
            memeosphereBinder = null;
        }
    };
    final private ResponseListener responseListener = new ResponseListener();
    final private RecordListener recordListener = new RecordListener();
    final private ListenerBinder mBinder = new ListenerBinder();

    public ListenerService() {
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying Service...");
        stopRequests();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        if(memeosphereBinder == null) {
            Log.d(TAG, "Creating two way bind with Memeosphere");
            Intent memeosphereIntent = new Intent(this, MemeosphereService.class);
            bindService(memeosphereIntent, memeosphereConnection, BIND_IMPORTANT);
        }
        return mBinder;
    }

    public void setUpManager() {
        if(mManager == null) {
            mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
            mChannel = mManager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener() {
                @Override
                public void onChannelDisconnected() {
                    Log.d(TAG, "P2P Channel Closed");
                }
            });
        }
    }

    public void discover() {
        setUpManager();
        mManager.setDnsSdResponseListeners(mChannel, responseListener, recordListener);
        //for(int test = 1; test <= 2; test++) {
            for(int round = 0; round < 10; round++) {
                final String requestName = "_test:" + String.valueOf(round);
                final WifiP2pDnsSdServiceRequest serviceRequest =
                        WifiP2pDnsSdServiceRequest.newInstance(requestName, "_presence._tcp");
                mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Add " + requestName + " Service Request Success");
                    }

                    @Override
                    public void onFailure(int code) {
                        Log.d(TAG, "Add " + requestName + " Service Request Failed");
                    }
                });
            }
        //}
        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Discover Services Success");
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "Discover Services Failed");
            }
        });
    }

    public void stopRequests() {
        setUpManager();
        mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Cleared Service Requests");
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "Failed to clear service requests");
            }
        });
    }
}

package edu.csuchico.ecst.ahorgan.neighbor.Memeosphere;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MemeosphereService extends Service {
    private String TAG = "MemeosphereService";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private ListenerService.ListenerBinder listenerBinder;
    private final MemeophereBinder mBinder = new MemeophereBinder();
    private ServiceConnection listenerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "Connected with ListenerService");
            listenerBinder = (ListenerService.ListenerBinder)iBinder;
            listenerBinder.getService().discover();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "Disconnected with ListenerService");
            listenerBinder = null;
        }
    };

    class MemeophereBinder extends Binder {
        MemeosphereService getService() {
            return MemeosphereService.this;
        }
    }

    public MemeosphereService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "P2P Channel Disconnected");
            }
        });
        startRegistration();
        if(listenerBinder == null) {
            Log.d(TAG, "Binding to ListenerService...");
            Intent listenerIntent = new Intent(this, ListenerService.class);
            bindService(listenerIntent, listenerConnection, BIND_AUTO_CREATE);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying...");
    }

    private void startRegistration() {
        //  Create a string map containing information about your service.
        //for (int test = 1; test <= 2; test++) {
            for (int round = 0; round < 10; round++) {
                Map record = new HashMap();
                record.put("listenport", String.valueOf(1234));
                record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
                record.put("available", "visible");
                /*if(test == 1) {
                    for (int attr = 1; attr <= round; attr++) {
                        record.put("attr_t1." + String.valueOf(attr),
                                "This is test string " + String.valueOf(attr));
                    }
                }
                else if(test == 2) {*/
                    String val = "";
                    for (int count = 0; count < round*5; count++) {
                        val += "another word ";
                    }
                    val = val.trim();
                    //record.put("date", Calendar.getInstance().toString());
                    record.put("location", "123 Main St. Springfield, IL 12345");
                    record.put("title", "title title title");
                    int valCount = 0;
                    String key = "val" + String.valueOf(valCount);
                    while(val.getBytes().length > 253-key.getBytes().length) {
                        record.put(key, new String(val.getBytes(), 0, 253-key.getBytes().length));
                        if(val.getBytes().length-253-key.getBytes().length < 1)
                        val = new String(val.getBytes(),
                                    253-key.getBytes().length, /*starting index*/
                                    val.getBytes().length-(253-key.getBytes().length)); /*length*/
                        valCount++;
                        key = "val" + String.valueOf(valCount);
                    }
                    if(val.length() > 0) {
                        record.put("val" + String.valueOf(valCount), val);
                        valCount++;
                    }
                    record.put("valcount", String.valueOf(valCount));
                //}

                // Service information.  Pass it an instance name, service type
                // _protocol._transportlayer , and the map containing
                // information other devices will want once they connect to this one.
                try {
                    final String serviceName = "_test:" + String.valueOf(round);
                    WifiP2pServiceInfo serviceInfo =
                            WifiP2pDnsSdServiceInfo.newInstance(serviceName,
                                    "_presence._tcp", record);

                    // Add the local service, sending the service info, network channel,
                    // and listener that will be used to indicate success or failure of
                    // the request.
                    mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Added Local Service " + serviceName + " Success");
                        }

                        @Override
                        public void onFailure(int arg0) {
                            Log.d(TAG, "Added Local Service " + serviceName + " Failed");
                        }
                    });
                }
                catch(IllegalArgumentException e) {
                    Log.d(TAG, e.getMessage());
                    Log.d(TAG, String.valueOf(record.get("val0").toString().getBytes().length
                        + "val0".getBytes().length));
                }
            }
        //}
    }


}

package edu.csuchico.ecst.ahorgan.neighbor.Memeosphere;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Event;

/**
 * Created by annika on 8/12/16.
 */
public class ServiceDiscovery {
    private final String TAG = "MemeServiceDiscovery";
    private final long BROADCAST_INTERVAL = 10000;
    private static ServiceDiscovery thisInstance = null;
    private Context mContext;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Map<String, WifiP2pServiceInfo> registeredServices;
    private Map<String, Map<String, String>> records;
    private Map<String, WifiP2pDnsSdServiceRequest> requestedServices;
    private ArrayList<String> expectingServices;
    private Map<String, Meme> trackedMemes;
    private String serviceType = "_presence._tcp";
    private String macAddress;
    private MemeosphereService memeosphere;
    private boolean isDiscovering = false;
    private final ResponseListener responseListener = new ResponseListener();
    private final RecordListener recordListener = new RecordListener();
    private boolean blockDiscovering = false;
    private class ResponseListener implements WifiP2pManager.DnsSdServiceResponseListener {
        @Override
        public void onDnsSdServiceAvailable(String instanceName,
                                            String registrationType,
                                            WifiP2pDevice srcDevice){
            Log.d(TAG, "Dns Service Available:");
            Log.d(TAG, instanceName + ' ' + registrationType);
            Log.d(TAG, srcDevice.deviceName + ' ' + srcDevice.deviceAddress);
            Matcher matcher = Pattern.compile("meme(-?\\d{10})_([a-z_]+)$").matcher(instanceName);
            if(matcher.matches()) {
                String hash = matcher.group(1);
                String attribute = matcher.group(2);
                boolean containsHash = trackedMemes.containsKey(hash);
                if (!containsHash || !trackedMemes.get(hash).containsKey(attribute)) {
                    if (!containsHash)
                        trackedMemes.put(hash, new Meme());
                    trackedMemes.get(hash).addKey(attribute);
                    if (!expectingServices.contains(instanceName))
                        expectingServices.add(instanceName);
                    if (!requestedServices.containsKey(instanceName))
                        addServiceRequest(instanceName);
                }
            }
            discoverServices();
        }
    }

    private class RecordListener implements WifiP2pManager.DnsSdTxtRecordListener {
        @Override
        public void onDnsSdTxtRecordAvailable(String fullDomainName,
                                              Map<String, String> txtRecordMap,
                                              WifiP2pDevice srcDevice) {
            Log.d(TAG, "Dns Record Available:");
            Log.d(TAG, fullDomainName);
            Matcher matcher = Pattern.compile("(meme(-?\\d{10})_([a-z_]+))\\..*")
                    .matcher(fullDomainName);
            Log.d(TAG, srcDevice.deviceName + ' ' + srcDevice.deviceAddress);
            if (matcher.matches()) {
                String instanceName = matcher.group(1);
                String hash = matcher.group(2);
                String attribute = matcher.group(3);
                if(trackedMemes.get(hash).belongsToThisDevice) {
                    removeServiceRequest(instanceName);
                    expectingServices.remove(instanceName);
                }
                else if (!trackedMemes.get(hash).containsProperty(attribute)) {
                    String value = "";
                    if (txtRecordMap.containsKey(attribute + "c")) {
                        for (int i = 0; i < Integer.getInteger(txtRecordMap.get(attribute + "c")); i++) {
                            value += txtRecordMap.get(attribute + i);
                        }
                    } else
                        value = txtRecordMap.get(attribute);
                    if(attribute.equals(Event.OWNER) && value.equals(macAddress)) {
                        trackedMemes.get(hash).setBelongsToThisDevice(true);
                    }
                    else if (attribute.equals(Event.OWNER) && value.equals("me"))
                        value = srcDevice.deviceAddress;
                    else if (attribute.equals((Event.BCAST)))
                        value = "false";
                    if (trackedMemes.get(hash).addProperty(attribute, value)) {
                        memeosphere.addMemeToDatabase(trackedMemes.get(hash));
                    }
                    removeServiceRequest(instanceName);
                    expectingServices.remove(instanceName);
                }
            }
        }
    }

    public static ServiceDiscovery getInstance(Context context) {
        if(thisInstance == null) {
            thisInstance = new ServiceDiscovery(context);
        }
        return thisInstance;
    }

    private ServiceDiscovery(Context context) {
        Log.d(TAG, "Initializing ServiceDiscovery");
        mContext = context;
        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, Looper.getMainLooper(),
                new WifiP2pManager.ChannelListener() {
                    @Override
                    public void onChannelDisconnected() {
                        Log.d(TAG, "Channel Disconnected");
                        thisInstance = null;
                    }
        });
        registeredServices = new HashMap<>();
        records = new HashMap<>();
        requestedServices = new HashMap<>();
        expectingServices = new ArrayList<>();
        mManager.setDnsSdResponseListeners(mChannel, responseListener, recordListener);
        trackedMemes = new HashMap<>();
    }

    public void linkToService(MemeosphereService service) {
        memeosphere = service;
    }

    public void setMacAddress(String mac) {
        macAddress = mac;
    }

    public void discoverServices() {
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

    public void discoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Discover Peers Success");
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "Discover Peers Failed");
            }
        });
    }

    public void stopDiscoverPeers() {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Successfully stopped peer discovery");
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "Failed to stop peer discovery");
            }
        });
    }

    public void stopDiscovering() {
        blockDiscovering = true;
    }

    public void addServiceRequest(String service) {
        if(service == null || service == "")
            return;
        final String serviceName = service;
        final WifiP2pDnsSdServiceRequest serviceRequest =
                WifiP2pDnsSdServiceRequest.newInstance(service, serviceType);
        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Successfully added service request " + serviceName);
                requestedServices.put(serviceName, serviceRequest);
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "Failed to add service request " + serviceName);
            }
        });
    }

    public void addServiceRequest() {
        if(expectingServices.isEmpty()) {
            final WifiP2pDnsSdServiceRequest serviceRequest =
                    WifiP2pDnsSdServiceRequest.newInstance(serviceType);
            mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Successfully added generic service request");
                }

                @Override
                public void onFailure(int i) {
                    Log.d(TAG, "Failed to add generic service request");
                }
            });
        }
        else {
            clearServiceRequests();
            for(String service : expectingServices)
                addServiceRequest(service);
        }
    }

    public void removeServiceRequest(String service) {
        if(service == null || service == "" || !requestedServices.containsKey(service))
            return;
        final String serviceName = service;
        final WifiP2pDnsSdServiceRequest serviceRequest = requestedServices.get(service);
        mManager.removeServiceRequest(mChannel, serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Removed service request " + serviceName +
                            "successfully");
                        requestedServices.remove(serviceRequest);
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d(TAG, "Failed to remove service request " + serviceName);
                    }
                });
    }

    public void clearServiceRequests() {
        mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Service Requests successfully cleared");
                requestedServices.clear();
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "Failed to clear service requests");
            }
        });
    }

    public void registerService(Map<String, String> record) {
        if(record == null || record.isEmpty())
            return;
        for(Map.Entry<String, String> entry : record.entrySet()) {
            registerService("meme" + record.hashCode(), entry.getKey(),
                    entry.getValue());
        }
    }

    public void registerService(String service, String key, String value) {
        if(service == null || service == "" ||
                key == null || key == "" ||
                value == null || value == "")
            return;
        final Map convertedRecord = new HashMap();
        final String mKey = key;
        final String mValue = value;
        addRecord(convertedRecord, key, value);
        try {
            final String serviceName = service;
            final WifiP2pServiceInfo serviceInfo =
                    WifiP2pDnsSdServiceInfo.newInstance(serviceName + "_" + key,
                            serviceType, convertedRecord);
            // Add the local service, sending the service info, network channel,
            // and listener that will be used to indicate success or failure of
            // the request.
            mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Added Local Service " + serviceName + "_" + mKey +" Success");
                    registeredServices.put(serviceName, serviceInfo);
                    records.put(serviceName + "_" + mKey, convertedRecord);
                }

                @Override
                public void onFailure(int arg0) {
                    Log.d(TAG, "Added Local Service " + serviceName + " Failed");
                }
            });
        }
        catch(IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void removeLocalService(String service) {
        if(service == null || service == "")
            return;
        final String serviceName = service;
        final WifiP2pServiceInfo serviceInfo = registeredServices.get(service);
        final Map<String, String> record = records.get(service);
        if(serviceInfo != null) {
            mManager.removeLocalService(mChannel,
                    serviceInfo,
                    new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Removed service " + serviceName);
                            registeredServices.remove(serviceInfo);
                            records.remove(record);
                        }

                        @Override
                        public void onFailure(int i) {
                            Log.d(TAG, "Failed to remove service " + serviceName);
                        }
                    });
        }
        else
            Log.d(TAG, "Service " + service + "does not exist");
    }
    /*
        Clears all broadcasting registered local services
     */
    public void clearLocalServices() {
        mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Cleared Local Services");
                registeredServices.clear();
                records.clear();
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG, "Failed to clear local services");
            }
        });
    }

    /*
        Adds key value pairs in converted form:
            if key AND value exceed 255 bytes, break up
            into multiple key value pairs labeled key0, key1, etc.
            with a keyc entry holding the count of key
     */
    private void addRecord(Map record, String key, String val) {
        if(val.getBytes().length <= 253-key.getBytes().length) {
            record.put(key, val);
        }
        else {
            int valCount = 0;
            String tmpkey = key + String.valueOf(valCount);
            while (val.getBytes().length > 253 - tmpkey.getBytes().length) {
                record.put(tmpkey, new String(val.getBytes(), 0, 253 - tmpkey.getBytes().length));
                val = new String(val.getBytes(),
                        253 - tmpkey.getBytes().length, /*starting index*/
                        val.getBytes().length - (253 - tmpkey.getBytes().length)); /*length*/
                valCount++;
                tmpkey = key + String.valueOf(valCount);
            }
            if (val.length() > 0) {
                record.put(tmpkey, val);
                valCount++;
            }
            record.put(key + "c", String.valueOf(valCount));
        }
    }


}

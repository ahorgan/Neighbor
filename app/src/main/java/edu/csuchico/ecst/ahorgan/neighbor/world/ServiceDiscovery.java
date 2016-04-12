package edu.csuchico.ecst.ahorgan.neighbor.world;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.*;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import edu.csuchico.ecst.ahorgan.neighbor.p2pWorld.World;

/**
 * Created by annika on 4/9/16.
 */
public class ServiceDiscovery extends Service {
    private static final String TAG = "ServiceDiscovery";
    private static final String serviceName = "_Neighbor";
    private static final String serviceType = "_presence._tcp";
    public static final int MSG_RESTART_SERVICE_DISCOVERY = 1;
    public static final int MSG_STOP_SERVICE_DISCOVERY = 2;
    public static final int MSG_SETUP = 3;
    public static final int MSG_RECORD_FOUND = 4;
    private static ServiceDiscovery ourInstance = new ServiceDiscovery();
    private int mPort;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiP2pDnsSdServiceInfo serviceInfo;
    WifiP2pDnsSdServiceRequest serviceRequest;
    WifiP2pManager.DnsSdTxtRecordListener txtListener;
    WifiP2pManager.DnsSdServiceResponseListener serviceListener;
    private boolean setup = false;
    private boolean noRecords = true;
    final LocalBinder mBinder = new LocalBinder();
    DiscoverService mDiscoverService;
    ServiceDiscoveryHandler mHandler;
    final ServiceListeners listeners = new ServiceListeners();
    final Map Neighbors = listeners.getTrustedPeersInfo();
    Context mContext;
    private boolean nothingFound = true;

    //public static ServiceDiscovery getInstance() {
      //  return ourInstance;
    //}

    public ServiceDiscovery() {

    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mDiscoverService = ((DiscoverService.LocalBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mDiscoverService = null;
        }
    };

    class LocalBinder extends Binder {
        ServiceDiscovery getService() {
            return ServiceDiscovery.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        // Start Discover //
        Intent discover_service = new Intent(this, DiscoverService.class);
        discover_service.putExtra("MESSAGE", DiscoverService.START_ALIVE);
        startService(discover_service);

        // Start Handler //
        HandlerThread thread = new HandlerThread("service_discovery_thread", Process.THREAD_PRIORITY_FOREGROUND);
        thread.start();
        mHandler = new ServiceDiscoveryHandler(thread.getLooper());
    }

    @Override
    public void onDestroy() {
        if(mDiscoverService != null)
           unbindService(mConnection);
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if(setup) {
            Log.d(TAG, "Set Up True");
            Message msg = Message.obtain(mHandler, intent.getIntExtra("MESSAGE", MSG_RESTART_SERVICE_DISCOVERY));
            msg.arg1 = startId;
            mHandler.sendMessage(msg);
        }
        else {
            Log.d(TAG, "Set Up False");
            mPort = intent.getIntExtra("PORT", -1);
            setup(getApplicationContext(), mPort);
            if(mDiscoverService == null)
                bindService(new Intent(this, DiscoverService.class), mConnection, BIND_IMPORTANT);
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        //if(mDiscoverService == null) {
           // Log.d(TAG, "Binding Service");
            //bindService(new Intent(this, intent.getComponent().getClass()), mConnection, Context.BIND_AUTO_CREATE);
        //}
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        bindService(new Intent(this, intent.getComponent().getClass()), mConnection, Context.BIND_AUTO_CREATE);
        return true;
    }

    class ServiceDiscoveryHandler extends Handler {
        ServiceDiscoveryHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_RESTART_SERVICE_DISCOVERY:
                    Log.d(TAG, "MSG_RESTART_SERVICE_DISCOVERY");
                    serviceDiscoveryTask.execute();
                    break;
                case MSG_STOP_SERVICE_DISCOVERY:
                    Log.d(TAG, "MSG_STOP_SERVICE_DISCOVERY");
                    clearServices();
                    break;
                case MSG_SETUP:
                    Log.d(TAG, "MSG_SETUP");
                    setup(mContext);
                    break;
                case MSG_RECORD_FOUND:
                    Log.d(TAG, "MSG_RECORD_FOUND");
                    nothingFound = false;
                    break;
            }
            stopSelfResult(msg.arg1);
        }
    }

    AsyncTask serviceDiscoveryTask = new AsyncTask() {
        @Override
        protected Object doInBackground(Object[] params) {
            if(!setup) {
                restartServices();
            }
            else
                setup(getApplicationContext());

            while(nothingFound) {
                try {
                    Thread.sleep(10000);
                }
                catch(InterruptedException e) {
                    Log.d(TAG, "serviceDiscoveryTask interrupted");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            mHandler.sendMessage(Message.obtain(mHandler, MSG_RECORD_FOUND));
        }
    };

    public Runnable restartServiceDiscovery = new Runnable() {
        @Override
        public void run() {
            restartServices();
        }
    };

    private void setup(Context context){
        if(!setup) {
            mManager = (WifiP2pManager) context.getSystemService(context.WIFI_P2P_SERVICE);
            mChannel = mManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
                @Override
                public void onChannelDisconnected() {
                    Log.d(TAG, "Channel Disconnected");
                }
            });
            setup = true;
            if (mPort != -1)
                restartServices();
        }
    }

    public void setup(Context context, int port){
        mManager = (WifiP2pManager) context.getSystemService(context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Channel Disconnected");
            }
        });
        setup = true;
        mPort = port;
        if(mPort != -1)
            restartServices();
    }

    public boolean isSetup() {
        return setup;
    }

    private void registerService() {
        Map record = new HashMap();
        record.put("listenport", String.valueOf(mPort));
        record.put("groupowner", "none");
        record.put("status", "available");
        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceName, serviceType, record);
        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                setUpServiceDiscoveryListeners();
            }

            @Override
            public void onFailure(int reason) {
                setup = false;
                Log.d(TAG, "setUpServiceDiscovery: Failed Adding Local Service");
            }
        });
    }

    public void restartServices() {
        if(setup) {
            mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "clear local services success");
                    mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "clear service requests success");
                            registerService();
                        }

                        @Override
                        public void onFailure(int reason) {
                            setup = false;
                            Log.d(TAG, "clear service requests failed");
                        }
                    });
                }

                @Override
                public void onFailure(int reason) {
                    setup = false;
                    Log.d(TAG, "clear local services fail");
                }
            });
        }
        else
            setup(getApplicationContext());
    }

    private void clearServices() {
        mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "clear local services success");
                mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "clear service requests success");
                    }

                    @Override
                    public void onFailure(int reason) {
                        setup = false;
                        Log.d(TAG, "clear service requests failed");
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                setup = false;
                Log.d(TAG, "clear local services fail");
            }
        });
    }

    private void setUpServiceDiscoveryListeners() {
        Log.d(TAG, "setUpServiceDiscoveryListeners()");
        mManager.setDnsSdResponseListeners(mChannel, listeners.serviceListener, listeners.txtListener);
        serviceDiscover();
    }

    private void serviceDiscover() {
        Log.d(TAG, "addServiceRequest()");
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(serviceName);
        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Service Discovery Success");
                        Intent discoverService = new Intent(ServiceDiscovery.this, DiscoverService.class);
                        bindService(discoverService, mConnection, BIND_IMPORTANT);
                    }

                    @Override
                    public void onFailure(int reason) {
                        setup = false;
                        Log.d(TAG, "Service Discovery Failed");
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                setup = false;
                Log.d(TAG, "addServiceRequest: Failed");
            }
        });
    }

    public boolean areNoRecords() {
        return noRecords;
    }

    public Map getNeighbors() {
        return Neighbors;
    }
}

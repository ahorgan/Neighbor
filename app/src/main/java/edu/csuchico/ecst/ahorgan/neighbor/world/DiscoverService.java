package edu.csuchico.ecst.ahorgan.neighbor.world;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
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
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.csuchico.ecst.ahorgan.neighbor.p2pWorld.World;

public class DiscoverService extends Service {
    private static String TAG = "DiscoverService";
    private static String serviceName = "_neighbor";
    private static String serviceType = "_presence._tcp";
    public static final int START_ALIVE = 0;
    public static final int MSG_DISCOVER = 1;
    public static final int MSG_STOP_DISCOVER = 2;
    public static final int MSG_SERVICE_DISCOVER = 3;
    public static final int MSG_PEERS_FOUND = 4;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    DiscoverHandler mHandler;
    ServerSocket mServer;
    int mPort;
    WifiP2pDnsSdServiceInfo serviceInfo;
    WifiP2pManager.DnsSdTxtRecordListener txtListener;
    WifiP2pManager.DnsSdServiceResponseListener serviceListener;
    WifiP2pDnsSdServiceRequest serviceRequest;
    final LocalBinder mBinder = new LocalBinder();
    int startId;
    int blanksCount = 0;
    Context mContext;
    boolean discovering;
    boolean servDiscovering;
    ServiceDiscovery mServiceDiscovery;
    final PeerListener mPeerListener = new PeerListener();
    Runnable startAndBindServiceDiscovery = new Runnable() {
        @Override
        public void run() {
            Intent service = new Intent(DiscoverService.this, ServiceDiscovery.class);
            startService(service);
            bindService(service, mDiscoveryConnection, BIND_IMPORTANT);
        }
    };

    public DiscoverService() {
    }

    ServiceConnection mDiscoveryConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mServiceDiscovery = ((ServiceDiscovery.LocalBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mServiceDiscovery = null;
        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();
        discovering = false;
        servDiscovering = false;
        mContext = getApplicationContext();
        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Channel Disconnected");
            }
        });
        HandlerThread thread = new HandlerThread("discover_thread", Process.THREAD_PRIORITY_FOREGROUND);
        thread.start();
        mHandler = new DiscoverHandler(thread.getLooper());
        if (mServer == null) {
            mHandler.initializeServer();
        }
        Intent service_discovery = new Intent(this, ServiceDiscovery.class);
        service_discovery.putExtra("PORT", mPort);
        startService(service_discovery);
        //discoverExecutor.scheduleAtFixedRate(
          //      peerDiscoveryRunnable, 0, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        if(mServiceDiscovery != null)
            unbindService(mDiscoveryConnection);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        Message msg = Message.obtain(mHandler, intent.getIntExtra("MESSAGE", MSG_DISCOVER));
        if(msg.what != START_ALIVE && mServiceDiscovery != null) {
            msg.arg1 = startId;
            msg.obj = intent;
            mHandler.sendMessage(msg);
        }
        else {
            bindService(new Intent(this, ServiceDiscovery.class), mDiscoveryConnection, BIND_ABOVE_CLIENT);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    class LocalBinder extends Binder {
        public DiscoverService getService() {
            return DiscoverService.this;
        }
    }

    public void peersFound() {
        Message msg = Message.obtain(mHandler, MSG_PEERS_FOUND);
        mHandler.sendMessage(msg);
    }

    class DiscoverHandler extends Handler {
        //final ScheduledExecutorService discoverExecutor = new ScheduledThreadPoolExecutor(1);
        AlarmManager alarm;
        PendingIntent pendingIntent;

        DiscoverHandler(Looper looper) {
            super(looper);
            Log.d(TAG, "DiscoverHandler()");
            alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "DiscoverHandler.handleMessage()");
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_DISCOVER:
                    Log.d(TAG, "MSG_DISCOVER");
                    if (!discovering) {
                        Log.d(TAG, "Turn on Discover Alarm");
                        /*discoverExecutor.scheduleWithFixedDelay(
                                peerDiscoveryRunnable, 0, 1000, TimeUnit.MILLISECONDS);*/
                        Intent alarmIntent = new Intent(mContext, AlarmReceiverForDiscoverLoop.class);
                        pendingIntent = PendingIntent.getBroadcast(mContext, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 10000, pendingIntent);
                        discovering = true;

                    }
                    mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Discover Peers Success " + String.valueOf(blanksCount));
                            if(mServiceDiscovery.getNeighbors().size() == 0) {
                                blanksCount++;
                                if(blanksCount >= 2) {
                                    Log.d(TAG, "Restarting Service Discovery");
                                    Intent intent = new Intent(DiscoverService.this, ServiceDiscovery.class);
                                    intent.putExtra("MESSAGE", ServiceDiscovery.MSG_RESTART_SERVICE_DISCOVERY);
                                    startService(intent);
                                    blanksCount = 0;
                                }
                            }
                        }

                        @Override
                        public void onFailure(int reason) {
                            discovering = false;
                            alarm.cancel(pendingIntent);
                            Log.d(TAG, "Discover Peers Failed");
                        }
                    });
                    break;
                case MSG_STOP_DISCOVER:
                    Log.d(TAG, "MSG_STOP_DISCOVER");
                    //discoverExecutor.shutdownNow();
                    if (discovering && alarm != null) {
                        alarm.cancel(pendingIntent);
                        discovering = false;
                    }
                    break;
                case MSG_SERVICE_DISCOVER:
                    Log.d(TAG, "MSG_SERVICE_DISCOVER");
                    if (mServer == null) {
                        initializeServer();
                    }
                    if(mServiceDiscovery == null) {
                        Log.d(TAG, "ServiceDiscovery not bound");
                        Intent serv_intent = new Intent(DiscoverService.this, ServiceDiscovery.class);
                        serv_intent.putExtra("PORT", mPort);
                        startService(serv_intent);
                        servDiscovering = true;
                        //bindService(serv_intent, mDiscoveryConnection, Context.BIND_IMPORTANT);
                    }
                    else {
                        Log.d(TAG, "ServiceDiscovery bound");
                        mServiceDiscovery.restartServices();
                    }
                    //sendMessage(Message.obtain(this, MSG_SERVICE_DISCOVER));
                    break;
                case MSG_PEERS_FOUND:
                    Log.d(TAG, "MSG_PEERS_FOUND");
                    requestPeers();
                    break;
            }
            stopSelfResult(msg.arg1);
        }

        /*Runnable setUpServiceDiscovery = new Runnable() {
            @Override
            public void run() {
                serv.setup(mContext, mPort);
            }
        };*/
        Runnable peerDiscoveryRunnable = new Runnable() {
            @Override
            public void run() {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        discovering = true;
                        Log.d(TAG, "Discover Peers Success");
                        mHandler.postDelayed(peerDiscoveryRunnable, 10000);
                    }

                    @Override
                    public void onFailure(int reason) {
                        discovering = false;
                        Log.d(TAG, "Discover Peers Fail");
                        if (alarm != null)
                            alarm.cancel(pendingIntent);
                    }
                });
            }
        };

        private void initializeServer() {
            if (mServer == null) {
                try {
                    mServer = new ServerSocket(0);
                    mPort = mServer.getLocalPort();
                    Log.d(TAG, "Listening on port " + String.valueOf(mPort));
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }

        private void requestPeers() {
            mManager.requestPeers(mChannel, mPeerListener);
        }
    }
}

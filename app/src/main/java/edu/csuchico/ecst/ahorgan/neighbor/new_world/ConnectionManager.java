package edu.csuchico.ecst.ahorgan.neighbor.new_world;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

import edu.csuchico.ecst.ahorgan.neighbor.p2pWorld.World;

public class ConnectionManager extends Service {
    private final static String TAG = "ConnectionManager";
    public final static int SIG = 0;
    public final static int SIG_REQUEST_CONN_INFO = 1;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    ServerSocket mServerSocket;
    NewWorldService nwService;
    PeerManagerService pmService;
    boolean groupFormed = false;
    boolean groupOwner = false;
    final CMBinder mBinder = new CMBinder();
    final CMConnectionListener cmConnectionListener = new CMConnectionListener();
    ServiceConnection nwConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected with New World");
            nwService = ((NewWorldService.NWBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "New World Disconnected");
            nwService = null;
        }
    };
    ServiceConnection pmConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected with Peer Manager");
            pmService = ((PeerManagerService.PMBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Peer Manager Disconnected");
        }
    };

    public ConnectionManager() {
    }

    class CMBinder extends Binder {
        ConnectionManager getService() {
            return ConnectionManager.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Create");
        Context mContext = getApplicationContext();
        mManager = (WifiP2pManager)mContext.getSystemService(mContext.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Channel Disconnected");
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start");
        if(nwService == null)
            bindService(new Intent(this, NewWorldService.class), nwConnection, BIND_AUTO_CREATE);
        if(pmService == null)
        bindService(new Intent(this, PeerManagerService.class), pmConnection, BIND_AUTO_CREATE);
        switch(intent.getIntExtra("SIG", SIG)) {
            case SIG:
                Log.d(TAG, "SIG");
                break;
            case SIG_REQUEST_CONN_INFO:
                Log.d(TAG, "SIG_REQUEST_CONN_INFO");
                requestConnectionInfo();
                break;
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Bind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "Rebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Unbind");
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroy");
        try {
            mServerSocket.close();
        }
        catch(IOException e) {
            Log.d(TAG, e.getMessage());
        }
        super.onDestroy();
    }

    public void setServerSocket(ServerSocket socket) {
        Log.d(TAG, "Setting Server Socket");
        mServerSocket = socket;
    }

    public void requestConnectionInfo() {
        mManager.requestConnectionInfo(mChannel, cmConnectionListener);
    }

    class CMConnectionListener implements WifiP2pManager.ConnectionInfoListener {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            Log.d(TAG, "Connection Info Available");
            groupFormed = info.groupFormed;
            groupOwner = info.isGroupOwner;
            if(groupFormed) {
                Log.d(TAG, "Group Formed");
                if(groupOwner) {
                    Log.d(TAG, "I'm Group Owner!");
                }
                else {
                    Log.d(TAG, "I'm a Peer in the Group");
                }
            }
            else {
                Log.d(TAG, "Group Did Not Form");
                if(pmService != null) {
                    Intent intent = new Intent(ConnectionManager.this, PeerManagerService.class);
                    intent.putExtra("SIG", PeerManagerService.SIG);
                    startService(intent);
                }
            }
        }
    }
}

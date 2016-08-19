package edu.csuchico.ecst.ahorgan.neighbor.Memeosphere;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MemeosphereService extends Service {
    private static String TAG = "MemeosphereService";
    private final MemeosphereBinder mBinder = new MemeosphereBinder();
    private ServiceDiscovery sd;
    private Runnable discoverRunnable = new Runnable() {
        @Override
        public void run() {
            sd.discoverPeers();
        }
    };
    private Runnable setupRunnable = new Runnable() {
        @Override
        public void run() {
            new SetUpTask().execute();
        }
    };
    private ScheduledExecutorService discoverExecutor =
            Executors.newScheduledThreadPool(2);
    private ScheduledFuture discoverLoop;
    private ScheduledFuture setUpLoop;

    public class MemeosphereBinder extends Binder {
        MemeosphereService getService() {
            return MemeosphereService.this;
        }
    }

    public MemeosphereService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Created");
        sd = ServiceDiscovery.getInstance(getApplicationContext());
        setupTest();
        discoverLoop = discoverExecutor.scheduleAtFixedRate(discoverRunnable,
                10, 10, TimeUnit.SECONDS);
        setUpLoop = discoverExecutor.scheduleAtFixedRate(setupRunnable,
                0, 10, TimeUnit.SECONDS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Started");
        String action = intent.getAction();
        if(action != null && action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
            setUpLoop.cancel(true);
            setUpLoop = discoverExecutor.scheduleAtFixedRate(setupRunnable,
                    0, 10, TimeUnit.SECONDS);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Binded");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Unbind");
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying");
        tearDown();
    }

    private class SetUpTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            sd.clearServiceRequests();
            int i = 3;
            sd.clearLocalServices();
            //for(int i = 0; i <= 3; i++) {
                Map<String, String> record = new HashMap<>();
                String description = "";
                record.put("date", "YYYY/MM/DD HH:MM");
                record.put("title", "Party at the secret coffee shop that you can only hope that " +
                "you're cool enough to know about");
                record.put("location", "secret coffee shop, it's a secret, USA");
                for(int j = 0; j < i; j++) {
                    description += "Just go around back. You can bring friends or friends of " +
                    "friends. It's an 80's style party. Call 123-456-7890 for questions or " +
                    "directions ";
                }
                description.trim();
                record.put("details", description);
                //sd.addServiceRequest("_test" + i);
                sd.registerService("meme" + record.hashCode() + "_date",
                        "date", record.get("date"));
                sd.registerService("meme" + record.hashCode() + "_title",
                        "title", record.get("title"));
                sd.registerService("meme" + record.hashCode() + "_location",
                        "location", record.get("location"));
                sd.registerService("meme" + record.hashCode() + "_details",
                        "details", record.get("details"));
            //}
            sd.addServiceRequest();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            Log.d(TAG, "Finished Set Up");
            sd.discoverServices();
        }
    }

    public void setupTest() {
        new SetUpTask().execute();
    }

    public void addMemeBroadcast(String name, Map<String, String> record) {
        sd.registerService(name, record);
    }

    public void removeMemeBroadcast(String name) {
        sd.removeLocalService(name);
    }

    public void removeAllMemeBroadcasts() {
        sd.clearLocalServices();
    }

    public void addMemeRequest(String name) {
        sd.addServiceRequest(name);
    }

    public void removeMemeRequest(String name) {
        sd.removeServiceRequest(name);
    }

    public void removeAllMemeRequests() {
        sd.clearServiceRequests();
    }

    public void tearDown() {
        if(discoverLoop != null && !discoverLoop.isCancelled())
            discoverLoop.cancel(true);
        if(setUpLoop != null && !setUpLoop.isCancelled())
            setUpLoop.cancel(true);
        removeAllMemeBroadcasts();
        removeAllMemeRequests();
    }
}

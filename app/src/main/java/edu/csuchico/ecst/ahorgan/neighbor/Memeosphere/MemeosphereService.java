package edu.csuchico.ecst.ahorgan.neighbor.Memeosphere;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Database;
import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Event;

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
    private Database db;
    private LiveQuery mQuery;
    private ArrayList<Map<String, String>> localServices;

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
        localServices = new ArrayList<>();
        sd = ServiceDiscovery.getInstance(getApplicationContext());
        sd.linkToService(this);
        db = Database.getInstance(getApplicationContext());
        mQuery = db.getBroadcastEventsView().createQuery().toLiveQuery();
        mQuery.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {
                if(setUpLoop != null && !setUpLoop.isCancelled())
                    setUpLoop.cancel(true);
                sd.clearLocalServices();
                localServices.clear();
                for(QueryRow row : mQuery.getRows()) {
                    Document doc = row.getDocument();
                    Map<String, String> properties = new HashMap<>();
                    for(Map.Entry<String, Object> property : doc.getProperties().entrySet()) {
                        if(property.getKey() != "_id" && property.getKey() != "_rev") {
                            properties.put(property.getKey(), property.getValue().toString());
                        }
                    }
                    localServices.add(properties);
                    sd.registerService(properties);
                }
                setUpLoop = discoverExecutor.scheduleAtFixedRate(setupRunnable,
                        0, 10, TimeUnit.SECONDS);
            }
        });
        try {
            QueryEnumerator results = mQuery.run();
            for (Iterator<QueryRow> it = results; it.hasNext();) {
                QueryRow row = it.next();
                Map<String, String> properties = new HashMap<>();
                for(Map.Entry<String, Object> entry : row.getDocument().getProperties().entrySet()) {
                    if(entry.getKey() != "_id" && entry.getKey() != "_rev"
                            && entry.getKey() != Event.BCAST)
                        properties.put(entry.getKey(), entry.getValue().toString());
                }
                localServices.add(properties);
                sd.registerService(properties);
            }
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getMessage());
        }
        mQuery.start();
        discoverLoop = discoverExecutor.scheduleAtFixedRate(discoverRunnable,
                20, 30, TimeUnit.SECONDS);
        setUpLoop = discoverExecutor.scheduleAtFixedRate(setupRunnable,
                0, 10, TimeUnit.SECONDS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Started");
        String action = null;
        if(intent != null)
            action = intent.getAction();
        if(action != null && action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
            setUpLoop.cancel(true);
            setUpLoop = discoverExecutor.scheduleAtFixedRate(setupRunnable,
                    0, 10, TimeUnit.SECONDS);
        }
        else if(action != null && action
                .equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)) {
            WifiP2pDevice thisDevice = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            sd.setMacAddress(thisDevice.deviceAddress);
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
            //int i = 3;
            sd.clearLocalServices();
            for(Map<String, String> service : localServices) {
                sd.registerService(service);
            }
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

    public void addMemeBroadcast(Map<String, String> record) {
        sd.registerService(record);
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

    public void addMemeToDatabase(Meme meme) {
        Query mQuery = db.getEventsbyOwnerView().createQuery();
        Map<String, Object> properties = new HashMap<>();
        properties.putAll(meme.getProperties());
        mQuery.setKeys(Arrays.asList(properties.get(Event.OWNER)));
        String id = null;
        try {
            QueryEnumerator rows = mQuery.run();
            for(Iterator<QueryRow> it = rows; it.hasNext();) {
                Document document = it.next().getDocument();
                if(properties.containsKey(Event.TITLE) && document.getProperties().get(Event.TITLE)
                        .equals(properties.get(Event.TITLE))) {
                    id = document.getId();
                }
            }
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getMessage());
        }
        properties.put(Event.BCAST, false);
        db.addDocument(id, properties);
    }

    public void tearDown() {
        if(discoverLoop != null && !discoverLoop.isCancelled())
            discoverLoop.cancel(true);
        if(setUpLoop != null && !setUpLoop.isCancelled())
            setUpLoop.cancel(true);
        sd.stopDiscovering();
        removeAllMemeBroadcasts();
        removeAllMemeRequests();
    }
}

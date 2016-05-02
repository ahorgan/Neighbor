package edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by annika on 5/1/16.
 */
public class Database {
    public static String TAG = "Database.java";
    ArrayList<Neighbor> neighbors;
    ArrayList<Profile> profiles;
    ArrayList<Event> events;
    static com.couchbase.lite.Database db;
    static Manager manager;
    static Context mContext;

    public Database(Context context) {
        mContext = context;
    }

    public final static com.couchbase.lite.Database getDatabaseInstance() throws CouchbaseLiteException {
        if ((db == null) & (manager != null)) {
            db = manager.getDatabase("community_db");
        }
        return db;
    }
    public final static Manager getManagerInstance() throws IOException {
        if (manager == null) {
            manager = new Manager(new AndroidContext(mContext), Manager.DEFAULT_OPTIONS);
        }
        return manager;
    }

    public void deleteDocument(String id) {
        try {
            db.deleteLocalDocument(id);
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    public Event addEvent(String id, Map<String, Object> properties) {
        if(id != null) {
            try {
                db.putLocalDocument(id, properties);
                events.add(new Event(mContext, id));
            }
            catch(CouchbaseLiteException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }
    }

    public Neighbor addNeighbor(String id, Map<String, Object> properties) {

    }

    public Profile addProfile(String id, Map<String, Object> properties) {

    }
}

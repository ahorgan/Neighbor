package edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.*;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by annika on 5/1/16.
 */
public class Neighbor {
    public static String PROFILES = "profiles";
    public static String MAC = "mac_address";
    public static String TYPE = "type";
    public static String LINKS = "links";
    private static String TAG = "Neighbor.java";
    private String mac;
    private ArrayList<Profile> profiles;
    private ArrayList<Neighbor> links;
    private String id;
    private Manager manager;
    private com.couchbase.lite.Database db;

    Neighbor(Context context, String mac) {
        initialize(context);
        this.id = mac;
        profiles = new ArrayList<>();
        links = new ArrayList<>();
        if(db.getDocument(id) == null) {
            try {
                db.putLocalDocument(mac, new HashMap<String, Object>());
            }
            catch(CouchbaseLiteException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }
    }

    Neighbor(String id, Context context) {
        initialize(context);this.id = id;
        if(db.getDocument(id) == null) {
            try {
                db.putLocalDocument(id, new HashMap<String, Object>());
                this.id = id;
            }
            catch(CouchbaseLiteException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }
    }

    public void initialize(Context context) {
        try {
            manager = Database.getManagerInstance();
        }
        catch(IOException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        try {
            db = Database.getDatabaseInstance();
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        profiles = new ArrayList<>();
        links = new ArrayList<>();
    }

    public Neighbor updateAttributes(Map<String, Object> attributes) {
        if(attributes.containsKey(MAC)) {
            attributes.remove(MAC);
        }
        if(attributes.containsKey(PROFILES)) {
            ArrayList<Profile> attr_profiles = (ArrayList)attributes.get(PROFILES);
            for(Profile prof : attr_profiles) {
                addProfile(prof);
            }
        }
        Document document = db.getDocument(id);
        Map data = document.getProperties();
        data.putAll(attributes);
        data.put(TYPE, "neighbor");
        try {
            document.putProperties(data);
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return Neighbor.this;
    }

    private void addProfile(Profile profile) {
        profiles.add(profile);
    }

    public String getId() {
        return id;
    }

    public ArrayList<Neighbor> getLinks() {
        return links;
    }

    public ArrayList<Profile> getProfiles() {
        return profiles;
    }
}

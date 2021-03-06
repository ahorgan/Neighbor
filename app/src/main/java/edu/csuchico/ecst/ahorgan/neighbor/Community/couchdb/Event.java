package edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by annika on 5/1/16.
 */
public class Event {
    public static String TITLE = "title";
    public static String LOCATION = "location";
    public static String STARTDATETIME = "start_date";
    public static String ENDDATETIME = "end_date";
    public static String DETAILS = "details";
    public static String OWNER = "owner";
    public static String HEARDFROMPROFILES = "heard_from_profiles";
    public static String TAGS = "tags";
    public static String TYPE = "type";
    public static String BCAST = "bcast";
    private static String TAG = "Event.java";
    private String name;
    private Location location;
    private Date dateTime;
    private String details;
    private Profile owner;
    private ArrayList<Profile> heard_from_profiles;
    private String id;
    private com.couchbase.lite.Database db;
    private Manager manager;

    Event(Context context) {
        initialize(context);
        Document document = db.createDocument();
        id = document.getId();
    }

    Event(Context context, String id) {
        initialize(context);
        this.id = id;
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
        heard_from_profiles = new ArrayList<>();
    }

    public Event updateAttributes(Map<String, Object> attributes) {
        if(attributes.containsKey(TITLE)) {
            this.name = (String)attributes.get(TITLE);
        }
        if(attributes.containsKey(LOCATION)) {
            this.location = (Location) attributes.get(LOCATION);
        }
        if(attributes.containsKey(STARTDATETIME)) {
            this.dateTime = (Date) attributes.get(STARTDATETIME);
        }
        if(attributes.containsKey(ENDDATETIME)) {
            this.dateTime = (Date) attributes.get(ENDDATETIME);
        }
        if(attributes.containsKey(DETAILS)) {
            this.details = (String)attributes.get(DETAILS);
        }
        if(attributes.containsKey(OWNER)) {
            this.owner = (Profile) attributes.get(OWNER);
        }
        if(attributes.containsKey(HEARDFROMPROFILES)) {
            this.heard_from_profiles.addAll((ArrayList<Profile>)attributes.get(HEARDFROMPROFILES));
        }
        Document document = db.getDocument(id);
        Map data = document.getProperties();
        data.put(TYPE, "event");
        data.putAll(attributes);
        try {
            document.putProperties(data);
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return Event.this;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public String getDetails() {
        return details;
    }

    public Profile getOwnerProfile() {
        return owner;
    }

    public ArrayList<Profile> getHeard_from_profiles() {
        return heard_from_profiles;
    }
}

package edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryOptions;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by annika on 5/1/16.
 */
public class Database {
    public static String TAG = "Database.java";
    Map<String, Neighbor> neighbors;
    Map<String, Profile> profiles;
    //Map<String, Event> events;
    static com.couchbase.lite.Database db;
    static Manager manager;
    static Context mContext;
    Profile my_cur_profile;
    View eventsbydateView;
    View eventsbylocationView;
    View eventsbyownerView;
    View profilesbynameView;
    View profilesbycontextView;
    View profilesbyoccupationView;
    View profilesbyeducationView;
    View profilesfemaleView;
    View profilesmaleView;
    View neighborsView;
    View tagsView;

    Mapper eventsbydateMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.get("type").equals("event")) {
                emitter.emit(document.get(Event.DATETIME), document);
            }
        }
    };

    Mapper eventsbyownerMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.get("type").equals("event")) {
                emitter.emit(document.get(Event.OWNERPROFILE), document);
            }
        }
    };

    Mapper eventsbylocationMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.get("type").equals("event")) {
                emitter.emit(document.get(Event.LOCATION), document);
            }
        }
    };

    Mapper profilesbyownerMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.get("type").equals("profile")) {
                emitter.emit(Profile.NAME, document);
            }
        }
    };

    Mapper profilesbycontextMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.get("type").equals("profile")) {
                emitter.emit(Profile.CONTEXT, document);
            }
        }
    };

    Mapper profilesbynameMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.get("type").equals("profile")) {
                emitter.emit(Profile.NAME, document);
            }
        }
    };

    Mapper profilesbyoccupationMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.get("type").equals("profile")) {
                emitter.emit(Profile.OCCUPATION, document);
            }
        }
    };

    Mapper profilesbyeducationMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.get("type").equals("profile")) {
                emitter.emit(Profile.EDUCATION, document);
            }
        }
    };

    Mapper profilesfemaleMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.get("type").equals("profile")
                    && document.get("gender").equals("female")) {
                emitter.emit(Profile.BIRTHDATE, document);
            }
        }
    };

    Mapper profilesmaleMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.get("type").equals("profile")
                    && document.get("gender").equals("male")) {
                emitter.emit(Profile.BIRTHDATE, document);
            }
        }
    };

    Mapper neighborsMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.get("type").equals("neighbor")) {
                List<Object>key = new ArrayList<>();
                key.add(document.get(Neighbor.MAC));

                HashMap<String, Object>value = new HashMap<>();
                value.put("_id", document.get("_id"));
                value.put(Neighbor.PROFILES, document.get(Neighbor.PROFILES));
                emitter.emit(key, value);
            }
        }
    };

    Mapper tagsMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.get("type").equals("profile") || document.get("type").equals("event")) {
                List<Object>key = new ArrayList<>();
                key.add(document.get("type"));
                key.add(document.get("tags"));

                HashMap<String, Object>value = new HashMap<>();
                value.putAll(document);
                emitter.emit(key, value);
            }
        }
    };

    public Database(Context context) {
        mContext = context;
        try {
            manager = getManagerInstance();
        }
        catch(IOException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        try {
            db = getDatabaseInstance();
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        profilesbynameView = db.getView("profiles_by_name");
        profilesbynameView.setMap(profilesbynameMapper, "1");

        profilesbyoccupationView = db.getView("profiles_by_occupation");
        profilesbyoccupationView.setMap(profilesbyoccupationMapper, "1");

        profilesbyeducationView = db.getView("profiles_by_education");
        profilesbyeducationView.setMap(profilesbyeducationMapper, "1");

        profilesbycontextView = db.getView("profiles_by_context");
        profilesbycontextView.setMap(profilesbycontextMapper, "1");

        profilesfemaleView = db.getView("profiles_female");
        profilesfemaleView.setMap(profilesfemaleMapper, "1");

        profilesmaleView = db.getView("profiles_male");
        profilesmaleView.setMap(profilesmaleMapper, "1");

        eventsbydateView = db.getView("events_by_date");
        eventsbydateView.setMap(eventsbydateMapper, "1");

        eventsbylocationView = db.getView("events_by_location");
        eventsbylocationView.setMap(eventsbylocationMapper, "1");

        eventsbyownerView = db.getView("events_by_owner");
        eventsbyownerView.setMap(eventsbyownerMapper, "1");

        neighborsView = db.getView("neighbors");
        neighborsView.setMap(neighborsMapper, "1");

        tagsView = db.getView("tags");
        tagsView.setMap(tagsMapper, "1");
        
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
            return new Event(mContext, id).updateAttributes(properties);
        }
        else {
            return new Event(mContext).updateAttributes(properties);
        }
    }

    public Neighbor addNeighbor(String id, Map<String, Object> properties) {
        if(id != null) {
            return new Neighbor(mContext, id).updateAttributes(properties);
        }
        return null;
    }

    public Profile addProfile(String id, Map<String, Object> properties) {
        if(id != null) {
            return new Profile(mContext, id).updateAttributes(properties);
        }
        else {
            return new Profile(mContext).updateAttributes(properties);
        }
    }
}

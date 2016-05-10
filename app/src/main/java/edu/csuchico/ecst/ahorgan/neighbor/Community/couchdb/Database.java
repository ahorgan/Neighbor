package edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.AsyncTask;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryOptions;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    View profilesbyownerView;
    View profilesbyeducationView;
    View profilesfemaleView;
    View profilesmaleView;
    View neighborsView;
    View tagsView;
    static Database thisDatabase;

    Mapper eventsbydateMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.containsKey("type") && document.get("type").equals("event")) {
                emitter.emit(document.get(Event.DATETIME), document);
            }
        }
    };

    Mapper eventsbyownerMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.containsKey("type") && document.get("type").equals("event")) {
                emitter.emit(document.get(Event.OWNERPROFILE), document);
            }
        }
    };

    Mapper eventsbylocationMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.containsKey("type") && document.get("type").equals("event")) {
                emitter.emit(document.get(Event.LOCATION), document);
            }
        }
    };

    Mapper profilesbyownerMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.containsKey("type") && document.get("type").equals("profile")) {
                emitter.emit(Profile.OWNER, document);
            }
        }
    };

    Mapper profilesbycontextMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.containsKey("type") && document.get("type").equals("profile")) {
                emitter.emit(Profile.CONTEXT, document);
            }
        }
    };

    Mapper profilesbynameMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.containsKey("type") && document.get("type").equals("profile")) {
                emitter.emit(Profile.NAME, document);
            }
        }
    };

    Mapper profilesbyoccupationMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.containsKey("type") && document.get("type").equals("profile")) {
                emitter.emit(Profile.OCCUPATION, document);
            }
        }
    };

    Mapper profilesbyeducationMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.containsKey("type") && document.get("type").equals("profile")) {
                emitter.emit(Profile.EDUCATION, document);
            }
        }
    };

    Mapper profilesfemaleMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.containsKey("type") && document.get("type").equals("profile")
                    && document.get("gender").equals("female")) {
                emitter.emit(Profile.BIRTHDATE, document);
            }
        }
    };

    Mapper profilesmaleMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.containsKey("type") && document.get("type").equals("profile")
                    && document.get("gender").equals("male")) {
                emitter.emit(Profile.BIRTHDATE, document);
            }
        }
    };

    Mapper neighborsMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.containsKey("type") && document.get("type").equals("neighbor")) {
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
            if(document.containsKey("type") && document.get("type").equals("profile") || document.get("type").equals("event")) {
                List<Object>key = new ArrayList<>();
                key.add(document.get("type"));
                key.add(document.get("tags"));

                HashMap<String, Object>value = new HashMap<>();
                value.putAll(document);
                emitter.emit(key, value);
            }
        }
    };

    public final static Database getInstance(Context context) {
        if(thisDatabase == null) {
            thisDatabase = new Database(context);
        }
        return thisDatabase;
    }

    private Database(Context context) {
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
        if (profilesbynameView.getMap() == null)
            profilesbynameView.setMap(profilesbynameMapper, "1");

        profilesbyoccupationView = db.getView("profiles_by_occupation");
        if (profilesbyoccupationView.getMap()  == null)
            profilesbyoccupationView.setMap(profilesbyoccupationMapper, "1");

        profilesbyeducationView = db.getView("profiles_by_education");
        if (profilesbyeducationView.getMap() == null)
            profilesbyeducationView.setMap(profilesbyeducationMapper, "1");

        profilesbycontextView = db.getView("profiles_by_context");
        if (profilesbycontextView.getMap() == null)
            profilesbycontextView.setMap(profilesbycontextMapper, "1");

        profilesbyownerView = db.getView("profiles_by_owner");
        if (profilesbyownerView.getMap() == null)
            profilesbyownerView.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    if(document.containsKey("type") && document.get("type").equals("profile")) {
                        emitter.emit(document.get(Profile.OWNER), document);
                    }
                }
            }, "1");

        profilesfemaleView = db.getView("profiles_female");
        if (profilesfemaleView.getMap() == null)
            profilesfemaleView.setMap(profilesfemaleMapper, "1");

        profilesmaleView = db.getView("profiles_male");
        if (profilesmaleView.getMap() == null)
            profilesmaleView.setMap(profilesmaleMapper, "1");

        eventsbydateView = db.getView("events_by_date");
        if (eventsbydateView.getMap() == null)
            eventsbydateView.setMap(eventsbydateMapper, "1");

        eventsbylocationView = db.getView("events_by_location");
        if (eventsbylocationView.getMap() == null)
            eventsbylocationView.setMap(eventsbylocationMapper, "1");

        eventsbyownerView = db.getView("events_by_owner");
        if (eventsbyownerView.getMap() == null)
            eventsbyownerView.setMap(eventsbyownerMapper, "1");

        neighborsView = db.getView("neighbors");
        if (neighborsView.getMap() == null)
            neighborsView.setMap(neighborsMapper, "1");

        tagsView = db.getView("tags");
        if (tagsView.getMap() == null)
            tagsView.setMap(tagsMapper, "1");
        
    }

    public final static com.couchbase.lite.Database getDatabaseInstance() throws CouchbaseLiteException {
        if ((db == null) && (manager != null)) {
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

    public void addDocument(String id, Map<String, Object> properties) {
        try {
            Document doc;
            if(id == null)
                doc = db.createDocument();
            else
                doc = db.getDocument(id);
            doc.putProperties(properties);
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    public String addEvent(String id, Map<String, Object> properties) {
        if(id != null) {
            return new Event(mContext, id).updateAttributes(properties).getId();
        }
        else {
            return new Event(mContext).updateAttributes(properties).getId();
        }
    }

    public Neighbor addNeighbor(String id, Map<String, Object> properties) {
        if(id != null) {
            return new Neighbor(mContext, id).updateAttributes(properties);
        }
        return null;
    }

    public String addProfile(String id, Map<String, Object> properties) {

        if(id != null) {
            Profile profile = new Profile(mContext, id, properties);
            return profile.getId();
        }
        else {
            Profile profile = new Profile(mContext, properties);
            return profile.getId();
        }
    }

    public View getEventsbydateView() {
        return eventsbydateView;
    }

    public View getEventsbylocationView() {
        return eventsbylocationView;
    }

    public View getEventsbyownerView() {
        return eventsbyownerView;
    }

    public View getProfilesbycontextView() {
        return profilesbycontextView;
    }

    public View getProfilesbyeducationView() {
        return profilesbyeducationView;
    }

    public View getProfilesbynameView() {
        return profilesbynameView;
    }

    public View getProfilesbyoccupationView() {
        return profilesbyoccupationView;
    }

    public View getProfilesbyownerView() {
        return db.getView("profiles_by_owner");
    }

    public View getProfilesfemaleView() {
        return profilesfemaleView;
    }

    public View getProfilesmaleView() {
        return profilesmaleView;
    }

    public View getNeighborsView() {
        return neighborsView;
    }

    public void printQueryToLog(com.couchbase.lite.View view) {
        // Get instance of Query from factory…
        Log.d(TAG, "Print Query To Log:");
        Query query = view.createQuery();
        ArrayList<Object> keys = new ArrayList<>();
        keys.add("me");
        query.setKeys(keys);
        query.setLimit(20);
        try {
            QueryEnumerator results = query.run();
       /* Iterate through the rows to get the document ids */
            for (Iterator<QueryRow> it = results; it.hasNext();) {
                Log.d(TAG, "Next row");
                QueryRow row = it.next();
                Map<String, Object> profile_properties = (Map) row.getValue();
                for (Map.Entry property : profile_properties.entrySet()) {
                    Log.d(TAG, property.getKey().toString() + " : " +
                            property.getValue().toString());
                }
            }
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error querying view.", e);
        }
    }
}

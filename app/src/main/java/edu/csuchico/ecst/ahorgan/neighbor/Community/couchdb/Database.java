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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    Mapper eventsbyownerMapper = new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
            if(document.containsKey("type") && document.get("type").equals("event")) {
                emitter.emit(document.get(Event.OWNER), document);
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
        if(thisDatabase == null && context != null) {
            thisDatabase = new Database(context);
        }
        return thisDatabase;
    }

    private Database(Context context) {
        if(context != null)
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


        profilesfemaleView = db.getView("profiles_female");
        if (profilesfemaleView.getMap() == null)
            profilesfemaleView.setMap(profilesfemaleMapper, "1");

        profilesmaleView = db.getView("profiles_male");
        if (profilesmaleView.getMap() == null)
            profilesmaleView.setMap(profilesmaleMapper, "1");

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
            Document doc = db.getDocument(id);
            if(doc.delete())
                Log.d(TAG, "Successfully deleted item");
            else
                Log.d(TAG, "Did not successfully delete item");
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    public Document getDocument(String id) {
        return db.getDocument(id);
    }

    public void addDocument(String id, Map<String, Object> properties) {
        Log.d(TAG, "add document");
        try {
            Document doc;
            HashMap data;
            if(id == null) {
                doc = db.createDocument();
                data = new HashMap(properties);
            }
            else {
                doc = db.getDocument(id);
                data = new HashMap(doc.getProperties());
                data.putAll(properties);
            }
            doc.putProperties(data);
            for(Map.Entry entry : doc.getProperties().entrySet()) {
                Log.d(TAG, entry.getKey() + " " + entry.getValue());
            }
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    public View getEventsbydateView() {
        View view = db.getView("events_by_date");
        if(view.getMap() == null) {
            view.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                    Date startDate = null;
                    Date endDate = null;
                    Date today = null;
                    try {
                        startDate = dateFormat.parse(document.get(Event.STARTDATETIME)
                                .toString());
                        endDate = dateFormat.parse(document.get(Event.ENDDATETIME).toString());
                        today = Calendar.getInstance().getTime();
                    }
                    catch(ParseException e) {
                        Log.d(TAG, e.getMessage());
                    }
                    if(document.containsKey("type") && document.get("type").equals("event")) {
                        if(startDate != null && endDate != null) {
                            if(endDate.after(today))
                                emitter.emit(Math.abs(today.getTime() - startDate.getTime()),
                                        document);
                        }
                        else {
                            emitter.emit(document.get(Event.STARTDATETIME), document);
                        }
                    }
                }
            }, "1");
        }
        return db.getView("events_by_date");
    }

    public View getBroadcastEventsView() {
        View view = db.getView("events_broadcast");
        if(view.getMap() == null) {
            view.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                    Date startDate = null;
                    Date endDate = null;
                    Date today = null;
                    try {
                        startDate = dateFormat.parse(document.get(Event.STARTDATETIME)
                                .toString());
                        endDate = dateFormat.parse(document.get(Event.ENDDATETIME).toString());
                        today = Calendar.getInstance().getTime();
                    }
                    catch(ParseException e) {
                        Log.d(TAG, e.getMessage());
                    }
                    if(document.containsKey("type") && document.get("type").equals("event")
                            && document.get(Event.BCAST).equals(true)) {
                        if(startDate != null && endDate != null) {
                            if(endDate.after(today) || endDate.equals(today))
                                emitter.emit(Math.abs(today.getTime() - startDate.getTime()),
                                        document);
                        }
                        else {
                            emitter.emit(document.get(Event.STARTDATETIME), document);
                        }
                    }
                }
            }, "1");
        }
        return db.getView("events_broadcast");
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
        View view = db.getView("profiles_by_owner");
        if(view.getMap() == null) {
            view.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    if (document.containsKey("type") && document.get("type").equals("profile")) {
                        emitter.emit(document.get(Profile.OWNER), document);
                    }
                }
            }, "1");
        }
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

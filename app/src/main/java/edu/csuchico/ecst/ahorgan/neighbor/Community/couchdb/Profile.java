package edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.*;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.StreamUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import edu.csuchico.ecst.ahorgan.neighbor.R;

/**
 * Created by annika on 5/1/16.
 */
public class Profile {
    private static String TAG = "Profile.java";
    public static String NAME = "name";
    public static String OCCUPATION = "occupation";
    public static String EDUCATION = "education";
    public static String BIRTHDATE = "birthdate";
    public static String GENDER = "gender";
    public static String MESSAGE = "message";
    public static String EVENTS = "events";
    public static String CONTEXT = "context";
    public static String OWNER = "owner";
    public static String TYPE = "type";
    public static String TAGS = "tags";
    private String name;
    private String occupation;
    private String education;
    private Date birthDate;
    private String gender;
    private String message;
    private ArrayList<Event> events;
    private ArrayList<String> tags;
    private String owner;
    private String id;
    private com.couchbase.lite.Database db;
    private Manager manager;

    Profile(Context context, Map<String, Object> properties) {
        initialize(context);
        Document document = db.createDocument();
        try {
            document.putProperties(properties);
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        id = document.getId();
    }

    Profile(Context context, String id, Map<String, Object> properties) {
        initialize(context);
        Document doc = db.getDocument(id);
        try {
            doc.putProperties(properties);
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
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
        events = new ArrayList<>();
    }

    public Profile updateAttributes(Map<String, Object> attributes) {
        if(attributes.containsKey(NAME)) {
            this.name = (String)attributes.get(NAME);
        }
        if(attributes.containsKey(OCCUPATION)) {
            this.occupation = (String)attributes.get(OCCUPATION);
        }
        if(attributes.containsKey(EDUCATION)) {
            this.education = (String)attributes.get(EDUCATION);
        }
        if(attributes.containsKey(BIRTHDATE)) {
            this.birthDate = (Date)attributes.get(BIRTHDATE);
        }
        if(attributes.containsKey(GENDER)) {
            this.gender = (String)attributes.get(GENDER);
        }
        if(attributes.containsKey(EVENTS)) {
            this.events.addAll((ArrayList<Event>)attributes.get(EVENTS));
        }
        if(attributes.containsKey(MESSAGE)) {
            this.message = (String)attributes.get(MESSAGE);
        }
        if(attributes.containsKey(OWNER)) {
            this.owner = (String)attributes.get(OWNER);
        }
        Document document = db.getDocument(id);

        Map<String, Object> data = new HashMap();
        data = document.getProperties();
        data.putAll(attributes);
        data.put(TYPE, "profile");
        try {
            document.putProperties(data);
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return Profile.this;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEducation() {
        return education;
    }

    public String getOccupation() {
        return occupation;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getGender() {
        return gender;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public String getMessage() {
        return message;
    }

    public String getOwnerNeighbor() {
        return owner;
    }
}

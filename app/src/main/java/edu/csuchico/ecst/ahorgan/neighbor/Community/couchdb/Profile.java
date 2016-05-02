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
    public final String NAME = "name";
    public final String OCCUPATION = "occupation";
    public final String EDUCATION = "education";
    public final String BIRTHDATE = "birthdate";
    public final String GENDER = "gender";
    public final String MESSAGES = "messages";
    public final String EVENTS = "events";
    private String name;
    private String occupation;
    private String education;
    private Date birthDate;
    private String gender;
    private Stack<String> messages;
    private ArrayList<Event> events;
    private Neighbor owner;
    private String id;
    private com.couchbase.lite.Database db;
    private Manager manager;

    Profile(Context context) {
        initialize(context);
        Document document = db.createDocument();
        id = document.getId();
    }

    Profile(Context context, String id) {
        initialize(context);
        this.id = id;
        if(db.getDocument(id) == null)
            throw new RuntimeException("Document " + id + " not found");
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
        messages = new Stack<>();
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
        if(attributes.containsKey(MESSAGES)) {
            this.messages.addAll((Stack<String>)attributes.get(MESSAGES));
        }
        Document document = db.getDocument(id);
        Map data = document.getProperties();
        data.putAll(attributes);
        try {
            document.putProperties(data);
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return Profile.this;
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

    public Stack<String> getMessages() {
        return messages;
    }

    public Neighbor getOwnerNeighbor() {
        return owner;
    }
}

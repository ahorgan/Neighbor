package edu.csuchico.ecst.ahorgan.neighbor.db;

import android.content.Context;
import android.location.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by annika on 2/21/16.
 */
public class Handshake {
    private static String SOURCE = "source";
    private static String DEST = "destination";
    private static String LOCATION = "location";
    private String documentID;
    private Neighbor mSource;
    private Neighbor mDestination;
    private Location mLocation;
    private static dbInterface db;
    private Map<String, Object> mData;

    public Handshake(Context context, Neighbor source, Neighbor destination, Location location) {
        db = new dbInterface(context, "NeighborDB");
        this.mSource = source;
        this.mDestination = destination;
        this.mLocation = location;
        this.mData = new HashMap<String, Object>();
        this.mData.put(SOURCE, this.mSource.getID());
        this.mData.put(DEST, this.mDestination.getID());
        this.mData.put(LOCATION, this.mLocation);
        this.documentID = db.createDocument(this.mData);
    }

    public void updateSource(Neighbor source) {
        mSource = source;
        Map<String, Object> newData = new HashMap<String, Object>();
        newData.put(SOURCE, mSource.getID());
        mData = db.addData(this.documentID, newData);
    }

    public void updateDestination(Neighbor destination){
        mDestination = destination;
        Map<String, Object> newData = new HashMap<String, Object>();
        newData.put(DEST, mDestination.getID());
        mData = db.addData(this.documentID, newData);
    }

    public void updateLocation(Location location){
        mLocation = location;
        Map<String, Object> newData = new HashMap<String, Object>();
        newData.put(LOCATION, mLocation);
        mData = db.addData(this.documentID, newData);
    }

    public void updateData(String key, Object value) {
        Map<String, Object> newData = new HashMap<String, Object>();
        newData.put(key, value);
        mData = db.addData(this.documentID, newData);
    }

    public Neighbor getSource() {
        return mSource;
    }

    public Neighbor getDestination() {
        return mDestination;
    }

    public Map<String, Object> getDataCollection() {
        return mData;
    }

    public String getID() {
        return documentID;
    }
}

package edu.csuchico.ecst.ahorgan.neighbor.db;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by annika on 2/21/16.
 */
public class Neighbor {
    private static String TAG = "Neighbor.java"
    private static String MAC = "mac_address";
    private static String NAME = "name";
    private static String GATEWAY = "gateway";
    private Neighbor mGateway;
    private String mMacAddress;
    private String mName;
    private String documentID;
    private ArrayList<Post> posts;
    private Map<String, Object> mData;
    private static dbInterface db;

    public Neighbor(Context context, String name, String macAddress) {
        db = new dbInterface(context, "NeighborDB");
        this.mName = name;
        this.mMacAddress = macAddress;
        mData = new HashMap<>();
        mData.put(NAME, mName);
        mData.put(MAC, macAddress);
        documentID = db.createDocument(mData);
    }

    public Neighbor(Context context, String docID) {
        db = new dbInterface(context, "NeighborDB");
        Document document
        if((document = db.getDocument(docID)) != null) {
            documentID = docID;
            Map<String, Object> data = new HashMap<>();
            try {
                document.putProperties(data);
                this.mName = data.get(NAME).toString();
                this.mMacAddress = data.get(MAC).toString();
            }
            catch(CouchbaseLiteException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public ArrayList<Post> addPost(Post p) {
        posts.add(p);
        return posts;
    }

    public void updateGateway(Neighbor gateway){
        mGateway = gateway;
        Map<String, Object> newData = new HashMap<String, Object>();
        newData.put(GATEWAY, gateway.getID());
        mData = db.addData(this.documentID, newData);
    }

    public void updateName(String name){
        mName = name;
        Map<String, Object> newData = new HashMap<String, Object>();
        newData.put(NAME, mName);
        mData = db.addData(this.documentID, newData);
    }

    public void updateMacAddress(String mac){
        mMacAddress = mac;
        Map<String, Object> newData = new HashMap<String, Object>();
        newData.put(MAC, mMacAddress);
        mData = db.addData(this.documentID, newData);
    }

    public void updateData(String key, Object value) {
        Map<String, Object> newData = new HashMap<String, Object>();
        newData.put(key, value);
        mData = db.addData(this.documentID, newData);
    }

    public Neighbor getGateway() {
        return mGateway;
    }

    public String getName() {
        return mName;
    }

    public String getMacAddress() {
        return mMacAddress;
    }

    public String getID() {
        return documentID;
    }

    public ArrayList<Post> getPosts() {
        return posts;
    }

    public Map<String, Object> getDataCollection() {
        return mData;
    }
}

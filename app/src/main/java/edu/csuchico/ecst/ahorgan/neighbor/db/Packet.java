package edu.csuchico.ecst.ahorgan.neighbor.db;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import edu.csuchico.ecst.ahorgan.neighbor.db.Content;
import edu.csuchico.ecst.ahorgan.neighbor.db.Neighbor;
import edu.csuchico.ecst.ahorgan.neighbor.db.dbInterface;

/**
 * Created by annika on 2/21/16.
 */
public class Packet {
    private static String OWNER = "owner";
    private static String TITLE = "title";
    private static String CONTENT = "content";
    private Neighbor mOwner;
    private Content mContent;
    private String mTitle;
    private String documentID;
    private static dbInterface db;
    Map<String, Object> mData;

    public Packet(Context context, Neighbor owner, Content content, String title) {
        db = new dbInterface(context, "NeighborDB");
        this.mOwner = owner;
        this.mContent = content;
        this.mTitle = title;
        mData = new HashMap<>();
        mData.put(OWNER, mOwner);
        mData.put(CONTENT, mContent.getDataCollection());
        documentID = db.createDocument(mData);
    }

    public void updateOwner(Neighbor owner) {
        mOwner = owner;
        Map<String, Object> newData = new HashMap<String, Object>();
        newData.put(OWNER, mOwner);
        mData = db.addData(this.documentID, newData);
    }

    public void updateTitle(String title) {
        mTitle = title;
        Map<String, Object> newData = new HashMap<String, Object>();
        newData.put(TITLE, mTitle);
        mData = db.addData(this.documentID, newData);
    }

    public void updateData(String key, Object value) {
        Map<String, Object> newData = new HashMap<String, Object>();
        newData.put(key, value);
        mData = db.addData(this.documentID, newData);
    }

    public void updateContent(Content content) {
        mContent = content;
        Map<String, Object> newData = new HashMap<String, Object>();
        newData.put(CONTENT, mContent);
        mData = db.addData(this.documentID, newData);
    }

    public Neighbor getOwner() {
        return mOwner;
    }

    public String getTitle() {
        return mTitle;
    }

    public Content getContent() {
        return mContent;
    }

    public String getID() {
        return documentID;
    }

    public Map<String, Object> getDataCollection() {
        return mData;
    }


}

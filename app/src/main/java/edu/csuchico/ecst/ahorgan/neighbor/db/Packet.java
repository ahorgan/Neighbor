package edu.csuchico.ecst.ahorgan.neighbor.db;

import android.content.Context;
import android.location.Location;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.couchbase.lite.Document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.csuchico.ecst.ahorgan.neighbor.db.Content;
import edu.csuchico.ecst.ahorgan.neighbor.db.Neighbor;
import edu.csuchico.ecst.ahorgan.neighbor.db.dbInterface;

/**
 * Created by annika on 2/21/16.
 */
public class Packet {
    private static String HEADER = "header";
    private static String SOURCE = "source";
    private static String HOPS = "hops";
    private static String TIME = "time";
    private static String CONTENT = "content";
    private static String DESTINATION = "destination";

    Header mHeader;
    private Content mContent;
    private String documentID;
    private Date mTime;
    private Location mLocation;
    private static dbInterface db;
    Map<String, Object> mData;

    public Packet(Context context, Neighbor src,
                  Neighbor dest,
                  Content content, Date time, Location location) {
        db = new dbInterface(context, "NeighborDB");
        mHeader = new Header(time, location, src, dest, null);
        mContent = content;
        mData = new HashMap<>();
        mData.put(HEADER, mHeader);
        mData.put(CONTENT, mContent.getDataCollection());
        documentID = db.createDocument(mData);
    }

    public void addHop(Neighbor device, Location location) {
        Map<String, Object> newEntry = new HashMap<>();
        Document thisDocument = db.getDocument(documentID);
        Header newHeader = new Header(new Date(location.getTime()), location,
                                        device, mHeader.getDestination(), mHeader);
        mHeader = newHeader;
        newEntry.put(HOPS, mHeader);
        mData = db.addData(this.documentID, newEntry);
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

    public Header getHeader() {
        return mHeader;
    }

    public Content getContent() {
        return mContent;
    }

    public String getID() {
        return documentID;
    }

}

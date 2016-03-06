package edu.csuchico.ecst.ahorgan.neighbor.db;

import android.location.Location;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by annika on 3/4/16.
 */
public class Header {
    private Neighbor source;
    private Header nextHeader;
    private Neighbor destinations;
    private Date time;
    private Location location;

    public Header(Date time, Location location, Neighbor src,
                  Neighbor dst, Header hdr) {
        this.time = time;
        this.location = location;
        source = src;
        destinations = dst;
        nextHeader = hdr;
    }

    public void setNextHeader(Header hdr) { nextHeader = hdr; }
    public Date getTime() { return time; }
    public Location getLocation() { return location; }
    public Neighbor getSource() { return source; }
    public Header getNextHeader() { return nextHeader; }
    public Neighbor getDestination() { return destinations; }
}

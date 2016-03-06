package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.text.format.DateFormat;
import android.util.Pair;

//import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import edu.csuchico.ecst.ahorgan.neighbor.db.Header;
import edu.csuchico.ecst.ahorgan.neighbor.db.Neighbor;
import edu.csuchico.ecst.ahorgan.neighbor.db.Packet;

/**
 * Created by annika on 3/5/16.
 */
public class ForwardingTimeTable {
    private class Entry {
        ArrayList<Location> locations;
        Double probability;
        Neighbor neighbor;
        FTTDate timeMask;

        public Entry(Neighbor n) {
            locations = new ArrayList<Location>();
            probability = 0.0;
            neighbor = n;
        }

        public double updateProbability(double p) {
            if(p <= 1.0 && p >= 0.0) {
                probability = (probability + p)/probability;
            }
            else
                return -1;

            return probability;
        }

        public double updateProbability(boolean peerAvailableAtTime) {
            if(peerAvailableAtTime) {
                return updateProbability(1.0);
            }
            else {
                return updateProbability(0.0);
            }
        }

        public void addLocation(Location l) {
            locations.add(l);
        }

        boolean equalsTime(Date time) {
            for(Location l : locations) {
                DateFormat getTime = new DateFormat();
            }
        }
    }
    private Map<Neighbor, ArrayList<Entry>> ports;
    private Neighbor thisDevice;
    private ArrayList<Neighbor> knownNeighbors;
    private Context mContext;
    private LocationManager locationManager;

    public ForwardingTimeTable(Context context, Neighbor device) {
        mContext = context;
        locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
        thisDevice = device;
    }

    public Neighbor addNeighbor(String name, String addr) {
        Neighbor n = findNeighbor(addr);
        if(n == null) {
            n = new Neighbor(mContext, name, addr)
        }
        return n;
    }

    public Neighbor findNeighbor(String deviceAddress) {
        for(Neighbor n : knownNeighbors) {
            if(n.getMacAddress() == deviceAddress) {
                return n;
            }
        }
        return null;
    }

    /*
        Input: Destination MAC, time
        Output: list of gateways before certain time
     */
    public ArrayList<Entry> lookUpByDeadline(Neighbor MAC, Date BeforeTime) {

    }

    /*
        Returns list of gateways starting with soonest time
     */
    public ArrayList<Entry> lookUpSoonest(Neighbor MAC) {

    }

    /*
        Returns list of possible paths paired with number of untrusted hops in path
     */
    public Pair<Integer, ArrayList<Entry>> lookUpPathsOrderedByTrust(String MAC) {

    }

    public void updateAllProbabilities(Date time) {

    }

    public Entry checkIfEntryExists(Neighbor port, Neighbor check) {
        ArrayList<Entry> entries = ports.get(port);
        for(Entry e : entries) {
            if(e.neighbor == check)
                return e;
        }
        return null;
    }

    public ArrayList<Packet> updateTable(ArrayList<Packet> inComingPackets) {
        for (Packet packet : inComingPackets) {
            Header hdr = packet.getHeader();
            ArrayList<Header> hops = new ArrayList<>();
            Header curHeader = hdr;
            while(curHeader != null) {
                for (int i = 0; i < hops.size(); i++) {
                    for(int j = i+1; j < hops.size(); j++) {
                        /*
                            Check if entry exists
                         */
                        Entry entry = checkIfEntryExists(curHeader.getSource(),
                                hops.get(j).getSource());
                        /*
                            If entry exists: update probability, add time
                            Else: add to knownNeighbors and add entry to port
                         */
                        if(entry != null) {
                            entry.updateProbability(1.0);
                            entry.addLocation(hdr.getLocation());
                        }
                        else {
                            addNeighbor(hops.get(j).getSource().getName(),
                                    hops.get(j).getSource().getMacAddress());
                        }
                    }
                }
                hops.add(curHeader);
                curHeader = hdr.getNextHeader();
            }
            packet.addHop(thisDevice, locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        }
        return inComingPackets;
    }
}

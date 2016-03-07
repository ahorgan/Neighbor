package edu.csuchico.ecst.ahorgan.neighbor.FTT;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Pair;

//import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import edu.csuchico.ecst.ahorgan.neighbor.db.Header;
import edu.csuchico.ecst.ahorgan.neighbor.db.Neighbor;
import edu.csuchico.ecst.ahorgan.neighbor.db.Packet;

/**
 * Created by annika on 3/5/16.
 */
public class ForwardingTimeTable {
    private static final String TAG = "ForwardingTimeTable";
    private class Entry {
        ArrayList<FTTLocation> locations;
        Double probability;
        Neighbor neighbor;
        FTTDate timeMask;

        public Entry(Neighbor n) {
            locations = new ArrayList<>();
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
            locations.add(new FTTLocation(l));
        }

    }
    private Map<Neighbor, ArrayList<Entry>> ports;
    private Neighbor thisDevice;
    private ArrayList<Neighbor> knownNeighbors;
    private Context mContext;
    private LocationManager locationManager;
    private FTTLocationListener locationListener;
    private Location mostRecentLocation;


    public ForwardingTimeTable(Context context, Neighbor device) {
        mContext = context;
        locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
        thisDevice = device;
        locationListener = new FTTLocationListener(this);

        /*
            Request Location Update every 5 minutes
         */
        long FIVE_MINUTES = 5 * 60 * 60 * 1000;
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    FIVE_MINUTES, 0, locationListener);
        }
        catch (SecurityException e) {
            Log.e(TAG, "Permission Denied: Request Location Updates");
        }
    }

    public Neighbor addNeighbor(String name, String addr) {
        Neighbor n = findNeighbor(addr);
        if(n == null) {
            n = new Neighbor(mContext, name, addr);
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
        return null;
    }

    /*
        Returns list of gateways starting with soonest time
     */
    public ArrayList<Entry> lookUpSoonest(Neighbor MAC) {
        return null;
    }

    /*
        Returns list of possible paths paired with number of untrusted hops in path
     */
    public Pair<Integer, ArrayList<Entry>> lookUpPathsOrderedByTrust(String MAC) {
        return null;
    }

    public void updateAllProbabilities(Date time) {

    }

    public void updateLocation(Location location) {
        mostRecentLocation = location;
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
                            entry.addLocation(mostRecentLocation);
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
            packet.addHop(thisDevice, mostRecentLocation);
        }
        return inComingPackets;
    }
}

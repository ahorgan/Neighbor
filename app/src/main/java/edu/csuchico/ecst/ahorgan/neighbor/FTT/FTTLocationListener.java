package edu.csuchico.ecst.ahorgan.neighbor.FTT;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by annika on 3/6/16.
 */
public class FTTLocationListener implements LocationListener {
    private ForwardingTimeTable FTT;

    public FTTLocationListener(ForwardingTimeTable FTT) {
        this.FTT = FTT;
    }
    public void onLocationChanged(Location location) {
        // Called when a new location is found by the network location provider.

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}

}

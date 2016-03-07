package edu.csuchico.ecst.ahorgan.neighbor.FTT;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by annika on 3/6/16.
 */
public class FTTLocation {
    private double latitude;
    private double longitude;
    private String name;

    public FTTLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        name = latitude + " " + longitude;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    float getDistance(FTTLocation compareLocation) {
        float result[] = new float[5];
        Location.distanceBetween(this.latitude, this.longitude,
                compareLocation.getLatitude(), compareLocation.getLongitude(),
                result);
        return result[0];
    }
}

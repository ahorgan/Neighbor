package edu.csuchico.ecst.ahorgan.neighbor.Memeosphere;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.csuchico.ecst.ahorgan.neighbor.Community.couchdb.Event;

/**
 * Created by annika on 8/20/16.
 */
public class Meme {
    private Map<String, String> properties;
    private Set expectedKeys;
    boolean belongsToThisDevice = false;

    Meme() {
        properties = new HashMap<>();
        expectedKeys = new HashSet();
    }

    public void setBelongsToThisDevice(boolean belongs) {
        belongsToThisDevice = belongs;
    }

    Meme addKey(String key) {
        expectedKeys.add(key);
        return this;
    }

    boolean addProperty(String key, String value) {
        properties.put(key, value);
        return properties.keySet().containsAll(expectedKeys);
    }

    boolean containsProperty(String property) {
        return properties.containsKey(property);
    }

    boolean containsKey(String key) {
        return expectedKeys.contains(key);
    }

    boolean isBelongsToThisDevice() {
        return belongsToThisDevice;
    }

    Map<String, String> getProperties() {
        return properties;
    }
}

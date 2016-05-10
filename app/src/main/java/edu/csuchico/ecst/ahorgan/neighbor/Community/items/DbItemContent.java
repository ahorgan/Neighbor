package edu.csuchico.ecst.ahorgan.neighbor.Community.items;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.csuchico.ecst.ahorgan.neighbor.Community.MyProfileRecyclerViewAdapter;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DbItemContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Item> ITEMS = new ArrayList<Item>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Object> ITEM_MAP = new HashMap<String, Object>();

    private static final int COUNT = 25;

    static {
        /*Log.d(TAG, "Running Query");
        Query query = db.getProfilesbyownerView().createQuery();
        ArrayList<Object>key = new ArrayList<>();
        key.add("me");
        query.setKeys(key);
        try {
            QueryEnumerator result = query.run();
            if(result.getCount() > 0) {
                Log.d(TAG, "result size: " + result.getCount());
                for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                    QueryRow row = it.next();
                    Log.d(TAG, row.toString());
                    items.add(row.getDocumentProperties());
                }
            }
            else {
                Log.d(TAG, "No Results");
            }
        }
        catch(CouchbaseLiteException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }*/
    }

    private static void addItem(Item item) {
        ITEMS.add(item);
        ITEM_MAP.put(String.valueOf(item.position), item);
    }

    private static Item createItem(int position) {
        return new Item(position, new HashMap<String, Object>());
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    public static class Item {
        public final Map<String, Object> document;
        public int position;

        public Item(int position, Map<String, Object> document) {
            this.position = position;
            this.document = document;
        }
    }
}


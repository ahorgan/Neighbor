package edu.csuchico.ecst.ahorgan.neighbor.db;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.android.AndroidContext;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by annika on 2/21/16.
 */
public class dbInterface {
    private static String DB_NAME;
    private static String TAG = "dbInterface.java";
    private static Database database = null;
    private static Manager manager = null;

    public dbInterface(Context context, String dbName) {
        DB_NAME = dbName;
        try {
            this.getManagerInstance(context);
            this.getDatabaseInstance();
        }
        catch(CouchbaseLiteException e) {
            Log.e(TAG, e.getMessage());
        }
        catch(IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    public String getDbName() {
        return DB_NAME;
    }
    public Database getDatabaseInstance() throws CouchbaseLiteException {
        if ((this.database == null) & (this.manager != null)) {
            this.database = manager.getDatabase(DB_NAME);
        }
        return database;
    }
    public Manager getManagerInstance(Context context) throws IOException {
        if (manager == null) {
            manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
        }
        return manager;
    }
    public String createDocument(Map<String, Object> data) {
        // Create a new document and add data
        Document document = database.createDocument();
        String documentId = document.getId();
        try {
            // Save the properties to the document
            document.putProperties(data);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error putting", e);
        }
        return documentId;
    }
    public Map<String, Object> addData(String documentID, Map<String, Object> newData) {
        try {
            Document document = getDatabaseInstance().getDocument(documentID);
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.putAll(document.getProperties());
            for (Map.Entry entry : newData.entrySet()) {
                properties.put(entry.getKey().toString(), entry.getValue());
            }
            document.putProperties(properties);
            return properties;
        }
        catch(CouchbaseLiteException e) {
            Log.e(TAG, "Error putting", e);
        }
        return new HashMap<>();
    }

    public Document getDocument(String documentID) {
        try {
            Document document = getDatabaseInstance().getDocument(documentID);
            return document;
        }
        catch(CouchbaseLiteException e) {
            Log.e(TAG, "Error getting", e);
        }
        return null;
    }

    public void deleteDocument(String documentID) {
        // delete the document
        try {
            Document document = getDatabaseInstance().getDocument(documentID);
            document.delete();
            Log.d (TAG, "Deleted document, deletion status = " + document.isDeleted());
        } catch (CouchbaseLiteException e) {
            Log.e (TAG, "Cannot delete document", e);
        }

    }

    public void addAttachment(String documentId) {
        try {
        /* Add an attachment with sample data as POC */
            Document document = getDatabaseInstance().getDocument(documentId);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[] { 0, 0, 0, 0 });
            UnsavedRevision revision = document.getCurrentRevision().createRevision();
            revision.setAttachment("binaryData", "application/octet-stream", //MIME type inputStream);
        /* Save doc & attachment to the local DB */
                    revision.save();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error putting", e);
        }
    }

    public Attachment getAttachment(String documentID) {
        try {
            Document document = getDatabaseInstance().getExistingDocument(documentID);
            SavedRevision saved = document.getCurrentRevision();
    // The content of the attachment is a byte[] we created
            Attachment attach = saved.getAttachment("binaryData");
            int i = 0;
            BufferedReader reader = new BufferedReader(new InputStreamReader(attach.getContent()));
            StringBuffer values = new StringBuffer();
            while (i++ < 4) {
                // We knew the size of the byte array
                // This is the content of the attachment
                values.append(reader.read() + " ");
            }
            Log.v(TAG, "The docID: " + documentID + ", attachment contents was: " + values.toString());
            return attach;
        }
        catch(CouchbaseLiteException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

}

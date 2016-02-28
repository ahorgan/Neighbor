package edu.csuchico.ecst.ahorgan.neighbor;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;

import java.lang.Override;

import edu.csuchico.ecst.ahorgan.neighbor.db.Neighbor;
import edu.csuchico.ecst.ahorgan.neighbor.db.dbInterface;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class DatabaseTest extends ApplicationTestCase<Application> {

    Context context;
    private static final String FAKE_NAME = "John Doe";
    private static final String FAKE_MAC_ADDR = "00:00:00:00:00:00";
    private static final String FAKE_GW_NAME = "Gateway Smith";
    private static final String FAKE_GW_MAC_ADDR = "aa:bb:cc:dd:ee:ff";

    public DatabaseTest() {
        super(Application.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        context = getContext();
    }

    public void testDatabaseNeighbor() {
        Neighbor neighborTest = new Neighbor(context, FAKE_NAME, FAKE_MAC_ADDR);
        Neighbor gatewayTest = new Neighbor(context, FAKE_GW_NAME, FAKE_GW_MAC_ADDR);
        dbInterface db = new dbInterface(context, Neighbor.DB_NAME);

        // ...when the string is returned from the object under test...
        String name = neighborTest.getName();
        String mac = neighborTest.getMacAddress();
        String id = neighborTest.getID();
        neighborTest.updateGateway(gatewayTest);
        Neighbor gw = neighborTest.getGateway();
        Document doc = db.getDocument(id);

        // ...then the result should be the expected one.
        assertEquals(name, doc.getProperty(Neighbor.NAME));
        assertEquals(mac, doc.getProperty(Neighbor.MAC));
        assertEquals(id, doc.getId());
        assertEquals(gw.getID(), doc.getProperty(Neighbor.GATEWAY));

        assertEquals(neighborTest.delete(), true);
        assertEquals(gatewayTest.delete(), true);

    }

}
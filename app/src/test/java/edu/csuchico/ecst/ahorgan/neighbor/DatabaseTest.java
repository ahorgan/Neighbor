package edu.csuchico.ecst.ahorgan.neighbor;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.test.ApplicationTestCase;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContext;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;

import org.junit.runner.RunWith;
import org.mockito.Mock;

import edu.csuchico.ecst.ahorgan.neighbor.db.Neighbor;
import edu.csuchico.ecst.ahorgan.neighbor.db.dbInterface;

public class DatabaseTest extends ApplicationTestCase<Application> {

    @Mock
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
        createApplication();
        context = getApplication();
    }

    public void testDatabaseNeighbor() {
        Neighbor neighborTest = new Neighbor(context, FAKE_NAME, FAKE_MAC_ADDR);
        Neighbor gatewayTest = new Neighbor(context, FAKE_GW_NAME, FAKE_GW_MAC_ADDR);
        dbInterface db = new dbInterface(context, Neighbor.DB_NAME);

        String name = neighborTest.getName();
        String mac = neighborTest.getMacAddress();
        String id = neighborTest.getID();
        neighborTest.updateGateway(gatewayTest);
        Neighbor gw = neighborTest.getGateway();
        Document doc = db.getDocument(id);

        assertEquals(name, doc.getProperty(Neighbor.NAME));
        assertEquals(mac, doc.getProperty(Neighbor.MAC));
        assertEquals(id, doc.getId());
        assertEquals(gw.getID(), doc.getProperty(Neighbor.GATEWAY));

        neighborTest.delete();
        gatewayTest.delete();
        assertNull(db.getDocument(id));
        assertNull(db.getDocument(gw.getID()));

    }
}
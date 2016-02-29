package edu.csuchico.ecst.ahorgan.neighbor;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.test.ApplicationTestCase;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

import edu.csuchico.ecst.ahorgan.neighbor.p2p.World;

public class WorldTest extends ApplicationTestCase<Application> {

    private Context mContext;
    private World mWorld;
    private WifiInfo mInfo;

    public WorldTest(){   super(Application.class);   }
    public void setUp() throws Exception {
        super.setUp();
        createApplication();
        mContext = getApplication();
    }

    public void testOnConnectionInfoAvailable() {
        WifiP2pManager manager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = manager.initialize(mContext, Looper.getMainLooper(), null);
        mWorld = Mockito.mock(World.class);
        doCallRealMethod().when(mWorld).setChannel(any(WifiP2pManager.Channel.class));
        doCallRealMethod().when(mWorld).setManager(any(WifiP2pManager.class));
        doCallRealMethod().when(mWorld).getChannel();
        doCallRealMethod().when(mWorld).getManager();
        doCallRealMethod().when(mWorld).requestConnectionInfo();
        doReturn(any(WifiP2pInfo.class)).when(mWorld).onConnectionInfoAvailable(any(WifiP2pInfo.class));
        mWorld.setChannel(channel);
        mWorld.setManager(manager);
        mWorld.requestConnectionInfo();
        assertEquals(mWorld.getManager(), manager);
        assertEquals(mWorld.getChannel(), channel);
        /*mWorld = new World(mContext);
        boolean success = false;

        doReturn(success = true).when(mWorld).onConnectionInfoAvailable(any(WifiP2pInfo.class));
        mWorld.requestConnectionInfo();
        assertTrue(success);*/
    }
}

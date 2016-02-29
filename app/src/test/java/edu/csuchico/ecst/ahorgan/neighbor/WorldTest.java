package edu.csuchico.ecst.ahorgan.neighbor;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.test.ApplicationTestCase;
import android.util.Log;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import edu.csuchico.ecst.ahorgan.neighbor.p2p.World;

public class WorldTest extends ApplicationTestCase<Application> {

    private Context mContext;
    private WifiInfo mInfo;

    public WorldTest(){   super(Application.class);   }
    public void setUp() throws Exception {
        super.setUp();
        createApplication();
        mContext = getApplication();
    }
    @Test
    public void onConnectionInfoAvailableTest() {
        World world = World(mContext);
        world.requestConnectionInfo();
        verify(world, never()).onConnectionInfoAvailable(any(WifiP2pInfo.class));
        verify(world, atLeastOnce()).onConnectionInfoAvailable(any(WifiP2pInfo.class));
        verify(world, atLeast(2)).onConnectionInfoAvailable(any(WifiP2pInfo.class));
    }
}

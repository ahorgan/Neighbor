package edu.csuchico.ecst.ahorgan.neighbor;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
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

import edu.csuchico.ecst.ahorgan.neighbor.p2p.WorldConnectionInfoListener;
import edu.csuchico.ecst.ahorgan.neighbor.p2p.WorldPeerListener;

public class WorldTest extends ApplicationTestCase<Application> {

    private Context mContext;
    private WorldConnectionInfoListener mWorld;
    private WorldPeerListener mPeer;
    private WifiInfo mInfo;

    public WorldTest() {
        super(Application.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        createApplication();
        mContext = getApplication();
    }

    public void testOnConnectionInfoAvailable() {
        WifiP2pManager manager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = manager.initialize(mContext, Looper.getMainLooper(), null);
        mWorld = Mockito.mock(WorldConnectionInfoListener.class);
        doCallRealMethod().when(mWorld).setChannel(any(WifiP2pManager.Channel.class));
        doCallRealMethod().when(mWorld).setManager(any(WifiP2pManager.class));
        doCallRealMethod().when(mWorld).getChannel();
        doCallRealMethod().when(mWorld).getManager();
        mWorld.setChannel(channel);
        mWorld.setManager(manager);
        assertEquals(mWorld.getManager(), manager);
        assertEquals(mWorld.getChannel(), channel);
        manager.requestConnectionInfo(channel, mWorld);
        verify(mWorld, atLeastOnce()).onConnectionInfoAvailable(any(WifiP2pInfo.class));
    }

    public void testOnPeersAvailable() {
        WifiP2pManager manager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = manager.initialize(mContext, Looper.getMainLooper(), null);
        mPeer = Mockito.mock(WorldPeerListener.class);
        doCallRealMethod().when(mPeer).setChannel(any(WifiP2pManager.Channel.class));
        doCallRealMethod().when(mPeer).setManager(any(WifiP2pManager.class));
        doCallRealMethod().when(mPeer).getChannel();
        doCallRealMethod().when(mPeer).getManager();
        mPeer.setChannel(channel);
        mPeer.setManager(manager);
        assertEquals(mPeer.getManager(), manager);
        assertEquals(mPeer.getChannel(), channel);
        manager.requestPeers(channel, mPeer);
        verify(mPeer, atLeastOnce()).onPeersAvailable(any(WifiP2pDeviceList.class));

    }
}

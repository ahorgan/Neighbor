package edu.csuchico.ecst.ahorgan.neighbor.p2p;

import android.content.Context;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.util.Log;

import com.couchbase.lite.Manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by annika on 2/28/16.
 */
public class WorldConnectionInfoListener implements WifiP2pManager.ConnectionInfoListener {
    private final static String TAG = "WorldConnInfoListener";
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private WifiP2pManager.ConnectionInfoListener mConnectionInfoListener;
    private WifiP2pManager.ActionListener mActionListener;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private WifiP2pDeviceList mDeviceList;
    private WifiP2pConfig mConfig;
    private WifiP2pManager.GroupInfoListener mGroupInfoListener;
    private World mWorld;
    private WifiP2pDevice thisDevice;
    private WifiP2pGroup mGroup = null;
    private List<WifiP2pDevice> peerList;
    private ClientCommunicationHandler clientHandler;
    private boolean groupFormed;
    private boolean groupOwner;

    public WorldConnectionInfoListener(World world, WifiP2pManager manager, WifiP2pManager.Channel channel) {
        mWorld = world;
        mManager = manager;
        mChannel = channel;
        peerList = new ArrayList<>();
        groupFormed = false;
        groupOwner = false;
    }

    public void setGroup(WifiP2pGroup group) {
        mGroup = group;
        peerList.clear();
        peerList.addAll(mGroup.getClientList());
    }

    public void setChannel(WifiP2pManager.Channel channel) {
        mChannel = channel;
    }

    public void setManager(WifiP2pManager manager) {
        mManager = manager;
    }

    public void setGroupInfoListener(WorldGroupInfoListener listener) {
        mGroupInfoListener = listener;
    }

    public void setPeerListListener(WifiP2pManager.PeerListListener mPeerListListener) {
        this.mPeerListListener = mPeerListListener;
    }

    public void setPeerList(List<WifiP2pDevice> peerList) {
        this.peerList = peerList;
    }

    public WifiP2pManager.Channel getChannel() {    return mChannel;    }
    public WifiP2pManager getManager() {    return mManager;    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        groupFormed = info.groupFormed;
        if(groupFormed) {
            Log.d(TAG, "onConnectionInfoAvailable: Group Formed");
            /*
                Start exchanging packets using group owner as router
             */
            groupOwner = info.isGroupOwner;
            mManager.requestGroupInfo(mChannel, mGroupInfoListener);
            if(groupOwner) {
                Log.d(TAG, "I'm Group Owner!");
                // Connected as 'router and peer'
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ServerSocket socket = mWorld.getServerSocket();
                            Log.d(TAG, "Using socket on port " + socket.getLocalPort());
                            while(true) {
                                    Socket client = socket.accept();
                                    Log.d(TAG, "Client accepted");
                                    new Thread(new ServerCommunicationHandler(thisDevice, client)).start();
                            }
                        }
                        catch(IOException e) {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                }).start();
            }
            else {
                Log.d(TAG, "I'm a Peer in the Group");
                Log.d(TAG, "Owner: " + info.groupOwnerAddress);
                Log.d(TAG, "Looking for owner in peerList, size " + peerList.size());
                for(WifiP2pDevice device : peerList) {
                    Log.d(TAG, device.deviceAddress + " " + device.deviceName);
                    if(device.isGroupOwner()) {
                        Log.d(TAG, "Found Group Owner!");
                        clientHandler = new ClientCommunicationHandler(info, device);
                        if(mWorld.getNeighbors().size() > 0)
                            startClientSocket();
                        break;
                    }
                }
            }
        }
        else {
            Log.d(TAG, "onConnectionInfoAvailable: Group Not Formed");

        }
    }

    public void startClientSocket() {
        new Thread(clientHandler).start();
    }

    class ServerCommunicationHandler implements Runnable {
        private Socket socket;
        private WifiP2pDevice device;
        public ServerCommunicationHandler(WifiP2pDevice device, Socket socket) {
            super();
            Log.d(TAG, "Server Communication Handler Created");
            this.device = device;
            this.socket = socket;
        }
        @Override
        public void run() {
            Log.d(TAG, "Handling Communication with" + socket.getInetAddress() + "on port" + socket.getLocalPort());
            try {
                OutputStream outStream = socket.getOutputStream();
                if(device != null) {
                    outStream.write(("Hello from" + device.deviceName).getBytes());
                }
                else {
                    outStream.write(("Hello from me").getBytes());
                }
                outStream.close();
            }
            catch(IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    class ClientCommunicationHandler implements Runnable {
        private WifiP2pInfo info;
        private WifiP2pDevice device;

        ClientCommunicationHandler(WifiP2pInfo info, WifiP2pDevice device) {
            super();
            Log.d(TAG, "Client Communication Handler Created");
            this.info = info;
            this.device = device;
        }

        @Override
        public void run() {
            Socket socket = new Socket();
            try {
                Log.d(TAG, "Connecting socket as client");
                HashMap<String, Integer> neighbors = mWorld.getNeighbors();
                int port = 2468;
                if(neighbors.containsKey(device.deviceAddress)) {
                    Log.d(TAG, "Getting port number from Neighbors");
                    port = neighbors.get(device.deviceAddress);
                }
                Log.d(TAG, "Connecting to port " + String.valueOf(port));
                socket.bind(null);
                socket.connect(new InetSocketAddress(info.groupOwnerAddress, port), 500);
                InputStream inStream = socket.getInputStream();
                byte msg[] = new byte[1024];
                inStream.read(msg);
                Log.d(TAG, msg.toString());
                inStream.close();
                socket.close();
            }
            catch(IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    public void setThisDevice(WifiP2pDevice device) {
        if(device != null) {
            thisDevice = device;
            Log.d(TAG, "This Device: Name=" + thisDevice.deviceName +
                    " Addr=" + thisDevice.deviceAddress);
        }
    }

    public WifiP2pDevice getThisDevice() {
        return thisDevice;
    }

    public boolean isGroupFormed() {
        return groupFormed;
    }

    public boolean isGroupOwner() {
        return groupOwner;
    }

}

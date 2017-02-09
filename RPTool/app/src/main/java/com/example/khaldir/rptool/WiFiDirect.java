package com.example.khaldir.rptool;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static android.os.Looper.getMainLooper;

/**
 * Created by JakeT12 on 09/02/2017.
 */
public class WiFiDirect implements WifiP2pManager.ConnectionInfoListener{
    private static WiFiDirect ourInstance = null;

    public static WiFiDirect getInstance(Context context)
    {
        if (ourInstance == null)
            ourInstance = new WiFiDirect(context);
        return ourInstance;
    }

    //Listens for WiFi events
    public final IntentFilter intentFilter = new IntentFilter();
    //Handlers
    Handler updateConversationHandler;

    //Connected IPs
    private final ArrayList<InetAddress> addressConnectionsList = new ArrayList<InetAddress>();


    //Sockets
    private Socket socket;
    private ServerSocket serverSocket;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    Context context;

    //Threads
    Thread serverThread;

    private WiFiDirect(final Context context) {
        this.context = context;

        peers = new ArrayList<WifiP2pDevice>();
        peerStrings = new ArrayList<String>();

        //Setup intentFilter
        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //Retrieve Channel
        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, getMainLooper(), new WifiP2pManager.ChannelListener() {

            @Override
            public void onChannelDisconnected() {
                Utilities.newToast(context,"Channel disconnected!");
            }
        });

        updateConversationHandler = new Handler();

        //Clean up previous connections
        disconnect();

        //Open Threads
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();


    }

    public void disconnect() {
        if (mManager != null && mChannel != null) {
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null
                            ) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Utilities.newToast(context,"Successfully Disconnected");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Utilities.newToast(context,"Disconnect Error: " + reason);
                            }
                        });
                    }
                }
            });
        }
    }



    private void parseJSON(String jsonFile)
    {
        try {
            JSONObject object = new JSONObject(jsonFile);
            if (object.has("clientIP"))
            {
                addressConnectionsList.add(InetAddress.getByName(object.getString("clientIP")));
            }
            if (object.has("message"))
            {
                Utilities.newToast(context, object.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    //List of Peers
    public List<WifiP2pDevice> peers;
    public List<String> peerStrings;

    public void peerListFill(WifiP2pDeviceList peerList)
    {
            peers.clear();
            peers.addAll(peerList.getDeviceList());
            peerStrings.clear();
            //Stringify devices
            for (WifiP2pDevice device : peers) {
                peerStrings.add(device.toString());
            }
            // If an AdapterView is backed by this data, notify it
            // of the change.  For instance, if you have a ListView of
            // available peers, trigger an update.
            mAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, peerStrings);
    }

    ArrayAdapter<String> mAdapter;


    //Device to Connect to
    public WifiP2pDevice connectDevice;

    protected void connectToDevice() {
        if (connectDevice != null) {
            WifiP2pConfig WiFiConfig = new WifiP2pConfig();
            WiFiConfig.deviceAddress = connectDevice.deviceAddress;
            WiFiConfig.wps.setup = WpsInfo.PBC;

            mManager.connect(mChannel, WiFiConfig, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.

                }

                @Override
                public void onFailure(int reason) {
                    Utilities.newToast(context,"Connect failed. Retry.");
                }
            });
        } else {
            Utilities.newToast(context,"Select a Device");
        }
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo p2pInfo) {
        // InetAddress from WifiP2pInfo struct.
        if (addressConnectionsList.contains(p2pInfo.groupOwnerAddress)) {
            Utilities.newToast(context,"Already connected to peer.");
            return;
        }
        if (p2pInfo.isGroupOwner && p2pInfo.groupFormed) {
            Utilities.newToast(context,"Group formed, Im the GO!");

            addressConnectionsList.add(p2pInfo.groupOwnerAddress);


        } else if (p2pInfo.groupFormed) {
            new Thread(new ClientThread(p2pInfo.groupOwnerAddress, 8888, Utilities.getDottedDecimalIP(Utilities.getLocalIPAddress()))).start();
            Utilities.newToast(context,"Connected as peer.");
            addressConnectionsList.add(p2pInfo.groupOwnerAddress);
        }
    }

    // Threads

    public class ClientThread implements Runnable {

        private InetAddress serverIP;
        private int serverPort;
        private String message;

        public ClientThread(InetAddress serverIP, int serverPort, String message) {
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                socket = new Socket(serverIP, serverPort);
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(message);
            } catch (UnknownHostException e1) {
                e1.printStackTrace();

            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }

    class ServerThread implements Runnable {
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket stSocket = serverSocket.accept();
                    CommunicationThread commThread = new CommunicationThread(stSocket);
                    new Thread(commThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    updateConversationHandler.post(new updateUIThread(read));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    class updateUIThread implements Runnable {
        private String msg;
        public updateUIThread(String str) {
            this.msg = str;
        }
        @Override
        public void run() {
            Utilities.newToast(context,"Message Recieved");
            parseJSON(msg);
        }
    }
}

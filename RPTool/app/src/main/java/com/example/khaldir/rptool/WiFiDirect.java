package com.example.khaldir.rptool;

import android.content.Context;
import android.content.Intent;
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
        else
            ourInstance.context = context;
        return ourInstance;
    }

    //Listens for WiFi events
    public final IntentFilter intentFilter = new IntentFilter();
    //Handlers
    Handler updateConversationHandler;

    //Connected IPs
    private final ArrayList<InetAddress> addressConnectionsList = new ArrayList<InetAddress>();
    public InetAddress gmIP;
    public InetAddress pilotIP;
    public InetAddress shieldIP;
    public InetAddress weaponIP;
    public InetAddress scannerIP;
    public InetAddress engineIP;

    //Sockets
    private Socket socket;
    private ServerSocket serverSocket;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    Context context;

    //Threads
    Thread serverThread;

    //Fields
    //All
    int EnginePower;

    //Engines
    int PilotEnergyIn;
    int SensorEnergyIn;
    int WeaponEnergyIn;
    int ShieldEnergyIn;

    //Shields
    int frontShields;
    int leftShields;
    int rightShields;
    int rearShields;
    int frontShieldHP;
    int leftShieldHP;
    int rightShieldHP;
    int rearShieldHP;

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

        EnginePower = 10;
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
        if (jsonFile != null)
        {
            try
            {
                JSONObject object = new JSONObject(jsonFile);
                if (object.has("clientIP"))
                    addressConnectionsList.add(InetAddress.getByName(object.getString("clientIP")));
                else if (object.has("message"))
                    Utilities.newToast(context, object.getString("message"));
                else if (object.has("maxEnginePower"))
                    EnginePower = Integer.getInteger(object.getString("maxEnginePower"));
                else if (object.has("pilotEnergyIn"))
                    PilotEnergyIn = object.getInt("pilotEnergyIn");
                else if (object.has("shieldEnergyIn"))
                    ShieldEnergyIn = object.getInt("shieldEnergyIn");
                else if (object.has("weaponEnergyIn"))
                    WeaponEnergyIn = object.getInt("weaponEnergyIn");
                else if (object.has("scannerEnergyIn"))
                    SensorEnergyIn = object.getInt("scannerEnergyIn");
                else if (object.has("frontShields"))
                    frontShields = Integer.getInteger(object.getString("frontShields"));
                else if (object.has("leftShields"))
                    leftShields = Integer.getInteger(object.getString("leftShields"));
                else if (object.has("rightShields"))
                    rightShields = Integer.getInteger(object.getString("rightShields"));
                else if (object.has("rearShields"))
                    rearShields = Integer.getInteger(object.getString("rearShields"));
                else if (object.has("frontShieldHP"))
                    frontShieldHP = Integer.getInteger(object.getString("frontShieldHP"));
                else if (object.has("leftShieldHP"))
                    leftShieldHP = Integer.getInteger(object.getString("leftShieldHP"));
                else if (object.has("rightShieldHP"))
                    rightShieldHP = Integer.getInteger(object.getString("rightShieldHP"));
                else if (object.has("rearShieldHP"))
                    rearShieldHP = Integer.getInteger(object.getString("rearShieldHP"));
                else
                {
                    Utilities.newToast(context, jsonFile);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
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
            gmIP = p2pInfo.groupOwnerAddress;
            sendValue("clientIP",Utilities.getDottedDecimalIP(Utilities.getLocalIPAddress()),gmIP);
            new Thread(new ClientThread(p2pInfo.groupOwnerAddress, 8888, Utilities.getDottedDecimalIP(Utilities.getLocalIPAddress()))).start();
            Utilities.newToast(context,"Connected as peer.");
            addressConnectionsList.add(p2pInfo.groupOwnerAddress);

            // For Test Purposes
            Intent engineIntent = new Intent(context,EngineActivity.class);
            context.startActivity(engineIntent);
        }
    }

    Thread SendVal;

    public void sendValue(String tag, String value, InetAddress recipient)
    {
        String message = Utilities.createJSON(value,tag);
        new Thread(new ClientThread(recipient, 8888, message)).start();
    }

    public void sendValue(String tag, String value)
    {
        String message = Utilities.createJSON(value,tag);
        for (InetAddress recipient:addressConnectionsList
             )
        {
            new Thread(new ClientThread(recipient, 8888, message)).start();
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
                return;
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
                    if(read==null)
                        return;
                    updateConversationHandler.post(new updateUIThread(read));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return;
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
            return;
        }
    }
}

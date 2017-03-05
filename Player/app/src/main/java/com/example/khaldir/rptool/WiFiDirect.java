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
import java.util.Set;

import static android.os.Looper.getMainLooper;

/**
 * Created by JakeT12 on 09/02/2017.
 */
public class WiFiDirect implements WifiP2pManager.ConnectionInfoListener{
    private static WiFiDirect ourInstance = null;

    public static WiFiDirect getInstance(ReactorClass context)
    {
        if (ourInstance == null)
            ourInstance = new WiFiDirect(context);
        else
            ourInstance.context = context;
        return ourInstance;
    }

    public static WiFiDirect getInstance()
    {
        return ourInstance;
    }

    //Listens for WiFi events
    public final IntentFilter intentFilter = new IntentFilter();

    //Handlers
    Handler updateConversationHandler;

    //Connected IPs
    public final ArrayList<InetAddress> addressConnectionsList = new ArrayList<InetAddress>();
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
    ReactorClass context;

    //Threads
    Thread serverThread;

    boolean isGroupOwner;

    //Fields
    //All
    int EnginePower;
    int currentLocation;
        //-1 - No Location
        //0 - GM
        //1 - Pilot
        //2 - Shields
        //3 - Weapons
        //4 - Scanners
        //5 - Engines

    //Engines
    int PilotEnergyIn;
    int SensorEnergyIn;
    int WeaponEnergyIn;
    int ShieldEnergyIn;
    boolean isEnginesEditable;

    //Shields
    int frontShields;
    int leftShields;
    int rightShields;
    int rearShields;
    int frontShieldHP;
    int leftShieldHP;
    int rightShieldHP;
    int rearShieldHP;
    boolean isShieldsEditable;

    //Pilot
    int speed;
    int speedMultiplier;
    int dodge;
    boolean isPilotEditable;

    //Weapons
    List<WeaponItem> weaponInfo;
    boolean isWeaponsEditable;

    //Scanners
    int scanPool;
    List<ScanItem> scanData;
    int enemyCount;
    int shipHealth;
    boolean isScannerEditable;

    private WiFiDirect(final ReactorClass context) {
        this.context = context;

        weaponInfo = new ArrayList<WeaponItem>();
        scanData = new ArrayList<ScanItem>();
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
        speedMultiplier = 10;

        isGroupOwner = false;
        isPilotEditable = isShieldsEditable = isWeaponsEditable = isScannerEditable = isEnginesEditable = true;
        currentLocation = -1;
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
            try {
                JSONObject object = new JSONObject(jsonFile);
                if (object.has("clientIP") && !addressConnectionsList.contains(InetAddress.getByName(object.getString("clientIP"))))
                {
                    addressConnectionsList.add(InetAddress.getByName(object.getString("clientIP")));
                }
                else if (object.has("message"))
                {
                    JSONObject message = new JSONObject(object.getString("message"));
                    if (message.has("pilot") && currentLocation == 1)
                        Utilities.newSnackbar(context, message.getString("pilot"));
                    else if (message.has("shields") && currentLocation == 2)
                        Utilities.newSnackbar(context,message.getString("shields"));
                    else if (message.has("weapons") && currentLocation == 3)
                        Utilities.newSnackbar(context,message.getString("weapons"));
                    else if (message.has("scanners") && currentLocation == 4)
                        Utilities.newSnackbar(context,message.getString("scanners"));
                    else if (message.has("engines") && currentLocation == 5)
                        Utilities.newSnackbar(context,message.getString("engines"));
                    else if (message.has("gm") && currentLocation == 0)
                        Utilities.newSnackbar(context,message.getString("gm"));
                    else if (message.has("all"))
                        Utilities.newSnackbar(context,message.getString("all"));
                }
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
                    frontShields = object.getInt("frontShields");
                else if (object.has("leftShields"))
                    leftShields = object.getInt("leftShields");
                else if (object.has("rightShields"))
                    rightShields = object.getInt("rightShields");
                else if (object.has("rearShields"))
                {
                    rearShields = object.getInt("rearShields");
                    isShieldsEditable = false;
                }
                else if (object.has("frontShieldHP"))
                    frontShieldHP = object.getInt("frontShieldHP");
                else if (object.has("leftShieldHP"))
                    leftShieldHP = object.getInt("leftShieldHP");
                else if (object.has("rightShieldHP"))
                    rightShieldHP = object.getInt("rightShieldHP");
                else if (object.has("rearShieldHP"))
                    rearShieldHP = object.getInt("rearShieldHP");
                else if (object.has("speed"))
                    speed = object.getInt("speed");
                else if (object.has("speedMulti"))
                    speedMultiplier = object.getInt("speedMulti");
                else if (object.has("dodge"))
                    dodge = object.getInt("dodge");
                else if (object.has("enable"))
                    makeEditable();
                else if (object.has("scanPool"))
                    scanPool = object.getInt("scanPool");
                else if (object.has("scanData"))
                {
                    if (object.has("nullify"))
                    {
                        scanData.remove(object.getInt("scanID"));
                    }
                    else
                    {
                        ScanItem scan = new ScanItem(object.getString("scanID"),object.getString("scanDesc"),object.getString("scanType"));
                        scanData.add(scan);
                    }
                }
                else if (object.has("enemyCount"))
                    enemyCount = object.getInt("enemyCount");
                else if (object.has("shipHealth"))
                    shipHealth = object.getInt("shipHealth");
                else if (object.has("weaponDetail"))
                {
                    if (object.has("nullify"))
                    {
                        weaponInfo.remove(object.getInt("weaponID"));
                    }
                    else
                    {
                        WeaponItem weapon = new WeaponItem(object.getString("weaponID"),object.getString("weaponName"),object.getString("weaponDesc"),object.getString("weaponPower"),object.getBoolean("weaponOn"));
                        weaponInfo.add(weapon);
                    }
                }
                else if (object.has("pilot"))
                    pilotIP = InetAddress.getByName(object.getString("pilot"));
                else if (object.has("shields"))
                    shieldIP = InetAddress.getByName(object.getString("shields"));
                else if (object.has("weapons"))
                    weaponIP = InetAddress.getByName(object.getString("weapons"));
                else if (object.has("scanners"))
                    scannerIP = InetAddress.getByName(object.getString("scanners"));
                else if (object.has("engines"))
                    engineIP = InetAddress.getByName(object.getString("engines"));
                else
                {
                    Utilities.newSnackbar(context, jsonFile);
                }
                context.reactToChanges();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public void makeEditable()
    {
        isPilotEditable = true;
        isShieldsEditable = true;
        isWeaponsEditable = true;
        isScannerEditable = true;
        isEnginesEditable = true;
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
                peerStrings.add(device.deviceName);
            }
            // If an AdapterView is backed by this data, notify it
            // of the change.  For instance, if you have a ListView of
            // available peers, trigger an update.
            mAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, peerStrings);
    }

    ArrayAdapter<String> mAdapter;


    //Device to Connect to
    public WifiP2pDevice connectDevice;

    protected boolean connectToDevice() {
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
            return true;
        } else {
            Utilities.newToast(context,"Select a Device");
            return false;
        }
    }

    public void reconnect()
    {
        for (InetAddress address:addressConnectionsList)
        {
            WifiP2pConfig WiFiConfig = new WifiP2pConfig();
            WiFiConfig.deviceAddress = address.getHostAddress();
            WiFiConfig.wps.setup = WpsInfo.PBC;
            mManager.connect(mChannel, WiFiConfig, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.

                }

                @Override
                public void onFailure(int reason) {
                    Utilities.newToast(context,"Connect failed. Retry. Err:" + reason);
                }
            });
        }
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo p2pInfo) {
        // InetAddress from WifiP2pInfo struct.
        gmIP = p2pInfo.groupOwnerAddress;
        if (addressConnectionsList.contains(p2pInfo.groupOwnerAddress)) {
            Utilities.newToast(context,"Already connected to peer.");
            return;
        }
        if (p2pInfo.isGroupOwner && p2pInfo.groupFormed) {
            Utilities.newToast(context,"Group formed, Im the GO!");
            isGroupOwner = true;
            addressConnectionsList.add(p2pInfo.groupOwnerAddress);



        } else if (p2pInfo.groupFormed) {
            isGroupOwner = false;
            gmIP = p2pInfo.groupOwnerAddress;
            sendValue("clientIP",Utilities.getDottedDecimalIP(Utilities.getLocalIPAddress()),gmIP);
            new Thread(new ClientThread(p2pInfo.groupOwnerAddress, 8888, Utilities.getDottedDecimalIP(Utilities.getLocalIPAddress()))).start();
            Utilities.newToast(context,"Connected as peer.");
            addressConnectionsList.add(p2pInfo.groupOwnerAddress);
        }
    }


    public void sendValue(String tag, String value, InetAddress recipient)
    {
        String message = Utilities.createJSON(value,tag);
        new Thread(new ClientThread(recipient, 8888, message)).start();
    }

    public void sendValue(String message, InetAddress recipient)
    {
        new Thread(new ClientThread(recipient, 8888, message)).start();
    }

    public void sendValue(String message)
    {
        boolean firstAddress = true;
        for (InetAddress recipient:addressConnectionsList)
        {
            // First address is always own Device
            if(!firstAddress)
                new Thread(new ClientThread(recipient, 8888, message)).start();
            firstAddress = false;
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
            //If GM:
            if (currentLocation == 0)
            {
                Utilities.newToast(context,"Sending to all");
                sendValue(msg);
            }
            else
                Utilities.newToast(context,"Message Recieved");
            parseJSON(msg);
            return;
        }
    }
}

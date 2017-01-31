package com.example.khaldir.rptool;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.R.attr.action;
import static android.R.attr.visible;
import static android.os.Looper.getMainLooper;
import static java.util.Collections.addAll;

public class MainActivity extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener {

    // Global Variables

    //Context
    Context context;

    //Listens for WiFi events
    private final IntentFilter intentFilter = new IntentFilter();

    //WiFi Channel
    WifiP2pManager.Channel mChannel;

    //WiFi Peer to Peer Manager
    WifiP2pManager mManager;

    //Records state of Wifi P2P
    private boolean IsWifiP2pEnabled;

    //Broadcast receiver
    WiFiDirectBroadcastReceiver receiver;

    //List of Peers
    private List<WifiP2pDevice> peers;
    private List<String> peerStrings;

    //Spinner
    private Spinner deviceSpinner;

    //Connect Button
    private Button connectButton;

    //Device to Connect to
    private WifiP2pDevice connectDevice;

    //Host address
    InetAddress groupOwnerAddress;

    //Connected IPs
    private final ArrayList<InetAddress> addressConnectionsList = new ArrayList<InetAddress>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        context = this;
        IsWifiP2pEnabled = false;
        peers = new ArrayList<WifiP2pDevice>();
        peerStrings = new ArrayList<String>();
        deviceSpinner = (Spinner)findViewById(R.id.deviceList);
        connectButton = (Button)findViewById(R.id.connectButton);

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
            mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            mChannel = mManager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener() {

                @Override
                public void onChannelDisconnected() {
                    Toast.makeText(context, "Channel disconnected!",
                            Toast.LENGTH_SHORT).show();
                }
            });

        //Clean up previous connections
        disconnect();

    }

    public void setIsWifiP2pEnabled(boolean in)
    {
        IsWifiP2pEnabled = in;
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this, peerListListener);
        registerReceiver(receiver, intentFilter);
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
                                Toast.makeText(MainActivity.this, "removeGroup onSuccess -",
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(int reason) {
                                Toast.makeText(MainActivity.this, "removeGroup onFailure -" + reason,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        disconnect();
    }

    protected void discoverPeers(View sender){
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank.  Code for peer discovery goes in the
                // onReceive method, detailed below.

                connectButton.setVisibility(View.VISIBLE);


            }

            @Override
            public void onFailure(int reasonCode) {
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.
            }
        });
    }



    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            //Get List of Peers
            peers.clear();
            peers.addAll(peerList.getDeviceList());
            peerStrings.clear();

            //Stringify devices
            for (WifiP2pDevice device:peers)
            {
                peerStrings.add(device.toString());
            }


            // If an AdapterView is backed by this data, notify it
            // of the change.  For instance, if you have a ListView of
            // available peers, trigger an update.

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, peerStrings);
            deviceSpinner.setAdapter(adapter);

            deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    connectButton.setText("Connect to " + peers.get(position).deviceName);
                    connectDevice = peers.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });

            // Perform any other updates needed based on the new list of
            // peers connected to the Wi-Fi P2P network.

        }
    };

   protected void connectToDevice (View view)
   {
       if (connectDevice != null){
           WifiP2pConfig WiFiConfig = new WifiP2pConfig();
           WiFiConfig.deviceAddress = connectDevice.deviceAddress;
           WiFiConfig.wps.setup = WpsInfo.PBC;

           mManager.connect(mChannel, WiFiConfig, new WifiP2pManager.ActionListener(){
               @Override
               public void onSuccess() {
                   // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                   findViewById(R.id.sendTextBox).setVisibility(View.VISIBLE);
                   findViewById(R.id.sendTextButton).setVisibility(View.VISIBLE);
               }

               @Override
               public void onFailure(int reason) {
                   Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                           Toast.LENGTH_SHORT).show();
               }
           });
       }
       else{
           Toast.makeText(MainActivity.this, "Select a Device",
                   Toast.LENGTH_SHORT).show();
       }
   }


    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo p2pInfo) {
        // InetAddress from WifiP2pInfo struct.
        FileServerAsyncTask handlerThread = new FileServerAsyncTask(this);
        if (addressConnectionsList.contains(p2pInfo.groupOwnerAddress)) {
            Toast.makeText(MainActivity.this, "Already connected to peer.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (p2pInfo.isGroupOwner && p2pInfo.groupFormed) {
            Toast.makeText(MainActivity.this, "Group formed, Im the GO!", Toast.LENGTH_SHORT).show();

            addressConnectionsList.add(p2pInfo.groupOwnerAddress);
            try {
                handlerThread.execute();
                addressConnectionsList.add(InetAddress.getByName(handlerThread.get()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        } else if (p2pInfo.groupFormed) {
            Toast.makeText(MainActivity.this, "Connected as peer.", Toast.LENGTH_SHORT).show();
            addressConnectionsList.add(p2pInfo.groupOwnerAddress);
        }
    }

    private byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }

    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }


    private void sendString(InetAddress targetAddress, String message)
    {
        int len;
        Socket socket = null;


        byte buf[]  = new byte[1024];

        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */

            if (groupOwnerAddress!=null)
                socket.connect((new InetSocketAddress(targetAddress, 0)), 500);

            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data will be retrieved by the server device.
             */
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = null;
            inputStream = new ByteArrayInputStream(message.getBytes());
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            //catch logic
        } catch (IOException e) {
            //catch logic
        }

/**
 * Clean up any open sockets when done
 * transferring or if an exception occurred.
 */
        finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //catch logic
                    }
                }
            }
        }
    }

    protected void sendTextClick (View view) {
        EditText messageBox = (EditText)findViewById(R.id.sendTextBox);
        for (InetAddress address:addressConnectionsList)
        {
            sendString(address, messageBox.getText().toString());
        }
    }
}

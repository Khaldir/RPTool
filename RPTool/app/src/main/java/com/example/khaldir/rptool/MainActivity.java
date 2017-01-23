package com.example.khaldir.rptool;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.R.attr.action;
import static android.os.Looper.getMainLooper;
import static java.util.Collections.addAll;

public class MainActivity extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener {

    // Global Variables

    //Context
    Context context = this;

    //Listens for WiFi events
    private final IntentFilter intentFilter = new IntentFilter();

    //WiFi Channel
    WifiP2pManager.Channel mChannel;

    //WiFi Peer to Peer Manager
    WifiP2pManager mManager;

    //Records state of Wifi P2P
    private boolean IsWifiP2pEnabled = false;

    //Broadcast receiver
    WiFiDirectBroadcastReceiver receiver;

    //List of Peers
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private List<String> peerStrings = new ArrayList<String>();

    //Spinner
    private Spinner deviceSpinner = (Spinner)findViewById(R.id.deviceList);

    //Connect Button
    private Button connectButton = (Button)findViewById(R.id.connectButton);

    //Device to Connect to
    private WifiP2pDevice connectDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            mChannel = mManager.initialize(this, getMainLooper(), null);


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

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    protected void discoverPeers(){
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
                    connectButton.setText("Connect to " + peerStrings.get(position));
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

   protected void connectToDevice ()
   {
       if (connectDevice != null){
           WifiP2pConfig WiFiConfig = new WifiP2pConfig();
           WiFiConfig.deviceAddress = connectDevice.deviceAddress;
           WiFiConfig.wps.setup = WpsInfo.PBC;

           mManager.connect(mChannel, WiFiConfig, new WifiP2pManager.ActionListener(){
               @Override
               public void onSuccess() {
                   // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
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
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        // InetAddress from WifiP2pInfo struct.
        try {
            InetAddress groupOwnerAddress = InetAddress.getByName(info.groupOwnerAddress.getHostAddress());
        } catch (UnknownHostException e){
            Toast.makeText(MainActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

        // After the group negotiation, we can determine the group owner
        // (server).
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
        } else if (info.groupFormed) {
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
        }
    }
}

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
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

public class MainActivity extends ReactorClass {

    // Global Variables

    ReactorClass context = this;


    //Broadcast receiver
    WiFiDirectBroadcastReceiver receiver;

    //Spinner
    private Spinner deviceSpinner;

    //Connect Button
    private Button connectButton;

    boolean isConnected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        wifiObject = WiFiDirect.getInstance(context);

        isConnected = false;
        deviceSpinner = (Spinner) findViewById(R.id.deviceList);
        connectButton = (Button) findViewById(R.id.connectButton);
        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList)
            {
                wifiObject.peerListFill(peerList);

                deviceSpinner.setAdapter(wifiObject.mAdapter);

                deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        if (!isConnected)
                            connectButton.setText("Connect to " + wifiObject.peers.get(position).deviceName);
                        wifiObject.connectDevice = wifiObject.peers.get(position);
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
    }


    /**
     * register the BroadcastReceiver with the intent values to be matched
     */
    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(wifiObject.mManager, wifiObject.mChannel, this, peerListListener);
        registerReceiver(receiver, wifiObject.intentFilter);
    }


    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiObject.disconnect();
    }

    protected void discoverPeers(View sender) {
        wifiObject.mManager.discoverPeers(wifiObject.mChannel, new WifiP2pManager.ActionListener() {

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


    private WifiP2pManager.PeerListListener peerListListener;


    protected void connectToDevice(View view) {
        if(!isConnected)
        {
            isConnected = wifiObject.connectToDevice();
            if (isConnected)
                connectButton.setText("Go to Player Screen");
            findViewById(R.id.nextActivityButton).setVisibility(View.VISIBLE);
        }


    }

    protected void nextActivity(View sender)
    {
        Intent intent = new Intent(context,PlayerActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void reactToChanges() {
        super.reactToChanges();
        TextView connectedDevices = (TextView)findViewById(R.id.connectedDevices);
        String text = "Connected Devices" + System.lineSeparator();
        for (InetAddress address:wifiObject.addressConnectionsList)
        {
            text = text + address.toString() + System.lineSeparator();
        }
        connectedDevices.setText(text);
    }
}

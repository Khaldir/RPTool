package com.example.khaldir.rptool;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScannerActivity extends ReactorClass
        implements NavigationView.OnNavigationItemSelectedListener {

    List<RelativeLayout> scanRow;
    List<TextView> scanNames;
    List<TextView> scanDescriptions;
    List<Button> scanButtons;
    ProgressBar availablePower;
    TextView availPower;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        wifiObject = WiFiDirect.getInstance(this);
        wifiObject.currentLocation = 4;
        sendLocation("scanners");

        initialise();
    }

    private void initialise()
    {
        availablePower = (ProgressBar) findViewById(R.id.scanPowerBar);
        availPower = (TextView) findViewById(R.id.availScanPower);

        scanRow = new ArrayList<RelativeLayout>();
        scanRow.add((RelativeLayout) findViewById(R.id.scan1));
        scanRow.add((RelativeLayout) findViewById(R.id.scan2));
        scanRow.add((RelativeLayout) findViewById(R.id.scan3));
        scanRow.add((RelativeLayout) findViewById(R.id.scan4));
        scanRow.add((RelativeLayout) findViewById(R.id.scan5));
        scanRow.add((RelativeLayout) findViewById(R.id.scan6));
        scanRow.add((RelativeLayout) findViewById(R.id.scan7));
        scanRow.add((RelativeLayout) findViewById(R.id.scan8));
        scanRow.add((RelativeLayout) findViewById(R.id.scan9));
        scanRow.add((RelativeLayout) findViewById(R.id.scan10));

        scanNames = new ArrayList<TextView>();
        scanNames.add((TextView) findViewById(R.id.scanType1));
        scanNames.add((TextView) findViewById(R.id.scanType2));
        scanNames.add((TextView) findViewById(R.id.scanType3));
        scanNames.add((TextView) findViewById(R.id.scanType4));
        scanNames.add((TextView) findViewById(R.id.scanType5));
        scanNames.add((TextView) findViewById(R.id.scanType6));
        scanNames.add((TextView) findViewById(R.id.scanType7));
        scanNames.add((TextView) findViewById(R.id.scanType8));
        scanNames.add((TextView) findViewById(R.id.scanType9));
        scanNames.add((TextView) findViewById(R.id.scanType10));

        scanDescriptions = new ArrayList<TextView>();
        scanDescriptions.add((TextView) findViewById(R.id.scanDesc1));
        scanDescriptions.add((TextView) findViewById(R.id.scanDesc2));
        scanDescriptions.add((TextView) findViewById(R.id.scanDesc3));
        scanDescriptions.add((TextView) findViewById(R.id.scanDesc4));
        scanDescriptions.add((TextView) findViewById(R.id.scanDesc5));
        scanDescriptions.add((TextView) findViewById(R.id.scanDesc6));
        scanDescriptions.add((TextView) findViewById(R.id.scanDesc7));
        scanDescriptions.add((TextView) findViewById(R.id.scanDesc8));
        scanDescriptions.add((TextView) findViewById(R.id.scanDesc9));
        scanDescriptions.add((TextView) findViewById(R.id.scanDesc10));

        scanButtons = new ArrayList<Button>();
        scanButtons.add((Button) findViewById(R.id.scanSend1));
        scanButtons.add((Button) findViewById(R.id.scanSend2));
        scanButtons.add((Button) findViewById(R.id.scanSend3));
        scanButtons.add((Button) findViewById(R.id.scanSend4));
        scanButtons.add((Button) findViewById(R.id.scanSend5));
        scanButtons.add((Button) findViewById(R.id.scanSend6));
        scanButtons.add((Button) findViewById(R.id.scanSend7));
        scanButtons.add((Button) findViewById(R.id.scanSend8));
        scanButtons.add((Button) findViewById(R.id.scanSend9));
        scanButtons.add((Button) findViewById(R.id.scanSend10));

        reactToChanges();
    }

    @Override
    public void reactToChanges() {
        availPower.setText("Available Power: "+String.valueOf(wifiObject.SensorEnergyIn));
        availablePower.setMax(wifiObject.EnginePower);
        availablePower.setProgress(wifiObject.SensorEnergyIn);

        // For each scan
        for (int i = 0; i < 10; i++) {
            // Mark all as empty
            scanRow.get(i).setVisibility(View.INVISIBLE);

            // If the slot isn't empty:
            if (wifiObject.scanData.size() > i)
                if (!wifiObject.scanData.get(i).equals(null))
                {
                    scanRow.get(i).setVisibility(View.VISIBLE);
                    scanNames.get(i).setText(wifiObject.scanData.get(i).type);
                    scanDescriptions.get(i).setText(wifiObject.scanData.get(i).description);
                }




        }
    }

    protected void scanSend(View sender)
    {
        sendValues((int)sender.getTag());
    }

    private void sendValues(int id)
    {
        //Get Destination
        JSONObject jsonChild;
        JSONObject jsonParent;
        switch((String)scanNames.get(id).getText())
        {
            //If the scanned item is an Enemy:
            case "Enemy":
                jsonChild = Utilities.addtoJSON(new JSONObject(),(String)scanDescriptions.get(id).getText(),"weapons");
                jsonParent = Utilities.addtoJSON(new JSONObject(),jsonChild.toString(),"message");
                wifiObject.sendValue(jsonParent.toString(),wifiObject.gmIP);
                break;
            case "Number of Enemies":
                jsonChild = Utilities.addtoJSON(new JSONObject(),(String)scanDescriptions.get(id).getText(),"weapons");
                jsonParent = Utilities.addtoJSON(new JSONObject(),jsonChild.toString(),"message");
                wifiObject.sendValue(jsonParent.toString(),wifiObject.gmIP);
                break;
            case "Ship Health":
                jsonChild = Utilities.addtoJSON(new JSONObject(),(String)scanDescriptions.get(id).getText(),"shields");
                jsonParent = Utilities.addtoJSON(new JSONObject(),jsonChild.toString(),"message");
                wifiObject.sendValue(jsonParent.toString(),wifiObject.gmIP);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        wifiObject.reconnect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sensor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearScanner()
    {
        wifiObject.scannerIP = null;
        clearLocation("scanners");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_pilot) {
            if (wifiObject.pilotIP.equals(wifiObject.nullIP))
            {
                clearScanner();
                Intent pilotIntent = new Intent(this,PilotActivity.class);
                this.startActivity(pilotIntent);
            }
            else
                Utilities.newToast(this,"There is already someone at this Station!");
        } else if (id == R.id.nav_shields) {
            if (wifiObject.shieldIP.equals(wifiObject.nullIP))
            {
                clearScanner();
                Intent shieldIntent = new Intent(this,ShieldsActivity.class);
                this.startActivity(shieldIntent);
            }
            else
                Utilities.newToast(this,"There is already someone at this Station!");
        } else if (id == R.id.nav_weapons) {
            if (wifiObject.weaponIP.equals(wifiObject.nullIP))
            {
                clearScanner();
                Intent weaponIntent = new Intent(this,WeaponsActivity.class);
                this.startActivity(weaponIntent);
            }
            else
                Utilities.newToast(this,"There is already someone at this Station!");
        } else if (id == R.id.nav_engines) {
            if (wifiObject.engineIP.equals(wifiObject.nullIP))
            {
                clearScanner();
                Intent engineIntent = new Intent(this,EngineActivity.class);
                this.startActivity(engineIntent);
            }
            else
                Utilities.newToast(this,"There is already someone at this Station!");
        } else if (id == R.id.nav_connect) {
            clearScanner();
            Intent connectIntent = new Intent(this,PlayerActivity.class);
            this.startActivity(connectIntent);
            finish();
        } else if (id == R.id.nav_sensors) {
            Utilities.newToast(this,"You are already at the Sensors!");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

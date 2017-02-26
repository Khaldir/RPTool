package com.example.khaldir.rptool;

import android.content.Intent;
import android.net.sip.SipSession;
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
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class PilotActivity extends ReactorClass
        implements NavigationView.OnNavigationItemSelectedListener {


    int maxEngineOutput;
    int availablePower;

    TextView speed;
    int speedVal;
    TextView dodge;
    int dodgeVal;

    ProgressBar availablePowerBar;

    SeekBar distBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilot);
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
        maxEngineOutput = wifiObject.EnginePower;
        availablePower = wifiObject.PilotEnergyIn;

        speed = (TextView) findViewById(R.id.speedVal);
        dodge = (TextView) findViewById(R.id.dodgeVal);

        availablePowerBar = (ProgressBar) findViewById(R.id.maxPilotEnergy);
        availablePowerBar.setMax(maxEngineOutput);
        availablePowerBar.setProgress(availablePower);

        distBar = (SeekBar) findViewById(R.id.pilotDistribution);
        distBar.setMax(availablePower);
        distBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser)
            {
                //Determine Speed
                speedVal = distBar.getProgress();
                //Determine Dodge [speedVal + dodgeVal = getMax()]
                dodgeVal = distBar.getMax()-speedVal;
                //Show Values;
                speed.setText(speedVal*wifiObject.speedMultiplier);
                dodge.setText(dodgeVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        wifiObject.currentLocation = 1;
        sendLocation("pilot");

    }

    protected void updatePilot(View sender) {
        wifiObject.sendValue("speed",String.valueOf(speedVal),wifiObject.gmIP);
        wifiObject.sendValue("dodge",String.valueOf(dodgeVal),wifiObject.gmIP);
    }



    @Override
    public void reactToChanges() {
        maxEngineOutput = wifiObject.EnginePower;
        availablePower = wifiObject.PilotEnergyIn;
        speedVal = wifiObject.speed;
        dodgeVal = wifiObject.dodge;
        distBar.setEnabled(wifiObject.isPilotEditable);
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
        getMenuInflater().inflate(R.menu.pilot, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_engines) {
            if (wifiObject.engineIP == null)
            {
                wifiObject.engineIP = null;
                Intent engineIntent = new Intent(this,EngineActivity.class);
                this.startActivity(engineIntent);
            }
            else
                Utilities.newToast(this,"There is already someone at this Station!");
        } else if (id == R.id.nav_shields) {
            if (wifiObject.shieldIP == null)
            {
                wifiObject.engineIP = null;
                Intent shieldIntent = new Intent(this,ShieldsActivity.class);
                this.startActivity(shieldIntent);
            }
            else
                Utilities.newToast(this,"There is already someone at this Station!");
        } else if (id == R.id.nav_weapons) {
            if (wifiObject.weaponIP == null)
            {
                wifiObject.engineIP = null;
                Intent weaponIntent = new Intent(this,WeaponsActivity.class);
                this.startActivity(weaponIntent);
            }
            else
                Utilities.newToast(this,"There is already someone at this Station!");
        } else if (id == R.id.nav_sensors) {
            if (wifiObject.scannerIP == null)
            {
                wifiObject.engineIP = null;
                Intent sensorIntent = new Intent(this,SensorActivity.class);
                this.startActivity(sensorIntent);
            }
            else
                Utilities.newToast(this,"There is already someone at this Station!");
        } else if (id == R.id.nav_pilot) {
            Utilities.newToast(this,"You are already at the Helm!");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

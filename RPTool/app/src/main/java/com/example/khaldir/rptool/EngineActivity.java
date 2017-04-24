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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;

public class EngineActivity extends ReactorClass
        implements NavigationView.OnNavigationItemSelectedListener {



    SeekBar pilotBar;
    SeekBar shieldBar;
    SeekBar weaponBar;
    SeekBar scannerBar;

    int maxEngineOutput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engine);
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

        pilotBar = (SeekBar) findViewById(R.id.pilotEnergy);
        shieldBar = (SeekBar) findViewById(R.id.shieldEnergy);
        weaponBar = (SeekBar) findViewById(R.id.weaponEnergy);
        scannerBar = (SeekBar) findViewById(R.id.scannerEnergy);

        maxEngineOutput = wifiObject.EnginePower;

        pilotBar.setMax(maxEngineOutput);
        shieldBar.setMax(maxEngineOutput);
        weaponBar.setMax(maxEngineOutput);
        scannerBar.setMax(maxEngineOutput);

        wifiObject.currentLocation = 5;
        sendLocation("engines");


    }

    @Override
    protected void onResume() {
        super.onResume();
        wifiObject.reconnect();
    }

    @Override
    public void reactToChanges()
    {
        maxEngineOutput = wifiObject.EnginePower;

        pilotBar.setMax(maxEngineOutput);
        pilotBar.setEnabled(wifiObject.isEnginesEditable);
        shieldBar.setMax(maxEngineOutput);
        shieldBar.setEnabled(wifiObject.isEnginesEditable);
        weaponBar.setMax(maxEngineOutput);
        weaponBar.setEnabled(wifiObject.isEnginesEditable);
        scannerBar.setMax(maxEngineOutput);
        scannerBar.setEnabled(wifiObject.isEnginesEditable);
    }

    protected void submitEnergy(View sender) {
        if (pilotBar.getProgress() + shieldBar.getProgress() + weaponBar.getProgress() + scannerBar.getProgress() <= maxEngineOutput)
        {
            wifiObject.sendValue("pilotEnergyIn", String.valueOf(pilotBar.getProgress()),wifiObject.gmIP);
            wifiObject.PilotEnergyIn = pilotBar.getProgress();
            wifiObject.sendValue("shieldEnergyIn", String.valueOf(shieldBar.getProgress()),wifiObject.gmIP);
            wifiObject.ShieldEnergyIn = shieldBar.getProgress();
            wifiObject.sendValue("weaponEnergyIn", String.valueOf(weaponBar.getProgress()),wifiObject.gmIP);
            wifiObject.WeaponEnergyIn = weaponBar.getProgress();
            wifiObject.sendValue("scannerEnergyIn", String.valueOf(scannerBar.getProgress()),wifiObject.gmIP);
            wifiObject.SensorEnergyIn = scannerBar.getProgress();
        }
        else
        {
            Utilities.newSnackbar(findViewById(android.R.id.content),"Overallocated Power!");
        }
        if (maxEngineOutput != wifiObject.EnginePower)
            maxEngineOutput = wifiObject.EnginePower;
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
        getMenuInflater().inflate(R.menu.engine, menu);
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

    private void clearEngines()
    {
        wifiObject.engineIP = null;
        clearLocation("engines");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_pilot) {
            if (wifiObject.pilotIP.equals(wifiObject.nullIP))
            {
                clearEngines();
                Intent pilotIntent = new Intent(this,PilotActivity.class);
                this.startActivity(pilotIntent);
                finish();
            }
            else
                Utilities.newToast(this,"There is already someone at this Station!");
        } else if (id == R.id.nav_shields) {
            if (wifiObject.shieldIP.equals(wifiObject.nullIP))
            {
                clearEngines();
                Intent shieldIntent = new Intent(this,ShieldsActivity.class);
                this.startActivity(shieldIntent);
                finish();
            }
            else
                Utilities.newToast(this,"There is already someone at this Station!");
        } else if (id == R.id.nav_weapons) {
            if (wifiObject.weaponIP.equals(wifiObject.nullIP))
            {
                clearEngines();
                Intent weaponIntent = new Intent(this,WeaponsActivity.class);
                this.startActivity(weaponIntent);
                finish();
            }
            else
                Utilities.newToast(this,"There is already someone at this Station!");
        } else if (id == R.id.nav_sensors) {
            if (wifiObject.scannerIP.equals(wifiObject.nullIP))
            {
                clearEngines();
                Intent sensorIntent = new Intent(this,ScannerActivity.class);
                this.startActivity(sensorIntent);
                finish();
            }
            else
                Utilities.newToast(this,"There is already someone at this Station!");
        } else if (id == R.id.nav_connect) {
            clearEngines();
            Intent connectIntent = new Intent(this,PlayerActivity.class);
            this.startActivity(connectIntent);
            finish();
        } else if (id == R.id.nav_engines) {
        Utilities.newToast(this,"You are already at the Engines!");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




}

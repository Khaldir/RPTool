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
import android.widget.TextView;

import java.net.InetAddress;

public class PlayerActivity extends ReactorClass
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView gm;
    TextView pilot;
    TextView shields;
    TextView weapons;
    TextView scanners;
    TextView engines;
    TextView others;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
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

        gm = (TextView) findViewById(R.id.gmText);
        pilot = (TextView) findViewById(R.id.pilotText);
        shields = (TextView) findViewById(R.id.shieldsText);
        weapons = (TextView) findViewById(R.id.weaponsText);
        scanners = (TextView) findViewById(R.id.scannersText);
        engines = (TextView) findViewById(R.id.enginesText);
        others = (TextView) findViewById(R.id.othersText);

        reactToChanges();

    }

    @Override
    public void reactToChanges() {
        if (wifiObject.gmIP != null)
            gm.setText("GM: "  +wifiObject.gmIP.getHostAddress());
        if (wifiObject.pilotIP != null)
            pilot.setText("Pilot: " + wifiObject.pilotIP.getHostAddress());
        if (wifiObject.shieldIP != null)
            shields.setText("Shields: " + wifiObject.shieldIP.getHostAddress());
        if (wifiObject.weaponIP != null)
            weapons.setText("Weapons: " + wifiObject.weaponIP.getHostAddress());
        if (wifiObject.scannerIP != null)
            scanners.setText("Scanners: " + wifiObject.scannerIP.getHostAddress());
        if (wifiObject.engineIP != null)
            engines.setText("Engines: " + wifiObject.engineIP.getHostAddress());
        String othersText = "Not at a Station:" + System.lineSeparator();
        for (InetAddress address : wifiObject.addressConnectionsList)
        {
            if (address != null && !address.equals(wifiObject.gmIP) && !address.equals(wifiObject.pilotIP) && !address.equals(wifiObject.shieldIP) && !address.equals(wifiObject.weaponIP) && !address.equals(wifiObject.scannerIP) && !address.equals(wifiObject.engineIP))
            {
                othersText = othersText + address.getHostAddress();
            }
        }
        others.setText(othersText);
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
        getMenuInflater().inflate(R.menu.player, menu);
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
        boolean alreadyThere = false;

        Intent gmIntent = new Intent();

        if (id == R.id.nav_gm) {
            gmIntent = new Intent(this,GMActivity.class);
        } else if (id == R.id.nav_pilot) {
            gmIntent = new Intent(this,PilotActivity.class);
        } else if (id == R.id.nav_shields) {
            gmIntent = new Intent(this,ShieldsActivity.class);
        } else if (id == R.id.nav_weapons) {
            gmIntent = new Intent(this,WeaponsActivity.class);
        } else if (id == R.id.nav_sensors) {
            gmIntent = new Intent(this,ScannerActivity.class);
        } else if (id == R.id.nav_engines) {
            gmIntent = new Intent(this,EngineActivity.class);
        } else if (id == R.id.nav_connect) {
            alreadyThere = true;
        }

        if (!alreadyThere)
        {
            this.startActivity(gmIntent);
            finish();
        }
        else
        {
            Utilities.newToast(this,"You are already at this Location!");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

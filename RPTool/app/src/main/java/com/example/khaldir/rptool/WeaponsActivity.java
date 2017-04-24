package com.example.khaldir.rptool;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class WeaponsActivity extends ReactorClass
        implements NavigationView.OnNavigationItemSelectedListener {


    List<RelativeLayout> weaponRow;
    List<TextView> weaponNames;
    List<TextView> weaponDescriptions;
    List<TextView> weaponPowerUses;
    List<Switch> weaponSwitches;
    ProgressBar availablePower;
    TextView availPower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weapons);
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
        wifiObject.currentLocation = 3;

        availablePower = (ProgressBar) findViewById(R.id.weaponPowerBar);

        weaponRow = new ArrayList<RelativeLayout>();
        weaponRow.add((RelativeLayout) findViewById(R.id.weapon1));
        weaponRow.add((RelativeLayout) findViewById(R.id.weapon2));
        weaponRow.add((RelativeLayout) findViewById(R.id.weapon3));
        weaponRow.add((RelativeLayout) findViewById(R.id.weapon4));
        weaponRow.add((RelativeLayout) findViewById(R.id.weapon5));
        weaponRow.add((RelativeLayout) findViewById(R.id.weapon6));

        weaponNames = new ArrayList<TextView>();
        weaponNames.add((TextView) findViewById(R.id.name1));
        weaponNames.add((TextView) findViewById(R.id.name2));
        weaponNames.add((TextView) findViewById(R.id.name3));
        weaponNames.add((TextView) findViewById(R.id.name4));
        weaponNames.add((TextView) findViewById(R.id.name5));
        weaponNames.add((TextView) findViewById(R.id.name6));

        weaponDescriptions = new ArrayList<TextView>();
        weaponDescriptions.add((TextView) findViewById(R.id.desc1));
        weaponDescriptions.add((TextView) findViewById(R.id.desc2));
        weaponDescriptions.add((TextView) findViewById(R.id.desc3));
        weaponDescriptions.add((TextView) findViewById(R.id.desc4));
        weaponDescriptions.add((TextView) findViewById(R.id.desc5));
        weaponDescriptions.add((TextView) findViewById(R.id.desc6));

        weaponPowerUses = new ArrayList<TextView>();
        weaponPowerUses.add((TextView) findViewById(R.id.power1));
        weaponPowerUses.add((TextView) findViewById(R.id.power2));
        weaponPowerUses.add((TextView) findViewById(R.id.power3));
        weaponPowerUses.add((TextView) findViewById(R.id.power4));
        weaponPowerUses.add((TextView) findViewById(R.id.power5));
        weaponPowerUses.add((TextView) findViewById(R.id.power6));

        weaponSwitches = new ArrayList<Switch>();
        weaponSwitches.add((Switch) findViewById(R.id.is1Active));
        weaponSwitches.add((Switch) findViewById(R.id.is2Active));
        weaponSwitches.add((Switch) findViewById(R.id.is3Active));
        weaponSwitches.add((Switch) findViewById(R.id.is4Active));
        weaponSwitches.add((Switch) findViewById(R.id.is5Active));
        weaponSwitches.add((Switch) findViewById(R.id.is6Active));

        reactToChanges();
    }

    @Override
    public void reactToChanges()
    {
        availPower = (TextView)findViewById(R.id.availWeaponPower);
        availPower.setText("Available Power: "+String.valueOf(wifiObject.WeaponEnergyIn));
        availablePower.setMax(wifiObject.EnginePower);
        availablePower.setProgress(wifiObject.WeaponEnergyIn);
        // For each weapon

        for (int i = 0; i < 6; i++) {
            // Mark all as empty
            weaponRow.get(i).setVisibility(View.INVISIBLE);

            // If the slot isn't empty:
            if (wifiObject.weaponInfo.size() > i)
                if (!wifiObject.weaponInfo.get(i).equals(null))
                {
                    weaponRow.get(i).setVisibility(View.VISIBLE);
                    weaponNames.get(i).setText(wifiObject.weaponInfo.get(i).name);
                    weaponDescriptions.get(i).setText(wifiObject.weaponInfo.get(i).description);
                    weaponPowerUses.get(i).setText(Integer.toString(wifiObject.weaponInfo.get(i).powerUse));
                    weaponSwitches.get(i).setChecked(wifiObject.weaponInfo.get(i).isActive);
                }




        }

    }

    protected void updateWeapons(View sender)
    {


        for (int i = 0; i< 6; i++) {

            if (weaponRow.get(i).getVisibility() == View.VISIBLE)
            {
                JSONObject weaponValues = new JSONObject();
                Utilities.addtoJSON(weaponValues,String.valueOf(i),"weaponID");
                Utilities.addtoJSON(weaponValues,weaponNames.get(i).getText().toString(),"weaponName");
                Utilities.addtoJSON(weaponValues,weaponDescriptions.get(i).getText().toString(),"weaponDesc");
                Utilities.addtoJSON(weaponValues,weaponPowerUses.get(i).getText().toString(),"weaponPower");
                Utilities.addtoJSON(weaponValues,String.valueOf(weaponSwitches.get(i).isChecked()),"weaponOn");
                wifiObject.sendValue(weaponValues.toString(),wifiObject.gmIP);
            }

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
        getMenuInflater().inflate(R.menu.weapons, menu);
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

    private void clearWeapons()
    {
        wifiObject.weaponIP = null;
        clearLocation("weapons");
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
            alreadyThere = true;
        } else if (id == R.id.nav_sensors) {
            gmIntent = new Intent(this,ScannerActivity.class);
        } else if (id == R.id.nav_engines) {
            gmIntent = new Intent(this,EngineActivity.class);
        } else if (id == R.id.nav_connect) {
            gmIntent = new Intent(this,PlayerActivity.class);
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

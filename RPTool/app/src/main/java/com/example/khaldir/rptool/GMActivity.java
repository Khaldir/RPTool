package com.example.khaldir.rptool;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;

public class GMActivity extends ReactorClass
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        wifiObject = WiFiDirect.getInstance(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiObject.sendValue("enableAll","1",wifiObject.gmIP);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    protected void modifyDodge(View sender)
    {
        EditText dodgeMod = (EditText)findViewById(R.id.dodgeMod);
        String first = dodgeMod.getText().toString().substring(0,1);
        if (first == "+")
        {
            wifiObject.sendValue("dodge",String.valueOf(wifiObject.dodge + Integer.getInteger(dodgeMod.getText().toString())),wifiObject.gmIP);
        }
        if (first == "-")
        {
            wifiObject.sendValue("dodge",String.valueOf(wifiObject.dodge + Integer.getInteger(dodgeMod.getText().toString())),wifiObject.gmIP);
        }
        else
            wifiObject.sendValue("dodge",String.valueOf(dodgeMod.getText().toString()),wifiObject.gmIP);
    }

    protected void modifyEngines(View sender)
    {
        EditText engineMod = (EditText)findViewById(R.id.enginePowerBox);
        String first = engineMod.getText().toString().substring(engineMod.getText().length()-1);
        if (first == "+")
        {
            wifiObject.sendValue("maxEnginePower",String.valueOf(wifiObject.EnginePower + Integer.getInteger(engineMod.getText().toString())),wifiObject.gmIP);
        }
        if (first == "-")
        {
            wifiObject.sendValue("maxEnginePower",String.valueOf(wifiObject.EnginePower - Integer.getInteger(engineMod.getText().toString())),wifiObject.gmIP);
        }
        else
            wifiObject.sendValue("maxEnginePower",String.valueOf(engineMod.getText().toString()),wifiObject.gmIP);
    }

    protected void modifySpeed(View sender)
    {
        EditText speedMod = (EditText)findViewById(R.id.speedMod);
        String first = speedMod.getText().toString().substring(0,1);
        if (first == "+")
        {
            wifiObject.sendValue("speed",String.valueOf(wifiObject.dodge + Integer.getInteger(speedMod.getText().toString())),wifiObject.gmIP);
        }
        if (first == "-")
        {
            wifiObject.sendValue("speed",String.valueOf(wifiObject.dodge + Integer.getInteger(speedMod.getText().toString())),wifiObject.gmIP);
        }
        else
            wifiObject.sendValue("speed",String.valueOf(speedMod.getText().toString()),wifiObject.gmIP);
    }

    protected void modifyShields(View sender)
    {
        EditText shieldMod = (EditText)findViewById(R.id.shieldMod);
        Spinner shieldFacing = (Spinner)findViewById(R.id.shieldFacing);
        String key = "";
        Integer currentVal = 0;
        switch (shieldFacing.getSelectedItem().toString())
        {
            case "Front": key = "frontShieldHP"; currentVal = wifiObject.frontShieldHP; break;
            case "Left": key = "leftShieldHP"; currentVal = wifiObject.leftShieldHP; break;
            case "Right": key = "rightShieldHP"; currentVal = wifiObject.rightShieldHP; break;
            case "Rear": key = "rearShieldHP"; currentVal = wifiObject.rearShieldHP; break;
        }
        String input = shieldMod.getText().toString();
        if (input.substring(0,1)=="-"||input.substring(0,1)=="+")
            currentVal = currentVal + Integer.parseInt(input);
        else
            currentVal = Integer.parseInt(input);
        wifiObject.sendValue(key,currentVal.toString(),wifiObject.gmIP);
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
        getMenuInflater().inflate(R.menu.gm, menu);
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
            alreadyThere = true;
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

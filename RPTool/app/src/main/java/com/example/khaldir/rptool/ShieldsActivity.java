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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;

public class ShieldsActivity extends ReactorClass
        implements NavigationView.OnNavigationItemSelectedListener {


    // Bars to input shield strength
    SeekBar frontBar;
    SeekBar leftBar;
    SeekBar rightBar;
    SeekBar backBar;

    ProgressBar availablePowerBar;
    // Bars to show the shield strength once submitted
    ProgressBar front;
    ProgressBar left;
    ProgressBar right;
    ProgressBar back;

    Button goButton;

    int maxEngineOutput;
    int availablePower;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shields);
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
        frontBar = (SeekBar) findViewById(R.id.frontShields);
        leftBar = (SeekBar) findViewById(R.id.leftShields);
        rightBar = (SeekBar) findViewById(R.id.rightShields);
        backBar = (SeekBar) findViewById(R.id.backShields);

        availablePowerBar = (ProgressBar) findViewById(R.id.maxShieldEnergy);
        front = (ProgressBar) findViewById(R.id.frontShieldProg);
        left = (ProgressBar) findViewById(R.id.leftShieldProg);
        right = (ProgressBar) findViewById(R.id.rightShieldProg);
        back = (ProgressBar) findViewById(R.id.backShieldProg);

        maxEngineOutput = wifiObject.EnginePower;
        availablePowerBar.setMax(maxEngineOutput);
        availablePower = wifiObject.ShieldEnergyIn;
        availablePowerBar.setProgress(availablePower);

        goButton = (Button) findViewById(R.id.updateShields);

        wifiObject.currentLocation = 2;
        sendLocation("shields");
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
        availablePowerBar.setMax(maxEngineOutput);
        availablePower = wifiObject.ShieldEnergyIn;
        availablePowerBar.setProgress(availablePower);
        left.setProgress(wifiObject.leftShieldHP);
        leftBar.setProgress(wifiObject.leftShields);
        right.setProgress(wifiObject.rightShieldHP);
        rightBar.setProgress(wifiObject.rightShields);
        front.setProgress(wifiObject.frontShieldHP);
        frontBar.setProgress(wifiObject.frontShields);
        back.setProgress(wifiObject.rearShieldHP);
        backBar.setProgress(wifiObject.rearShields);
    }

    protected void updateShields(View sender)
    {
        if (frontBar.getProgress() + leftBar.getProgress() + rightBar.getProgress() + backBar.getProgress() <= availablePower)
        {
            if(wifiObject.isShieldsEditable)
            {
                frontBar.setVisibility(View.INVISIBLE);
                leftBar.setVisibility(View.INVISIBLE);
                rightBar.setVisibility(View.INVISIBLE);
                backBar.setVisibility(View.INVISIBLE);
                front.setVisibility(View.VISIBLE);
                front.setMax(frontBar.getProgress());
                left.setVisibility(View.VISIBLE);
                left.setMax(leftBar.getProgress());
                right.setVisibility(View.VISIBLE);
                right.setMax(rightBar.getProgress());
                back.setVisibility(View.VISIBLE);
                back.setMax(backBar.getProgress());
                wifiObject.sendValue("frontShields",String.valueOf(frontBar.getProgress()),wifiObject.gmIP);
                wifiObject.sendValue("leftShields",String.valueOf(leftBar.getProgress()),wifiObject.gmIP);
                wifiObject.sendValue("rightShields",String.valueOf(rightBar.getProgress()),wifiObject.gmIP);
                wifiObject.sendValue("rearShields",String.valueOf(backBar.getProgress()),wifiObject.gmIP);
                goButton.setVisibility(View.INVISIBLE);
                reactToChanges();
            }
        }
        else
            Utilities.newSnackbar(this,"Not Enough Power!");
        if (!wifiObject.isShieldsEditable)
        {
            frontBar.setVisibility(View.VISIBLE);
            leftBar.setVisibility(View.VISIBLE);
            rightBar.setVisibility(View.VISIBLE);
            backBar.setVisibility(View.VISIBLE);
            front.setVisibility(View.INVISIBLE);
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.INVISIBLE);
            back.setVisibility(View.INVISIBLE);
            goButton.setVisibility(View.VISIBLE);
            reactToChanges();
        }
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
        getMenuInflater().inflate(R.menu.shields, menu);
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

    private void clearShields()
    {
        wifiObject.shieldIP = null;
        clearLocation("shields");
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
            alreadyThere = true;
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

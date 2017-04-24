package com.example.khaldir.rptool;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.json.JSONObject;

public class GMActivity extends ReactorClass
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView shipSpeed, dodgePool,
            frontShieldDisplay,leftShieldDisplay,rightShieldDisplay,rearShieldDisplay,
            activeWeapons, activeScans,
            pilotPowerLabel,shieldPowerLabel,weaponPowerLabel,sensorPowerLabel,enginePowerLabel;
    Spinner weaponsList;

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

        Spinner shieldLocations = (Spinner)findViewById(R.id.shieldFacing);
        shieldLocations.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Front","Left","Right","Rear"}));

        shipSpeed = (TextView)findViewById(R.id.shipSpeed);
        dodgePool = (TextView)findViewById(R.id.dodgePool);
        frontShieldDisplay = (TextView)findViewById(R.id.frontShieldDisplay);
        leftShieldDisplay = (TextView)findViewById(R.id.leftShieldDisplay);
        rightShieldDisplay = (TextView)findViewById(R.id.rightShieldDisplay);
        rearShieldDisplay = (TextView)findViewById(R.id.rearShieldDisplay);
        activeWeapons = (TextView)findViewById(R.id.activeWeapons);
        activeScans = (TextView)findViewById(R.id.activeScans);
        pilotPowerLabel = (TextView)findViewById(R.id.pilotPowerLabel);
        shieldPowerLabel = (TextView)findViewById(R.id.shieldPowerLabel);
        weaponPowerLabel = (TextView)findViewById(R.id.weaponPowerLabel);
        sensorPowerLabel = (TextView)findViewById(R.id.sensorPowerLabel);
        enginePowerLabel = (TextView)findViewById(R.id.enginePowerLabel);
        weaponsList = (Spinner)findViewById(R.id.weaponsList);
        reactToChanges();
    }

    @Override
    public void reactToChanges() {
        super.reactToChanges();
        shipSpeed.setText("Ship Speed: "+wifiObject.speed);
        dodgePool.setText("Dodge Pool: "+wifiObject.dodge);
        frontShieldDisplay.setText("Front Shields: "+wifiObject.frontShieldHP+"/"+wifiObject.frontShields);
        leftShieldDisplay.setText("Left Shields: "+wifiObject.leftShieldHP+"/"+wifiObject.leftShields);
        rightShieldDisplay.setText("Right Shields: "+wifiObject.rightShieldHP+"/"+wifiObject.rightShields);
        rearShieldDisplay.setText("Rear Shields: "+wifiObject.rearShieldHP+"/"+wifiObject.rearShields);
        String weaponString = "Active Weapons: ";
        for (WeaponItem weapon:wifiObject.weaponInfo)
        {
            weaponString = weaponString + weapon.name + System.lineSeparator();
        }
        activeWeapons.setText(weaponString);
        String scanString = "Scans: ";
        for (ScanItem scan:wifiObject.scanData)
        {
            scanString = scanString + scan.name + System.lineSeparator();
        }
        activeScans.setText(scanString);
        pilotPowerLabel.setText("Pilot Power: "+wifiObject.PilotEnergyIn);
        shieldPowerLabel.setText("Shields Power: "+wifiObject.ShieldEnergyIn);
        weaponPowerLabel.setText("Weapons Power: "+wifiObject.WeaponEnergyIn);
        sensorPowerLabel.setText("Sensor Power: "+wifiObject.SensorEnergyIn);
        enginePowerLabel.setText("Set Engine Power: (Currently "+wifiObject.EnginePower+")");
        weaponsList.setAdapter(new ArrayAdapter<WeaponItem>(this,android.R.layout.simple_spinner_item, wifiObject.weaponInfo));

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

    private String addWeaponName, addWeaponDesc, addWeaponPower;

    protected void addWeapon(View sender)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Weapon Name");

        //Set up input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        //Set up buttons
        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addWeaponName = input.getText().toString();
                addWeaponDesc();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    protected void addWeaponDesc()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Weapon Description");

        //Set up input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        //Set up buttons
        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addWeaponDesc = input.getText().toString();
                addWeaponPower();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    protected void addWeaponPower()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Weapon Power");

        //Set up input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        //Set up buttons
        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addWeaponPower = input.getText().toString();
                JSONObject newWeapon = Utilities.addtoJSON(new JSONObject(),"New Weapon","weaponDetail");
                newWeapon = Utilities.addtoJSON(newWeapon,addWeaponName,"weaponName");
                newWeapon = Utilities.addtoJSON(newWeapon,addWeaponDesc,"weaponDesc");
                newWeapon = Utilities.addtoJSON(newWeapon,addWeaponPower,"weaponPower");
                newWeapon = Utilities.addtoJSON(newWeapon,"false","weaponOn");
                newWeapon = Utilities.addtoJSON(newWeapon, Integer.toString(wifiObject.weaponInfo.size()+1),"weaponID");
                wifiObject.sendValue(newWeapon.toString(),wifiObject.gmIP);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    protected void removeWeapon(View sender)
    {
        int index = weaponsList.getSelectedItemPosition();
        JSONObject newWeapon = Utilities.addtoJSON(new JSONObject(),"remove Weapon","weaponDetail");
        newWeapon = Utilities.addtoJSON(newWeapon,"remove","nullify");
        newWeapon = Utilities.addtoJSON(newWeapon,Integer.toString(index),"weaponID");
        wifiObject.sendValue(newWeapon.toString(),wifiObject.gmIP);
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

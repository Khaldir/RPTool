package com.example.khaldir.rptool;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by JakeT12 on 14/02/2017.
 */

public class ReactorClass extends AppCompatActivity {

    WiFiDirect wifiObject;


    public void reactToChanges()
    {

    }

    public void sendLocation(String location)
    {
        wifiObject.sendValue(location,Utilities.getDottedDecimalIP(Utilities.getLocalIPAddress()),wifiObject.gmIP);
    }

    public void clearLocation(String location)
    {
        wifiObject.sendValue(location,"",wifiObject.gmIP);
    }
}

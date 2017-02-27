package com.example.khaldir.rptool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeaponItem {
    public String id;
    public String name;
    public String description;
    public int powerUse;
    public boolean isActive;

    public WeaponItem(String id, String content, String description, String powerUse, boolean isON) {
        this.id = id;
        this.name = content;
        this.description = description;
        this.powerUse = Integer.getInteger(powerUse);
        this.isActive = isON;
    }

    public void changeActive(boolean newState)
    {
        this.isActive = newState;
    }

    @Override
    public String toString() {
        return name;
    }
}

package com.example.khaldir.rptool;

public class ScanItem {
    public String id;
    public String name;
    public String description;
    public String type;

    public ScanItem(String id, String description, String type) {
        this.id = id;
        this.description = description;
        this.type = type;
    }


    @Override
    public String toString() {
        return name;
    }
}

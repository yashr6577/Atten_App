package com.suyogbauskar.atten.pojos;

public class Subject {
    private String name, shortName;

    public Subject(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }
}

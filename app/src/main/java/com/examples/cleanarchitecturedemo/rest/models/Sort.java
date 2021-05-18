package com.examples.cleanarchitecturedemo.rest.models;

public enum Sort {
    Created("created"),
    Updated("updated"),
    Pushed("pushed"),
    FullName("full_name");

    String value;

    Sort(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

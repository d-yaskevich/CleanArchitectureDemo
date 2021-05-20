package com.examples.cleanarchitecturedemo.rest.models;

public enum Direction {
    Ask("ask"),
    Desk("desc");

    String value;

    Direction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

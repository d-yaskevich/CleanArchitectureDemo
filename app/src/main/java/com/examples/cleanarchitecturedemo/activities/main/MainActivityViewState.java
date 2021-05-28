package com.examples.cleanarchitecturedemo.activities.main;

import com.examples.cleanarchitecturedemo.rest.github.models.Repo;

import java.util.ArrayList;

public class MainActivityViewState {
    public boolean reset = true;
    public ArrayList<Repo> repos = new ArrayList<>();
    public String errorMessage = "";
}

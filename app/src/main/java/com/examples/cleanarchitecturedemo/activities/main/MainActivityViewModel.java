package com.examples.cleanarchitecturedemo.activities.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {

    private MutableLiveData<MainActivityViewState> state = new MutableLiveData<>();
}

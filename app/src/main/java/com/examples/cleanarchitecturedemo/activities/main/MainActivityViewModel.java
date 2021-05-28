package com.examples.cleanarchitecturedemo.activities.main;

import android.util.Log;
import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.examples.cleanarchitecturedemo.R;
import com.examples.cleanarchitecturedemo.rest.github.models.Sort;
import com.examples.cleanarchitecturedemo.storage.Repository;
import com.google.android.material.snackbar.Snackbar;

import rx.Subscription;

public class MainActivityViewModel extends ViewModel {

    private static final String TAG = MainActivityViewModel.class.getSimpleName();

    public MutableLiveData<MainActivityViewState> state = new MutableLiveData<>();
    private MainActivityViewState _state = new MainActivityViewState();

    public MutableLiveData<Boolean> firstLoadingState = new MutableLiveData<>();
    public MutableLiveData<Boolean> loadingState = new MutableLiveData<>();

    private final Repository repository = Repository.getInstance();

    private int page = 0;
    private static final int PER_PAGE = 5;

    public void loadRepos(String user, Sort selectedSort, boolean refresh) {
        String sort = selectedSort.getValue();

        if (refresh) page = 1;
        else page++;

        getRepos(user, sort);
    }

    private Subscription subscription;

    private void getRepos(String user,
                          String sort) {
        Log.d(TAG, "getRepos(" + user + ", " + sort + ", " + page + ", " + PER_PAGE + ")");
        subscription = repository.getRepos(user, sort, page, PER_PAGE)
                .doOnSubscribe(() -> {
                    if (page == 1) {
                        firstLoadingState.postValue(true);
                    } else {
                        loadingState.postValue(true);
                    }
                })
                .doAfterTerminate(() -> {
                    if (page == 1) {
                        firstLoadingState.postValue(false);
                    } else {
                        loadingState.postValue(false);
                    }
                })
                .subscribe(repos -> {
                    _state = new MainActivityViewState();
                    _state.repos = repos;
                    _state.reset = page == 1;

                    state.postValue(_state);
                }, error -> {
                    String message = error.getMessage();
                    if (message == null) message = "Unknown error";

                    _state = new MainActivityViewState();
                    _state.errorMessage = message;

                    state.postValue(_state);
                });
    }

    public void cancelRepos(){
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}

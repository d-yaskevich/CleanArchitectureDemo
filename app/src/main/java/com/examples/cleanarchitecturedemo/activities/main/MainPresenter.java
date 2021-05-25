package com.examples.cleanarchitecturedemo.activities.main;

import android.util.Log;

import com.examples.cleanarchitecturedemo.rest.github.models.Sort;
import com.examples.cleanarchitecturedemo.storage.Repository;

import java.lang.ref.WeakReference;

import rx.Subscription;

public class MainPresenter implements MainContract.Presenter {

    private static final String TAG = MainPresenter.class.getSimpleName();

    private WeakReference<MainContract.View> view = null;
    private final MainContract.Repository repository = Repository.getInstance();

    private Sort selectedSort = Sort.Created;

    private int page = 0;
    private static final int PER_PAGE = 5;

    @Override
    public void attachView(MainContract.View view) {
        if (view != null) {
            this.view = new WeakReference<>(view);
        }
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void viewIsReady() {

    }

    @Override
    public Sort getSelectedSort() {
        return selectedSort;
    }

    @Override
    public void onSortSelected(Sort sort) {
        selectedSort = sort;
        loadRepos(true);
    }

    @Override
    public void onLoadMore() {
        loadRepos(false);
    }

    @Override
    public void onRefresh() {
        loadRepos(true);
    }

    @Override
    public void onActionSearch() {
        loadRepos(true);
    }

    private void loadRepos(boolean refresh) {
        String user = "";
        String sort = selectedSort.getValue();
        if (view != null) {
            user = view.get().getUserName();
        }

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
                    if (view != null) {
                        if (page == 1) {
                            view.get().startRefreshing();
                        } else {
                            view.get().showProgress();
                        }
                    }
                })
                .doAfterTerminate(() -> {
                    if (view != null) {
                        if (page == 1) {
                            view.get().stopRefreshing();
                        } else {
                            view.get().hideProgress();
                        }
                    }
                })
                .subscribe(repos -> {
                    if (view != null) {
                        view.get().updateRepos(repos, page == 1);
                    }
                }, error -> {
                    if (view != null) {
                        String message = error.getMessage();
                        if (message == null) message = "Unknown error";
                        view.get().showReloadMessage(message, v -> {
                            loadRepos(true);
                        });
                    }
                });
    }

    @Override
    public void onPause() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}

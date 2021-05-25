package com.examples.cleanarchitecturedemo.activities.main;

import com.examples.cleanarchitecturedemo.rest.github.models.Repo;
import com.examples.cleanarchitecturedemo.rest.github.models.Sort;

import java.util.ArrayList;

import rx.Single;

public interface MainContract {
    interface View {
        String getUserName();

        void updateRepos(ArrayList<Repo> repos, boolean reset);

        void startRefreshing();
        void stopRefreshing();

        void showProgress();
        void hideProgress();

        void showReloadMessage(String message, android.view.View.OnClickListener listener);
    }

    interface Presenter {
        void attachView(MainContract.View view);
        void detachView();
        void viewIsReady();

        Sort getSelectedSort();
        void onSortSelected(Sort sort);

        void onLoadMore();
        void onRefresh();
        void onActionSearch();

        void onPause();
    }

    interface Repository {
        Single<ArrayList<Repo>> getRepos(String user, String sort, int page, int perPage);
    }
}

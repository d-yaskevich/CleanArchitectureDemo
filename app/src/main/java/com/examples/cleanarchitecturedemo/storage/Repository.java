package com.examples.cleanarchitecturedemo.storage;

import com.examples.cleanarchitecturedemo.rest.API;
import com.examples.cleanarchitecturedemo.rest.github.GithubService;
import com.examples.cleanarchitecturedemo.rest.github.models.Repo;

import java.util.ArrayList;

import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class Repository {

    private static final Repository INSTANCE = new Repository();

    public static Repository getInstance() {
        return INSTANCE;
    }

    private final GithubService githubApi = API.github();
    // can add any another data sources: database, files, etc

    public Single<ArrayList<Repo>> getRepos(String user, String sort, int page, int perPage) {
        // choose data source
        return githubApi.listRepos(user, sort, page, perPage).compose(new AsyncTransformer<>());
    }

    public static class AsyncTransformer<T> implements Single.Transformer<T, T> {
        @Override
        public Single<T> call(Single<T> single) {
            return single.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }
}

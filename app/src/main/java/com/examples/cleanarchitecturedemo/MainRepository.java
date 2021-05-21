package com.examples.cleanarchitecturedemo;

import com.examples.cleanarchitecturedemo.rest.API;
import com.examples.cleanarchitecturedemo.rest.GithubService;
import com.examples.cleanarchitecturedemo.rest.models.Repo;

import java.util.ArrayList;

import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainRepository {

    private static final MainRepository INSTANCE = new MainRepository();

    public static MainRepository getInstance() {
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

package com.examples.cleanarchitecturedemo.rest;

import com.examples.cleanarchitecturedemo.rest.models.Repo;

import java.util.ArrayList;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Single;

public interface GithubService {

    @Headers({
            "Accept: " + GithubConfig.GITHUB_ACCEPT,
            "Authorization: " + GithubConfig.GITHUB_AUTH_TOKEN
    })
    @GET(GithubConfig.GITHUB_API_REPOS)
    Single<ArrayList<Repo>> listRepos(@Path("user") String user,
                                      @Query("sort") String sort,
                                      @Query("page") int page,
                                      @Query("per_page") int perPage,
                                      @Query("direction") String direction);

}

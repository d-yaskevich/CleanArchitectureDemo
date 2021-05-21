package com.examples.cleanarchitecturedemo.activities.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.examples.cleanarchitecturedemo.R;
import com.examples.cleanarchitecturedemo.adapters.ReposAdapter;
import com.examples.cleanarchitecturedemo.rest.github.models.Sort;
import com.examples.cleanarchitecturedemo.storage.Repository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

import rx.Subscription;

public class MainActivity extends AppCompatActivity
        implements ChipGroup.OnCheckedChangeListener,
        SwipeRefreshLayout.OnRefreshListener,
        ReposAdapter.OnClickListener,
        MainContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final Repository repository = Repository.getInstance();

    private EditText etUserName;

    private ChipGroup chipGroup;
    private Sort selectedSort = Sort.Created;
    private final HashMap<Integer, Sort> sortHashMap = new HashMap<>();

    private int page = 0;
    private static final int PER_PAGE = 5;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ReposAdapter adapter;

    private ViewGroup vgProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUserName = findViewById(R.id.et_user_name);
        etUserName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                loadRepos(true);
                v.clearFocus();
                hideKeyboard();
                return true;
            }
            return false;
        });

        chipGroup = findViewById(R.id.cg_sort);
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            int chipId = chipGroup.getChildAt(i).getId();
            Sort sort = getSortByChipId(chipId);
            sortHashMap.put(chipId, sort);
        }
        updateSelectedChip();
        chipGroup.setOnCheckedChangeListener(this);

        swipeRefreshLayout = findViewById(R.id.srv_repos);
        swipeRefreshLayout.setOnRefreshListener(this);

        adapter = new ReposAdapter(this, this);
        RecyclerView rvReps = findViewById(R.id.rv_repos);
        rvReps.setLayoutManager(new LinearLayoutManager(this));
        rvReps.setAdapter(adapter);

        vgProgress = findViewById(R.id.vg_progress);
    }

    private void hideKeyboard() {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
    }

    @SuppressLint("NonConstantResourceId")
    private Sort getSortByChipId(int chipId) {
        switch (chipId) {
            case R.id.chip_updated:
                return Sort.Updated;
            case R.id.chip_pushed:
                return Sort.Pushed;
            case R.id.chip_full_name:
                return Sort.FullName;
            default:
                return Sort.Created;
        }
    }

    private void updateSelectedChip() {
        for (Map.Entry<Integer, Sort> entry : sortHashMap.entrySet()) {
            int chipId = entry.getKey();
            Sort sort = entry.getValue();

            if (selectedSort.equals(sort)) {
                Chip chip = chipGroup.findViewById(chipId);
                chip.setChecked(true);
                return;
            }
        }
    }

    @Override
    public void onCheckedChanged(ChipGroup group, int checkedId) {
        Sort sort = sortHashMap.get(checkedId);
        if (sort == null) return;
        selectedSort = sort;
        loadRepos(true);
    }

    @Override
    public void onRefresh() {
        loadRepos(true);
    }

    @Override
    public void onLoadMoreClick() {
        loadRepos(false);
    }

    private void loadRepos(boolean refresh) {
        String user = etUserName.getText().toString();
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
                        swipeRefreshLayout.setRefreshing(true);
                    } else {
                        vgProgress.setVisibility(View.VISIBLE);
                    }
                })
                .doAfterTerminate(() -> {
                    if (page == 1) {
                        swipeRefreshLayout.setRefreshing(false);
                    } else {
                        vgProgress.setVisibility(View.GONE);
                    }
                })
                .subscribe(repos -> {
                    if (page == 1) {
                        adapter.resetRepos(repos);
                    } else {
                        adapter.addRepos(repos);
                    }
                }, error -> {
                    String message = error.getMessage();
                    if (message == null) message = "Unknown error";
                    Snackbar.make(etUserName, message, Snackbar.LENGTH_LONG)
                            .setAction(R.string.reload, v -> loadRepos(true)).show();
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
package com.examples.cleanarchitecturedemo.activities.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
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
import com.examples.cleanarchitecturedemo.rest.github.models.Repo;
import com.examples.cleanarchitecturedemo.rest.github.models.Sort;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements ChipGroup.OnCheckedChangeListener,
        SwipeRefreshLayout.OnRefreshListener,
        ReposAdapter.OnClickListener,
        MainContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainContract.Presenter presenter;

    private EditText etUserName;

    private ChipGroup chipGroup;
    private final HashMap<Integer, Sort> sortHashMap = new HashMap<>();

    private SwipeRefreshLayout swipeRefreshLayout;
    private ReposAdapter adapter;

    private ViewGroup vgProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainPresenter();
        presenter.attachView(this);

        etUserName = findViewById(R.id.et_user_name);
        etUserName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                presenter.onActionSearch();
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
        presenter.viewIsReady();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
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

            Sort selectedSort = presenter.getSelectedSort();

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
        if (sort == null) {
            updateSelectedChip();
            return;
        }
        presenter.onSortSelected(sort);
    }

    @Override
    public void onRefresh() {
        presenter.onRefresh();
    }

    @Override
    public void onLoadMoreClick() {
        presenter.onLoadMore();
    }

    @Override
    public String getUserName() {
        return etUserName.getText().toString();
    }

    @Override
    public void updateRepos(ArrayList<Repo> repos, boolean reset) {
        if (reset) adapter.resetRepos(repos);
        else adapter.addRepos(repos);
    }

    @Override
    public void showReloadMessage(String message, View.OnClickListener listener) {
        Snackbar.make(etUserName, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.reload, listener).show();
    }

    @Override
    public void startRefreshing() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void stopRefreshing() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showProgress() {
        vgProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        vgProgress.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }
}
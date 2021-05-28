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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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
        ReposAdapter.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainActivityViewModel viewModel;

    private EditText etUserName;

    private ChipGroup chipGroup;
    private Sort selectedSort = Sort.Created;
    private final HashMap<Integer, Sort> sortHashMap = new HashMap<>();

    private SwipeRefreshLayout swipeRefreshLayout;
    private ReposAdapter adapter;

    private ViewGroup vgProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        etUserName = findViewById(R.id.et_user_name);
        etUserName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.loadRepos(getUserName(), selectedSort, true);
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

        viewModel.firstLoadingState.observe(this, isFirstLoading -> {
            swipeRefreshLayout.setRefreshing(isFirstLoading);
        });
        viewModel.loadingState.observe(this, isLoading -> {
            if (isLoading) vgProgress.setVisibility(View.VISIBLE);
            else vgProgress.setVisibility(View.GONE);
        });
        viewModel.state.observe(this, viewState -> {
            if (!viewState.errorMessage.isEmpty()) {
                Snackbar.make(etUserName, viewState.errorMessage, Snackbar.LENGTH_LONG)
                        .setAction(R.string.reload, v -> {
                            viewModel.loadRepos(getUserName(), selectedSort, true);
                        }).show();
                return;
            }

            if (viewState.reset) {
                adapter.resetRepos(viewState.repos);
            } else {
                adapter.addRepos(viewState.repos);
            }
        });
    }

    private String getUserName() {
        return etUserName.getText().toString();
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
        viewModel.loadRepos(getUserName(), selectedSort, true);
    }

    @Override
    public void onRefresh() {
        viewModel.loadRepos(getUserName(), selectedSort, true);
    }

    @Override
    public void onLoadMoreClick() {
        viewModel.loadRepos(getUserName(), selectedSort, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.cancelRepos();
    }
}
package com.examples.cleanarchitecturedemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.examples.cleanarchitecturedemo.R;
import com.examples.cleanarchitecturedemo.rest.models.Repo;

import java.util.ArrayList;

public class ReposAdapter extends RecyclerView.Adapter<ReposAdapter.VH> {

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_LOAD_MORE = 2;

    private final LayoutInflater inflater;
    private final ArrayList<Repo> repos = new ArrayList<>();

    private OnClickListener listener;

    public interface OnClickListener {
        void onLoadMoreClick();
    }

    public ReposAdapter(Context context, OnClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return position == repos.size() ? TYPE_LOAD_MORE : TYPE_ITEM;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOAD_MORE) {
            View view = inflater.inflate(R.layout.loading_list_item, parent, false);
            return new LoadMoreVH(view);
        }
        View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        return new RepoVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        int type = getItemViewType(position);
        if (type == TYPE_LOAD_MORE) {
            holder.onBind(isLoadMore(), listener);
        } else {
            Repo item = repos.get(position);
            holder.onBind(item, listener);
        }
    }

    @Override
    public int getItemCount() {
        return repos.size() + 1;
    }

    public void resetRepos(ArrayList<Repo> repos) {
        this.repos.clear();
        this.addRepos(repos);
    }

    public void addRepos(ArrayList<Repo> repos) {
        if (repos != null) {
            this.repos.addAll(repos);
        }
        notifyDataSetChanged();
    }

    private boolean isLoadMore() {
        return !this.repos.isEmpty();
    }

    public abstract static class VH extends RecyclerView.ViewHolder {
        public VH(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void onBind(Object item, OnClickListener listener);
    }

    public static class RepoVH extends VH {

        private final TextView tvText;

        public RepoVH(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(android.R.id.text1);
        }

        public void onBind(Object item, OnClickListener listener) {
            Repo repo = (Repo) item;
            tvText.setText(repo.name);
        }
    }

    public static class LoadMoreVH extends VH {

        private final ImageView imageView;

        public LoadMoreVH(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_plus);
        }

        @Override
        public void onBind(Object item, OnClickListener listener) {
            boolean isLoadMore = (boolean) item;
            if (isLoadMore) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.GONE);
            }
            imageView.setOnClickListener(v -> listener.onLoadMoreClick());
        }
    }
}

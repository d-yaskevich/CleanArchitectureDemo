package com.examples.cleanarchitecturedemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.examples.cleanarchitecturedemo.R;
import com.examples.cleanarchitecturedemo.rest.models.Repo;

import java.util.ArrayList;

public class ReposAdapter extends RecyclerView.Adapter<ReposAdapter.VH> {

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_LOAD_MORE = 2;

    private final LayoutInflater inflater;
    private final ArrayList<Repo> repos = new ArrayList<>();

    private OnItemAdapterClickListener listener;

    public interface OnItemAdapterClickListener {

        void onLoadMoreClick();

        void onRepoClicked(Repo r);
    }

    public ReposAdapter(Context context, OnItemAdapterClickListener listener) {
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
        View view = inflater.inflate(R.layout.list_item_repo, parent, false);
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

        public abstract void onBind(Object item, OnItemAdapterClickListener listener);
    }

    public static class RepoVH extends VH {

        private final TextView tvText;
        private final ImageView ivAvatarId;

        public RepoVH(@NonNull View itemView) {
            super(itemView);

            tvText = itemView.findViewById(R.id.tv_name);
            ivAvatarId = itemView.findViewById(R.id.iv_avatar_id);
        }

        public void onBind(Object item, OnItemAdapterClickListener listener) {
            final Repo repo = (Repo) item;
            itemView.setOnClickListener(v -> {
                listener.onRepoClicked(repo);
            });
            tvText.setText(repo.name);
            Glide.with(itemView.getContext()).load(repo.owner.avatarUrl).into(ivAvatarId);

        }


    }

    public static class LoadMoreVH extends VH {

        private final ImageView imageView;

        public LoadMoreVH(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.iv_plus);
        }

        @Override
        public void onBind(Object item, OnItemAdapterClickListener listener) {
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

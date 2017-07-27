package com.example.dab.explorerecyclerview.recycler.adapter.just;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by dab on 2017/7/20.
 */

public class JustAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<IJustBind> mIJustBinds;
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}

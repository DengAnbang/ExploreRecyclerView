package com.example.dab.explorerecyclerview.recycler.adapter.just;

import android.support.v7.widget.RecyclerView;

/**
 * Created by dab on 2017/7/20.
 */

public interface IJustBind {
    int getLayout();

    void bind(RecyclerView.ViewHolder holder);
}

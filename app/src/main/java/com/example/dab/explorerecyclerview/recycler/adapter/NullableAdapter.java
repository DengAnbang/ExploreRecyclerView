package com.example.dab.explorerecyclerview.recycler.adapter;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by DAB on 2017/4/12 22:55.
 */

public abstract class NullableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "NullableAdapter";
    public static boolean DEBUG = true;

    public abstract VH onCreateViewHolderLike(ViewGroup parent, int viewType);

    public abstract void onBindViewHolderLike(VH holder, int position);

    public abstract int getItemCountLike();

    public <T> void onBindViewHolderLike(NoDataHolder holder, int position) {

    }

    public int getItemViewTypeLike(int position) {
        return 0;
    }

    @LayoutRes
    public int getNotDataLayout() {
        return 0;
    }

    @Override
    public final int getItemCount() {
        if (getNotDataLayout() == 0) {
            if (DEBUG) Log.e(TAG, " getNotDataLayout(): 未重写,跳过布局");
            return getItemCountLike();
        }
        int count = getItemCountLike();
        if (count < 0) {
            if (DEBUG) Log.e(TAG, "getItemCount: 小于0");
            return 0;
        }
        return count == 0 ? 1 : count;
    }
    @SuppressWarnings("unchecked")
    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getClass().getName().equals(NoDataHolder.class.getName())) {
            onBindViewHolderLike((NoDataHolder) holder, position);
        } else {
            onBindViewHolderLike((VH) holder, position);
        }

    }

    public NoDataHolder noDataHolder(View view) {
        return new NoDataHolder(view);
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == -1) {
            return noDataHolder(LayoutInflater.from(parent.getContext()).inflate(getNotDataLayout(), parent, false));
        }
        return onCreateViewHolderLike(parent, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        int itemCountLike;
        try {
            itemCountLike = getItemCountLike();
        } catch (Exception e) {
            itemCountLike = 0;
            e.printStackTrace();
        }
        if (position == 0 && getNotDataLayout() != 0 && itemCountLike == 0) return -1;
        int viewType = getItemViewTypeLike(position);
        if (viewType == -1) {
            if (DEBUG) Log.e(TAG, "viewType:返回为-1 ,被当做没有数据处理了");
        }
        return viewType;
    }

    public class NoDataHolder extends RecyclerView.ViewHolder {
        public NoDataHolder(View inflate) {
            super(inflate);
        }
    }
}

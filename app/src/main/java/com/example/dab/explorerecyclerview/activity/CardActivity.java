package com.example.dab.explorerecyclerview.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dab.explorerecyclerview.R;
import com.example.dab.explorerecyclerview.recycler.manager.CardLayoutManager;

public class CardActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_card);
        CardAdapter cardAdapter = new CardAdapter();
        mRecyclerView.setAdapter(cardAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        CardLayoutManager cardLayoutManager = new CardLayoutManager();
        mRecyclerView.setLayoutManager(linearLayoutManager);
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
//        pagerSnapHelper.attachToRecyclerView(mRecyclerView);
    }













    public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
        @Override
        public CardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image,parent,false));
        }

        @Override
        public void onBindViewHolder(CardAdapter.ViewHolder holder, int position) {
                holder.mTextView.setText(position+"");
        }

        @Override
        public int getItemCount() {
            return 100;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView mTextView;
            public ViewHolder(View itemView) {
                super(itemView);
                mTextView = (TextView) itemView.findViewById(R.id.tv_tv);
            }
        }
    }

}

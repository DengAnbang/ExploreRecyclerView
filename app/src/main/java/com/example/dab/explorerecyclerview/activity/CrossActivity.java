package com.example.dab.explorerecyclerview.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.dab.explorerecyclerview.R;
import com.example.dab.explorerecyclerview.recycler.manager.CrossLayoutManager111;

public class CrossActivity extends AppCompatActivity {
    private static final String TAG = "CrossActivity";
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cross);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_cross);



//        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
//        mRecyclerView.setLayoutManager(new CrossLayoutManager());
        mRecyclerView.setLayoutManager(new CrossLayoutManager111());
//        mRecyclerView.setLayoutManager(new FlowLayoutManager());
//        mRecyclerView.setLayoutManager(new NyLayoutManager());
        mRecyclerView.setAdapter(new CrossAdapter());
    }





    public class CrossAdapter extends RecyclerView.Adapter<CrossActivity.CrossAdapter.ViewHolder> {
        @Override
        public CrossActivity.CrossAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new CrossActivity.CrossAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cross,parent,false));
        }

        @Override
        public void onBindViewHolder(CrossActivity.CrossAdapter.ViewHolder holder, int position) {
//            holder.mImageView.setText(position+"");
            Log.e(TAG, "onBindViewHolder: "+position );

        }

        @Override
        public int getItemCount() {
            return 20;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView mImageView;
            public ViewHolder(View itemView) {
                super(itemView);
                mImageView = (ImageView) itemView.findViewById(R.id.iv_image);
            }
        }
    }
}



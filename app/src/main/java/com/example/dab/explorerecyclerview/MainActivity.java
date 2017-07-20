package com.example.dab.explorerecyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.dab.explorerecyclerview.activity.CardActivity;
import com.example.dab.explorerecyclerview.activity.CrossActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void card(View view) {
        startActivity(new Intent(this, CardActivity.class));
    }
    public void cross(View view) {
        startActivity(new Intent(this, CrossActivity.class));
    }
}

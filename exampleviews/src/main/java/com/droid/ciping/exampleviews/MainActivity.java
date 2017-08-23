package com.droid.ciping.exampleviews;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onBubbleViewClick(View view) {
        startActivity(new Intent(this, BubbleViewActivity.class));
    }

    public void onGalleryClick(View view) {
        startActivity(new Intent(this, GalleryActivity.class));
    }

    public void onClick(View view) {
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }
}

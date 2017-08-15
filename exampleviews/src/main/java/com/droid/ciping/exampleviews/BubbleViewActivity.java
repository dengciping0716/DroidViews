package com.droid.ciping.exampleviews;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.droid.ciping.views.BubbleView;

import java.util.ArrayList;

public class BubbleViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble_view);

        BubbleView viewById = (BubbleView) findViewById(R.id.bubbleView);

        ArrayList<Drawable> drawables = new ArrayList<>();
        drawables.add(getResources().getDrawable(R.mipmap.ic_launcher));
        viewById.setDrawableList(drawables)
                .setRiseDuration(2000)
                .setAnimationDelay(200)
                .setItemViewWH(20, 20)
                .startAnimation(30);
    }
}

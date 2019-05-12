package com.example.image_editor;

import android.content.ClipData;
import android.content.ClipDescription;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Segmentation extends Conductor{

    ImageView imageView;
    MainActivity activity;

    ImageView dragable;

    Segmentation(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        this.activity = activity;
        this.imageView = activity.getImageView();
    }

    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.movable_view);

        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        ElevationDragFragment fragment = new ElevationDragFragment();
        transaction.replace(R.id.sample_content_fragment, fragment);
        transaction.commit();
    }
}

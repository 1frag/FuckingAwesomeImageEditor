package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.util.ArrayList;


public class Color_Filters extends Conductor {

    private Bitmap mBitmap;

    private ImageView mImageView;
    private MainActivity mainActivity;

    private ArrayList<String> mNamesFilters = new ArrayList<>();
    private ArrayList<String> mNamesProg = new ArrayList<>();

    Color_Filters(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        mainActivity = activity;
        mImageView = activity.getImageView();
    }

    void touchToolbar() {
        super.touchToolbar();
        prepareToRun(R.layout.filters_menu);
        pickFilters();

        mBitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();

        mImageView.setImageBitmap(mBitmap);
    }

    private void pickFilters() {
        // pick to arrays
        mNamesFilters.add("Original"); // 0
        mNamesProg.add("Original");

        mNamesFilters.add("Movie"); // 1
        mNamesProg.add("Movie");

        mNamesFilters.add("Blur"); // 2
        mNamesProg.add("Blur");

        mNamesFilters.add("B&W"); // 3
        mNamesProg.add("B&W");

        mNamesFilters.add("Blue laguna"); // 4
        mNamesProg.add("Blue laguna");

        mNamesFilters.add("Contrast"); // 5
        mNamesProg.add("Contrast");

        mNamesFilters.add("Sephia"); // 6
        mNamesProg.add("Sephia");

        mNamesFilters.add("Noise"); // 7
        mNamesProg.add("Noise");

        mNamesFilters.add("Green grass"); // 8
        mNamesProg.add("Green grass");

        try {
            initRecyclerView();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void initRecyclerView() throws NoSuchMethodException {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView filters_bar = mainActivity.findViewById(R.id.bar_filters);
        filters_bar.setLayoutManager(layoutManager);
        FiltersAdapter adapter = new FiltersAdapter(mainActivity, mNamesFilters, mNamesProg);
        filters_bar.setAdapter(adapter);
    }


}
package com.example.image_editor;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class Color_Filters extends Conductor {

    private ArrayList<String> mNamesFilters = new ArrayList<>();
    private ArrayList<String> mNamesProg = new ArrayList<>();

    Color_Filters(MainActivity activity) {
        super(activity);
    }

    void touchToolbar() {
        super.touchToolbar();
        prepareToRun(R.layout.filters_menu);
        setHeader(mainActivity.getResources().getString(R.string.color_correction_name));
        pickFilters();
    }

    private void pickFilters() {
        // pick to arrays
        mNamesFilters.add(mainActivity.getResources().getString(R.string.filter_original)); // 0
        mNamesProg.add("Original");

        mNamesFilters.add(mainActivity.getResources().getString(R.string.filters_movie)); // 1
        mNamesProg.add("Movie");

        mNamesFilters.add(mainActivity.getResources().getString(R.string.filters_blur)); // 2
        mNamesProg.add("Blur");

        mNamesFilters.add(mainActivity.getResources().getString(R.string.filers_black_and_white)); // 3
        mNamesProg.add("B&W");

        mNamesFilters.add(mainActivity.getResources().getString(R.string.filter_blue_laguna)); // 4
        mNamesProg.add("Blue laguna");

        mNamesFilters.add(mainActivity.getResources().getString(R.string.filter_contrast)); // 5
        mNamesProg.add("Contrast");

        mNamesFilters.add(mainActivity.getResources().getString(R.string.filter_sephia)); // 6
        mNamesProg.add("Sephia");

        mNamesFilters.add(mainActivity.getResources().getString(R.string.filter_noise)); // 7
        mNamesProg.add("Noise");

        mNamesFilters.add(mainActivity.getResources().getString(R.string.filter_green_grass)); // 8
        mNamesProg.add("Green grass");

        mNamesFilters.add(mainActivity.getResources().getString(R.string.filter_ruby)); // 9
        mNamesProg.add("Ruby");

        mNamesFilters.add(mainActivity.getResources().getString(R.string.filter_vignette)); // 10
        mNamesProg.add("Vignette");

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
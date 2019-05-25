package com.example.image_editor.utils.drag;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.example.image_editor.MainActivity;
import com.example.image_editor.R;

public class DrivingViews {

    private MainActivity mainActivity;
    private FragmentTransaction transaction;
    private ElevationDragFragment fragment;

    public DrivingViews(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void hide() {
        transaction = mainActivity.getSupportFragmentManager().beginTransaction();
        Fragment empty_fragment = new Fragment();
        transaction.replace(R.id.sample_content_fragment, empty_fragment);
        transaction.commit();
    }

    public void show() {
        transaction = mainActivity.getSupportFragmentManager().beginTransaction();
        fragment = new ElevationDragFragment();
        transaction.replace(R.id.sample_content_fragment, fragment);
        transaction.commit();
    }

}

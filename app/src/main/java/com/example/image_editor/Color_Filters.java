package com.example.image_editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import static android.app.PendingIntent.getActivity;

public class Color_Filters extends Conductor {

    private Bitmap bitmap;
    private Bitmap bufferedBitmap;

    private Button selectFilter;

    private String IMAGE_DIRECTORY = "/demonuts";

    private ImageView imageView;
    private MainActivity activity;

    private ArrayList<String> mNamesUser = new ArrayList<>();
    private ArrayList<String> mNamesProg = new ArrayList<>();
    private ArrayList<Integer> mImageUrls = new ArrayList<>();

    Color_Filters(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        this.activity = activity;
        this.imageView = activity.getImageView();

    }

    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.filters_menu);
        pickFilters();

        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        imageView.setImageBitmap(bitmap);
    }

    private void pickFilters() {
        // pick to arrays
        mNamesUser.add("Original"); // 0
        mNamesProg.add("Original");
        mImageUrls.add(R.drawable.icon_a_star);

        mNamesUser.add("Movie"); // 1
        mNamesProg.add("Movie");
        mImageUrls.add(R.drawable.icon_a_star);

        mNamesUser.add("Blur"); // 2
        mNamesProg.add("Blur");
        mImageUrls.add(R.drawable.icon_a_star);

        mNamesUser.add("B&W"); // 3
        mNamesProg.add("B&W");
        mImageUrls.add(R.drawable.icon_a_star);

        mNamesUser.add("Blue laguna"); // 4
        mNamesProg.add("Blue laguna");
        mImageUrls.add(R.drawable.icon_a_star);

        mNamesUser.add("Contrast"); // 5
        mNamesProg.add("Contrast");
        mImageUrls.add(R.drawable.icon_a_star);

        mNamesUser.add("Sephia"); // 6
        mNamesProg.add("Sephia");
        mImageUrls.add(R.drawable.icon_a_star);

        mNamesUser.add("Noise"); // 7
        mNamesProg.add("Noise");
        mImageUrls.add(R.drawable.icon_a_star);

        mNamesUser.add("Green grass"); // 8
        mNamesProg.add("Green grass");
        mImageUrls.add(R.drawable.icon_a_star);

        try {
            initRecyclerView();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void initRecyclerView() throws NoSuchMethodException {
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView filters_bar = activity.findViewById(R.id.filters_bar);
        filters_bar.setLayoutManager(layoutManager);
        FiltersAdapter adapter = new FiltersAdapter(activity, mNamesUser, mNamesProg, mImageUrls);
        filters_bar.setAdapter(adapter);
    }


}
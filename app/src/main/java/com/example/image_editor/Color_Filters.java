package com.example.image_editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
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

    Color_Filters(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        this.activity = activity;
        this.imageView = activity.getImageView();

    }

    void touchToolbar() {
        super.touchToolbar();
        activity.getLayoutInflater().inflate( // constant line (magic)
                R.layout.filters_menu, // your layout
                activity.getPlaceHolder()); // constant line (magic)
        RecyclerView rv = activity.findViewById(R.id.recyclerView);
        rv.setVisibility(View.GONE);
        // here you can touch your extending layout

        Button btn_filter_picker = activity.findViewById(R.id.filter_picker);

        configSelectFilterButton(btn_filter_picker);

        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        imageView.setImageBitmap(bitmap);
    }

    private void configSelectFilterButton(Button button) {
        button.setText("select filter");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
    }


    private void showFilterDialog() {
        AlertDialog.Builder filterDialog = new AlertDialog.Builder(activity);
        filterDialog.setTitle("Select color filter");
        final String[] pictureDialogItems = {
                "Movie",
                "Blur",
                "Black and white"};
        filterDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // start algo in the background
                        AsyncTaskConductor filterAsync = new AsyncTaskConductor();
                        filterAsync.execute(pictureDialogItems[which]);
                    }
                });
        filterDialog.show();
    }
}
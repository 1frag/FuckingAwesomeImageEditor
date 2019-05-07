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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private DesignerSingleton managerDesign;

    Color_Filters(DesignerSingleton managerDesign) {
        super(managerDesign.btn4);
        this.managerDesign = managerDesign;
        this.selectFilter = managerDesign.btn1;
        this.imageView = managerDesign.imageView;
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

    void touchToolbar() {
        super.touchToolbar();
        managerDesign.btn4.setText("Do algo");
        configSelectFilterButton(managerDesign.btn1);

        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        imageView.setImageBitmap(bitmap);
    }

    private void showFilterDialog() {
//        AlertDialog.Builder filterDialog = new AlertDialog.Builder();
//        filterDialog.setTitle("Select color filter");
//        String[] pictureDialogItems = {
//                "Movie",
//                "Blur",
//                "Black and white"};
//        filterDialog.setItems(pictureDialogItems,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // start algo in the backgroung
//                        AsyncTaskFilters filterAsync = new AsyncTaskFilters();
//                        filterAsync.execute(which);
//                    }
//                });
//        filterDialog.show();
    }

    private class AsyncTaskFilters extends AsyncTask <Integer, Void, Bitmap> {

//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            Toast.makeText(getApplicationContext(), "New thread created", Toast.LENGTH_SHORT).show();

//        }

        @Override
        protected Bitmap doInBackground(Integer... which) {
            int a = which[0];
            Bitmap result = null;
            switch (a) {
                case 0:
                    result = ColorFIltersCollection.movieFilter(bitmap);
                    break;
                case 1:
                    result = ColorFIltersCollection.fastBlur(bitmap);
                    break;
                case 2:
                    result = ColorFIltersCollection.createGrayScale(bitmap);
                    break;
            }
            if (result == null){
//                Toast.makeText(getApplicationContext(), "This wasn't supposed to happen.", Toast.LENGTH_LONG).show();
                return bitmap;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            bufferedBitmap = result;
            imageView.setImageBitmap(result);
//            Toast.makeText(getApplicationContext(), "SO GOOD", Toast.LENGTH_SHORT).show();
        }
    }

}

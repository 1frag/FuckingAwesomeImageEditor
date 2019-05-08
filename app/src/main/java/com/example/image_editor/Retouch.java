package com.example.image_editor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnTouchListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;

import static java.lang.Math.abs;

public class Retouch extends Conductor implements OnTouchListener {
    private MainActivity activity;

    private Bitmap bitmap;
    private Bitmap mask;
    private Bitmap bufferedBitmap;
    private Bitmap original;

    private Button applyRetouch;

    private ImageView imageView;

    private TextView textViewBrushSize;
    private TextView textViewBlurRadius;

    Canvas bufferCanvas;
    Canvas canvas;

    Retouch(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        this.activity = activity;
    }

    void touchToolbar() {
        super.touchToolbar();
        activity.getLayoutInflater().inflate( // constant line (magic)
                R.layout.retouch_menu, // your layout
                activity.getPlaceHolder()); // constant line (magic)
        RecyclerView rv = activity.findViewById(R.id.recyclerView);
        rv.setVisibility(View.GONE);
        // here you can touch your extending layout

        mainActivityMenuGone();

        applyRetouch = activity.findViewById(R.id.btn_apply_retouch);

        textViewBrushSize = activity.findViewById(R.id.text_view_brush_size);
        textViewBlurRadius = activity.findViewById(R.id.text_view_radius);

        configApplyButton(applyRetouch);

        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        imageView.setImageBitmap(bitmap);

    }

    private void configApplyButton(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // PorterDuff режим
                PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;

                Paint paint = new Paint();
                paint.setXfermode(new PorterDuffXfermode(mode));

                // blurred bitmap
                bufferedBitmap = ColorFIltersCollection.fastBlur(original);

                bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                bufferCanvas = new Canvas(bitmap);

                bufferCanvas.drawBitmap(mask, 0, 0, null);
                bufferCanvas.drawBitmap(bufferedBitmap, 0, 0, paint);

                //dump the buffer
                canvas.drawBitmap(original, 0, 0, null);
                canvas.drawBitmap(bitmap, 0, 0, null);

                // TODO: need to return drawing option to user
            }
        });
    }

    private boolean canPutRect(int rad, int mx, int my) {
        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= bitmap.getWidth() ||
                        0 > my + j || my + j >= bitmap.getHeight()) {
                    continue;
                }
                if (abs(i) + abs(j) <= rad) {
                    if (bitmap.getPixel(mx + i, my + j) == Color.rgb(10, 255, 10) ||
                            bitmap.getPixel(mx + i, my + j) == Color.rgb(255, 10, 10)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void errorTouched() {
        // todo: hand this
        return;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int mx = (int) event.getX();
        int my = (int) event.getY();

        int rad = 10;
        if (!canPutRect(rad, mx, my)) {
            errorTouched();
            return false;
        }

        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= bitmap.getWidth()) {
                    continue;
                }
                if (0 > my + j || my + j >= bitmap.getHeight()) {
                    continue;
                }
                if (Math.sqrt(i*i + j*j) <= rad) {
                    Pixel now = new Pixel(mx + i, my + j, bitmap.getPixel(mx + i, my + j));
//                        remfinish.add(now);
                    bitmap.setPixel(mx + i, my + j, Color.RED);
                    mask.setPixel(mx + i, my + j, Color.BLUE);
                }
            }
        }
        imageView.invalidate();
        return true;
    }

}

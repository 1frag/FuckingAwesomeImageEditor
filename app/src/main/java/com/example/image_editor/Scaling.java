package com.example.image_editor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class Scaling extends Conductor {

    private Bitmap bitmap;
    private Bitmap original;
    private ImageView imageView;
    private MainActivity activity;

    private TextView textViewScaling;
    private TextView textWidth;
    private TextView textHeight;

    private Button resetScaling;
    private Button applyScaling;

    private SeekBar seekBarScaling;

    private int scalingValue = 100;
    
    Scaling(MainActivity activity) {
        super(activity);
        this.activity = activity;
        this.imageView = activity.getImageView();
    }

    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.scaling_menu);

        original = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        bitmap = original.copy(Bitmap.Config.ARGB_8888, true);

        resetScaling = activity.findViewById(R.id.btn_reset_scaling);
        applyScaling = activity.findViewById(R.id.btn_apply_scaling);
        textViewScaling = activity.findViewById(R.id.text_view_scale_size);
        textHeight = activity.findViewById(R.id.current_height);
        textWidth = activity.findViewById(R.id.current_width);

        textWidth.setText("Width: " + bitmap.getWidth());
        textHeight.setText("Height: " + bitmap.getHeight());

        seekBarScaling = activity.findViewById(R.id.seek_bar_scaling);
        seekBarScaling.setMax(200);
        seekBarScaling.setProgress(100);

        configResetButton(resetScaling);
        configApplyButton(applyScaling);

        // TODO: reconfig me pls
        seekBarScaling.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                scalingValue = progress;
                textViewScaling.setText("Scale: " + ((float)scalingValue/100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                return;
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) { return; }

        });
    }


    private void configResetButton(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(original);
            }
        });
    }

    private void configApplyButton(final Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskConductor scalingAsync = new AsyncTaskConductor(){
                    @Override
                    protected Bitmap doInBackground(String... params){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setEnabled(false);
                            }
                        });
                        bitmap = algorithm(original, (float) scalingValue/100);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                                textWidth.setText("Width: " + bitmap.getWidth());
                                textHeight.setText("Height: " + bitmap.getHeight());
                                button.setEnabled(true);
                            }
                        });
                        return bitmap;
                    }
                };
                scalingAsync.execute();
            }
        });
    }

    Bitmap algorithm(Bitmap now, float coef) {
        if (coef < 0.12){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity.getApplicationContext(), "Dude, it's too small", Toast.LENGTH_SHORT).show();
                }
            });
            return now;
        }
        int w = now.getWidth();
        int h = now.getHeight();

        if (coef > 1) now = ColorFIltersCollection.resizeBilinear(now, w, h, (int)(w*coef), (int)(h*coef));

        else now = ColorFIltersCollection.resizeBicubic(now, (int)(w*coef), activity.getApplicationContext());  // test

        return now;
    }
}

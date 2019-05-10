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
    private ImageView imageView;
    private MainActivity activity;

    private TextView textViewScaling;

    private Button resetScaling;

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

        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        resetScaling = activity.findViewById(R.id.btn_reset_scaling);
        textViewScaling = activity.findViewById(R.id.text_view_scale_size);

        seekBarScaling = activity.findViewById(R.id.seek_bar_scaling);
        seekBarScaling.setMax(200);
        seekBarScaling.setProgress(100);

        configResetButton(resetScaling);

        // TODO: reconfig me pls
        seekBarScaling.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                scalingValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                return;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewScaling.setText("Scale: " + ((float)scalingValue/100));
                bitmap = algorithm(bitmap, (float) scalingValue/100);
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    // TODO: here)
    private void configResetButton(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });
    }

    Bitmap algorithm(Bitmap now, float coef) {
        int w = now.getWidth();
        int h = now.getHeight();
        System.out.println(w);
        System.out.println(h);
        System.out.println(coef);

        // TODO: some exceptions here
        now = ColorFIltersCollection.resizeBilinear(now, w, h, (int)(w*coef), (int)(h*coef));
        System.out.println(now.getHeight());
        System.out.println(now.getWidth());
        return now;
    }
}

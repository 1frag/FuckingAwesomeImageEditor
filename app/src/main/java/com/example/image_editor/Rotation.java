package com.example.image_editor;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class Rotation extends Conductor {

    private Bitmap bitmap;

    private ImageButton btn_rotate90;
    private Button btn_reset;
    private SeekBar angleSeekBar;

    private ImageView imageView;
    private MainActivity activity;

    private TextView textViewAngle;

    private int currentAngle = 0;

    Rotation(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        this.activity = activity;
        this.imageView = activity.getImageView();
    }

    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.rotate_menu);

        // here you can touch your extending layout

        btn_rotate90 = activity.findViewById(R.id.rotate90);
        btn_reset = activity.findViewById(R.id.reset_seekbar);

        textViewAngle = activity.findViewById(R.id.text_view_angle);

        angleSeekBar = activity.findViewById(R.id.seekBar);
        angleSeekBar.setProgress(90);
        angleSeekBar.setMax(180);

        configRotate90Button(btn_rotate90);
        configResetButton(btn_reset);

        // Initialize the textview with '0'.
        textViewAngle.setText("Current angle: " + (angleSeekBar.getProgress()-90));

        // TODO: really need async
        angleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                Toast.makeText(activity.getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(activity.getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setEnabled(false);
                textViewAngle.setText("Current angle: " + ((angleSeekBar.getProgress()+progress-180)/2));
                Toast.makeText(activity.getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
                System.out.println(progress);
                rotateOnAngle(progress-90);
                seekBar.setEnabled(true);
            }
        });
        
        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        imageView.setImageBitmap(bitmap);

    }

    private void configResetButton(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(bitmap);
                currentAngle = 0;
                angleSeekBar.setProgress(90);
                textViewAngle.setText("Current angle: " + (angleSeekBar.getProgress()-90));
            }
        });
    }

    private void configRotate90Button(ImageButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateOnAngle(currentAngle + 90);
                currentAngle += 90;
                textViewAngle.setText("Current angle: " + currentAngle);
            }
        });
    }

    private void rotateOnAngle(int angle) {
        Bitmap btmp = bitmap;
        if (angle < 0) angle += 360;
        if (((angle / 90) & 1) == 1){
            btmp = Bitmap.createBitmap(bitmap.getHeight(),
                    bitmap.getWidth(),
                    Bitmap.Config.ARGB_8888);
            btmp = btmp.copy(Bitmap.Config.ARGB_8888, true);
        }

        for(int w=0;w<bitmap.getWidth();w++){
            for(int h=0;h<bitmap.getHeight();h++){
                int a = w, b = h;
                if(((angle / 90) & 1) == 1){
                    a = bitmap.getHeight() - h;
                    b = w;
                }
                if(((angle / 90) & 2) == 2){
                    a = bitmap.getHeight() - h;
                    b = bitmap.getWidth() - w;
                }
                if(a<0 || a>=btmp.getWidth())continue;
                if(b<0 || b>=btmp.getHeight())continue;
                if(w<0 || w>=bitmap.getWidth())continue;
                if(h<0 || h>=bitmap.getHeight())continue;
                btmp.setPixel(a, b, bitmap.getPixel(w, h));
            }
        }
        bitmap = btmp;
        int x = btmp.getWidth();
        int y = btmp.getHeight();
        double a = (double) (90 - angle % 90) * Math.PI / 180.0;
        double cosa = Math.cos(a);
        double sina = Math.sin(a);
        double AB = x * sina + y * cosa;
        double AD = y * sina + x * cosa;

        btmp = Bitmap.createBitmap((int) AB + 2, (int) AD + 2, Bitmap.Config.ARGB_8888);
        btmp = btmp.copy(Bitmap.Config.ARGB_8888, true);
//        Log.i("UPD", "hi");
        for (int nx = 0; nx <= (int) AB; nx++) {
//            Log.i("UPD", ((Integer)nx).toString());
            for (int ny = 0; ny <= (int) AD; ny++) {
                int w = (int) (nx * sina - ny * cosa + x * cosa * cosa);
                int h = (int) (nx * cosa + ny * sina - x * sina * cosa);

                if(w<0 || w>=bitmap.getWidth())continue;
                if(h<0 || h>=bitmap.getHeight())continue;
                btmp.setPixel(nx, ny, bitmap.getPixel(w, h));
            }
        }
//        Log.i("UPD", "end");

        imageView.setImageBitmap(btmp);
        imageView.invalidate();
    }
}

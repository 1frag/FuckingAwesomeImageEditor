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
        activity.getLayoutInflater().inflate( // constant line (magic)
                R.layout.rotate_menu, // your layout
                activity.getPlaceHolder()); // constant line (magic)
        RecyclerView rv = activity.findViewById(R.id.recyclerView);
        rv.setVisibility(View.GONE);
        // here you can touch your extending layout

        activity.findViewById(R.id.imgUndo).setVisibility(View.GONE);
        activity.findViewById(R.id.imgRedo).setVisibility(View.GONE);
        activity.findViewById(R.id.imgDownload).setVisibility(View.GONE);
        activity.findViewById(R.id.imgCamera).setVisibility(View.GONE);
        activity.findViewById(R.id.imgGallery).setVisibility(View.GONE);

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
                textViewAngle.setText("Current angle: " + ((angleSeekBar.getProgress()+progress-180)/2));
                Toast.makeText(activity.getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
                System.out.println(progress);
                rotateOnAngle(progress-90);
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

    @SuppressLint("Assert")
    private void rotateOnAngle(int angle) {
        if (angle < 0) angle += 360;
        int x = bitmap.getWidth();
        int y = bitmap.getHeight();
        double a = (double) (90 - angle % 90) * Math.PI / 180.0;
        double cosa = Math.cos(a);
        double sina = Math.sin(a);
        double AB = x * sina + y * cosa;
        double AD = y * sina + x * cosa;
        Bitmap btmp;
        btmp = Bitmap.createBitmap((int) AB + 2, (int) AD + 2, Bitmap.Config.ARGB_8888);
        btmp = btmp.copy(Bitmap.Config.ARGB_8888, true);
//        Log.i("UPD", "hi");
        for (int nx = 0; nx <= (int) AB; nx++) {
//            Log.i("UPD", ((Integer)nx).toString());
            for (int ny = 0; ny <= (int) AD; ny++) {
                int w = (int) (nx * sina - ny * cosa + x * cosa * cosa);
                int h = (int) (nx * cosa + ny * sina - x * sina * cosa);
                if(((angle / 90) & 1) == 1){
                    int cnt = w;
                    w = y - h;
                    h = cnt;
                }
                if(((angle / 90) & 2) == 2){
                    h = y - h;
                    w = x - w;
                }
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

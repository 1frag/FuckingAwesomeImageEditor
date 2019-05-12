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
    private Bitmap original;

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

        textViewAngle.setText("Current angle: " + (angleSeekBar.getProgress()-90));

        angleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                textViewAngle.setText("Current angle: " + ((angleSeekBar.getProgress()+progress-180)/2));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { return; }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setEnabled(false);
                System.out.println(progress);
                AsyncTaskConductor asyncRotate = new AsyncTaskConductor(){
                    @Override
                    protected Bitmap doInBackground(String... params){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_rotate90.setEnabled(false);
                                btn_reset.setEnabled(false);
                                angleSeekBar.setEnabled(false);
                            }
                        });
                        bitmap = rotateOnAngle(progress - 90);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_rotate90.setEnabled(true);
                                btn_reset.setEnabled(true);
                                angleSeekBar.setEnabled(true);
                            }
                        });
                        return bitmap;
                    }
                };
                asyncRotate.execute();
                seekBar.setEnabled(true);
            }
        });
        
        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        original = bitmap.copy(Bitmap.Config.ARGB_8888, false);

        imageView.setImageBitmap(bitmap);

    }

    private void configResetButton(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(original);
                currentAngle = 0;
                angleSeekBar.setProgress(90);
                textViewAngle.setText("Current angle: " + (angleSeekBar.getProgress()-90));
            }
        });
    }

    private void configRotate90Button(final ImageButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskConductor asyncRotate = new AsyncTaskConductor(){
                    @Override
                    protected Bitmap doInBackground(String... params){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setEnabled(false);
                                btn_reset.setEnabled(false);
                                angleSeekBar.setEnabled(false);
                            }
                        });
                        bitmap = rotateOnAngle(currentAngle + 90);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setEnabled(true);
                                btn_reset.setEnabled(true);
                                angleSeekBar.setEnabled(true);
                            }
                        });
                        return bitmap;
                    }
                };
                asyncRotate.execute();
                currentAngle += 90;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.invalidate();
                        textViewAngle.setText("Current angle: " + currentAngle);
                    }
                });
            }
        });
    }

    private Bitmap rotateOnAngle(int angle) {
        Bitmap btmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
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

        return btmp;
    }
}

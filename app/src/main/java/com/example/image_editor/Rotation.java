package com.example.image_editor;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.provider.ContactsContract;
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
    private Button btn_apply;
    private ImageButton btn_mirrorV;
    private ImageButton btn_mirrorH;
    private ImageButton btn_crop;
    private SeekBar angleSeekBar;

    private ImageView imageView;
    private MainActivity activity;

    private TextView textViewAngle;

    private int mCurrentAngle = 0;
    private int mProgress = 45;

    Rotation(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        this.activity = activity;
        this.imageView = activity.getImageView();
    }

    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.rotate_menu);

        btn_rotate90 = activity.findViewById(R.id.rotate90);
        btn_reset = activity.findViewById(R.id.reset_seekbar);
        btn_apply = activity.findViewById(R.id.btn_apply_rotate);
        btn_mirrorH = activity.findViewById(R.id.mirrorH);
        btn_mirrorV = activity.findViewById(R.id.mirrorV);
        btn_crop = activity.findViewById(R.id.crop);

        textViewAngle = activity.findViewById(R.id.text_view_angle);

        angleSeekBar = activity.findViewById(R.id.seekBar);
        angleSeekBar.setProgress(45);
        angleSeekBar.setMax(90);

        configRotate90Button(btn_rotate90);
        configResetButton(btn_reset);
        configApplyButton(btn_apply);
        configMirrorHorizontalButton(btn_mirrorH);
        configMirrorVerticalButton(btn_mirrorV);
        configCropButton(btn_crop);

        textViewAngle.setText("Angle: " + (angleSeekBar.getProgress()-45));

        angleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                mProgress = progressValue;
                textViewAngle.setText("Angle: " + (mProgress - 45));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { return; }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println(mProgress);
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
                mCurrentAngle = 0;
                angleSeekBar.setProgress(45);
                textViewAngle.setText("Angle: " + (angleSeekBar.getProgress()-45));
            }
        });
    }

    private void configApplyButton(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskConductor asyncRotate = new AsyncTaskConductor(){
                    @Override
                    protected Bitmap doInBackground(String... params){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lockButtons();
                            }
                        });
                        bitmap = rotateOnAngle(mProgress - 45);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                unlockButtons();
                                imageView.invalidate();
                            }
                        });
                        return bitmap;
                    }
                };
                asyncRotate.execute();
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
                                lockButtons();
                            }
                        });
                        bitmap = rotateOnAngle(mCurrentAngle + 90);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                unlockButtons();
                                textViewAngle.setText("Angle: " + mCurrentAngle);
                                imageView.invalidate();
                            }
                        });
                        return bitmap;
                    }
                };
                asyncRotate.execute();
                mCurrentAngle += 90;
            }
        });
    }

    // TODO: below
    private void configMirrorHorizontalButton(ImageButton button){

    }

    private void configMirrorVerticalButton(ImageButton button){

    }

    private void configCropButton(ImageButton button){

    }

    // secure algo running
    private void lockButtons(){
        btn_rotate90.setEnabled(false);
        btn_reset.setEnabled(false);
        angleSeekBar.setEnabled(false);
        btn_apply.setEnabled(false);
    }

    private void unlockButtons(){
        btn_rotate90.setEnabled(true);
        btn_reset.setEnabled(true);
        angleSeekBar.setEnabled(true);
        btn_apply.setEnabled(true);
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

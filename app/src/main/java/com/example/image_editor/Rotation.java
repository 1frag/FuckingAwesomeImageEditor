package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


public class Rotation extends Conductor {

    private Bitmap mBitmap;
    private Bitmap mOriginal;

    private Button mResetRotateButton;
    private Button mApplyRotateButton;
    private ImageButton mRotate90Button;
    private ImageButton mMirrorVButton;
    private ImageButton mMirrorHButton;
    private ImageButton mCropButton;
    private SeekBar mSeekBarAngle;

    private ImageView mImageView;
    private MainActivity mainActivity;

    private TextView mTextViewAngle;

    private int mCurrentAngle = 0;
    private int mProgress = 45;

    Rotation(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        mainActivity = activity;
        mImageView = activity.getImageView();
    }

    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.rotate_menu);

        mRotate90Button = mainActivity.findViewById(R.id.button_rotate90);
        mResetRotateButton = mainActivity.findViewById(R.id.button_reset_seekbar);
        mApplyRotateButton = mainActivity.findViewById(R.id.button_apply_rotate);
        mMirrorHButton = mainActivity.findViewById(R.id.button_mirrorH);
        mMirrorVButton = mainActivity.findViewById(R.id.button_mirrorV);
        mCropButton = mainActivity.findViewById(R.id.button_crop);

        mTextViewAngle = mainActivity.findViewById(R.id.text_angle);

        mSeekBarAngle = mainActivity.findViewById(R.id.seekbar_rotate);
        mSeekBarAngle.setProgress(45);
        mSeekBarAngle.setMax(90);

        configRotate90Button(mRotate90Button);
        configResetButton(mResetRotateButton);
        configApplyButton(mApplyRotateButton);
        configMirrorHorizontalButton(mMirrorHButton);
        configMirrorVerticalButton(mMirrorVButton);
        configCropButton(mCropButton);

        mTextViewAngle.setText("Angle: " + (mSeekBarAngle.getProgress()-45));

        mSeekBarAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                mProgress = progressValue;
                mTextViewAngle.setText("Angle: " + (mProgress - 45));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { return; }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println(mProgress);
            }
        });
        
        mBitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        mOriginal = mBitmap.copy(Bitmap.Config.ARGB_8888, false);

        mImageView.setImageBitmap(mBitmap);

    }

    private void configResetButton(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.setImageBitmap(mOriginal);
                mCurrentAngle = 0;
                mSeekBarAngle.setProgress(45);
                mTextViewAngle.setText("Angle: " + (mSeekBarAngle.getProgress()-45));
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
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lockButtons();
                            }
                        });
                        mBitmap = rotateOnAngle(mProgress - 45);
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                unlockButtons();
                                mImageView.invalidate();
                            }
                        });
                        return mBitmap;
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
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lockButtons();
                            }
                        });
                        mBitmap = rotateOnAngle(mCurrentAngle + 90);
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                unlockButtons();
                                mTextViewAngle.setText("Angle: " + mCurrentAngle);
                                mImageView.invalidate();
                            }
                        });
                        return mBitmap;
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
        mRotate90Button.setEnabled(false);
        mResetRotateButton.setEnabled(false);
        mSeekBarAngle.setEnabled(false);
        mApplyRotateButton.setEnabled(false);
    }

    private void unlockButtons(){
        mRotate90Button.setEnabled(true);
        mResetRotateButton.setEnabled(true);
        mSeekBarAngle.setEnabled(true);
        mApplyRotateButton.setEnabled(true);
    }

    private Bitmap rotateOnAngle(int angle) {
        Bitmap btmp = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        if (angle < 0) angle += 360;
        if (((angle / 90) & 1) == 1){
            btmp = Bitmap.createBitmap(mBitmap.getHeight(),
                    mBitmap.getWidth(),
                    Bitmap.Config.ARGB_8888);
            btmp = btmp.copy(Bitmap.Config.ARGB_8888, true);
        }

        for(int w = 0; w< mBitmap.getWidth(); w++){
            for(int h = 0; h< mBitmap.getHeight(); h++){
                int a = w, b = h;
                if(((angle / 90) & 1) == 1){
                    a = mBitmap.getHeight() - h;
                    b = w;
                }
                if(((angle / 90) & 2) == 2){
                    a = mBitmap.getHeight() - h;
                    b = mBitmap.getWidth() - w;
                }
                if(a<0 || a>=btmp.getWidth())continue;
                if(b<0 || b>=btmp.getHeight())continue;
                btmp.setPixel(a, b, mBitmap.getPixel(w, h));
            }
        }
        mBitmap = btmp;
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

                if(w<0 || w>= mBitmap.getWidth())continue;
                if(h<0 || h>= mBitmap.getHeight())continue;
                btmp.setPixel(nx, ny, mBitmap.getPixel(w, h));
            }
        }
//        Log.i("UPD", "end");

        return btmp;
    }
}

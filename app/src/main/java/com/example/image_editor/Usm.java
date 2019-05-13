package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

public class Usm extends Conductor {

    private Bitmap mBitmap;

    private SeekBar mSeekBarAmount;
    private SeekBar mSeekBarRadius;
    private SeekBar mSeekBarThreshold;

    private Button runUsmButton;

    private MainActivity mainActivity;
    private ImageView mImageView;

    private double mAmount = 1;
    private double mRadius = 1;
    private int mThreshold = 0;

    Usm(MainActivity activity) {
        super(activity);
        mainActivity = activity;
        mImageView = activity.getImageView();
    }

    void touchToolbar() {
        // btn1 -> draw point
        // btn2 -> do interpolation
        super.touchToolbar();
        PrepareToRun(R.layout.sharpness_menu);

        mSeekBarAmount = mainActivity.findViewById(R.id.seekbar_amount);
        mSeekBarAmount.setMax(100);
        mSeekBarThreshold = mainActivity.findViewById(R.id.seekbar_threshold);
        mSeekBarThreshold.setMax(255);
        mSeekBarRadius = mainActivity.findViewById(R.id.seekbar_radius);
        mSeekBarRadius.setMax(50);

        runUsmButton = mainActivity.findViewById(R.id.button_start_usm);

        configRunUsmButton(runUsmButton);

        mBitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        mBitmap = mBitmap.copy(Bitmap.Config.RGB_565, true);
        mImageView.setImageBitmap(mBitmap);
    }

    private void configRunUsmButton(final Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long startTime = System.currentTimeMillis();
                AsyncTaskConductor asyncTask = new AsyncTaskConductor(){
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setEnabled(false);
                            }
                        });
                        algorithm();
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setEnabled(true);
                            }
                        });
                        return mBitmap;
                    }
                };
                asyncTask.execute();
                long endTime = System.currentTimeMillis();
                Log.i("upd", "already");
                System.out.println("That took " + (endTime - startTime) + " milliseconds");
            }
        });

    }
    private int getR(int color){
        return ((1 << 8) - 1) & color;
    }

    private int getG(int color){
        return ((((1 << 8) - 1) << 8) & color) >> 8;
    }

    private int getB(int color){
        return ((((1 << 8) - 1) << 16) & color) >> 16;
    }

    private int fixColor(int origColor, int blurColor){
        int R = getR(origColor);
        int G = getG(origColor);
        int B = getB(origColor);

        int difR = (int) ((255 - getR(blurColor)) / mAmount);
        double dR = Math.abs(difR - R);
        if(dR > mThreshold)R += difR;

        int difG = (int) ((255 - getG(blurColor)) / mAmount);
        double dG = Math.abs(difG - G);
        if(dG > mThreshold)G += difG;

        int difB = (int) ((255 - getB(blurColor)) / mAmount);
        double dB = Math.abs(difB - B);
        if(dB > mThreshold)B += difB;

        if(R>255)R=255;if(R<0)R=0;
        if(G>255)G=255;if(G<0)G=0;
        if(B>255)B=255;if(B<0)B=0;

        return Color.rgb(R, G, B);
    }

    private void algorithm() {
        InitParams();

        Log.i("upd", ((Integer) mBitmap.getWidth()).toString() + " x " +
                ((Integer) mBitmap.getHeight()).toString());

//        Bitmap blurred = (new GaussianBlur(mBitmap, mRadius)).algorithm();
        Bitmap blurred = ColorFIltersCollection.fastBlur(mBitmap, (int) mRadius, 1);

//        for (int w = 0; w < blurred.getWidth(); w++) {
//            for (int h = 0; h < blurred.getHeight(); h++) {
//                int now = mBitmap.getPixel(w, h);
//                int R = (int) ((255 - Color.red(now)) / mAmount);
//                int G = (int) ((255 - Color.green(now)) / mAmount);
//                int B = (int) ((255 - Color.blue(now)) / mAmount);
//                blurred.setPixel(w, h,
//                        Color.rgb(R, G, B));
//            }
//        }
//
//        Bitmap unsharpMask = difference(mBitmap, blurred);
//        Bitmap highContrast = increaseContrast(mBitmap, mAmount);

        for (int w = 0; w < mBitmap.getWidth(); w++) {
            for (int h = 0; h < mBitmap.getHeight(); h++) {
                int origColor = mBitmap.getPixel(w, h);
                int blurColor = blurred.getPixel(w, h);
//                int contrastColor = highContrast.getPixel(w, h);

                mBitmap.setPixel(w, h, fixColor(origColor, blurColor));
            }
        }
    }

    private void InitParams() {

        this.mAmount = mSeekBarAmount.getProgress();
        this.mRadius = 1 + mSeekBarRadius.getProgress();
        this.mThreshold = mSeekBarThreshold.getProgress();

        // debug information
        System.out.println();
        System.out.println(mAmount);
        System.out.println(mRadius);
        System.out.println(mThreshold);

    }

    private double luminanceAsPercent(int pixel) {
        int R = Color.red(pixel);
        int G = Color.green(pixel);
        int B = Color.blue(pixel);
        return ((0.2126 * R) + (0.7152 * G) + (0.0722 * B)) / 255.0;
    }

    private Bitmap increaseContrast(Bitmap bitmap, double amount) {
        Bitmap result = bitmap.copy(Bitmap.Config.RGB_565, true);
        double F = (259.0 * (amount + 255)) / (255.0 * (259 - amount));
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                int now = bitmap.getPixel(i, j);
                int R = Color.red(now);
                R = (int) (F * (R - 128) + 128);
                int G = Color.green(now);
                G = (int) (F * (G - 128) + 128);
                int B = Color.blue(now);
                B = (int) (F * (B - 128) + 128);
                if(R>255)R=255;if(R<0)R=0;
                if(G>255)G=255;if(G<0)G=0;
                if(B>255)B=255;if(B<0)B=0;
                if (R>255 || B>255 || G>255 || R<0 || B<0 || G<0){
                    Log.i("upd", "xepnZ");
                    Log.i("upd", ((Integer)now).toString());
                    Log.i("upd", ((Integer)R).toString());
                    Log.i("upd", ((Integer)G).toString());
                    Log.i("upd", ((Integer)B).toString());
                    Log.i("upd", "xepnZ");
                }
                result.setPixel(i, j,
                        Color.rgb(R, G, B));
            }
        }
        return result;
    }

    private Bitmap difference(Bitmap bitmap, Bitmap blurred) {
        Bitmap result = bitmap.copy(Bitmap.Config.RGB_565, true);
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                result.setPixel(i, j,
                        bitmap.getPixel(i, j) - blurred.getPixel(i, j));
            }
        }
        return result;
    }

}

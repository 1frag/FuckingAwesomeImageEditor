package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

public class Usm extends Conductor {

    private Bitmap bitmap;
    private MainActivity activity;
    private ImageView imageView;
    private double amount;
    private double radius;
    private int threshold;

    Usm(MainActivity activity) {
        super(activity);
        this.activity = activity;
        this.imageView = activity.getImageView();
    }

    void touchToolbar() {
        // btn1 -> draw point
        // btn2 -> do interpolation
        super.touchToolbar();
        PrepareToRun(R.layout.sharpness_menu);

        activity.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                algorithm();
                imageView.invalidate();
                Log.i("upd", "already");
            }
        });

        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
        imageView.setImageBitmap(bitmap);

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

        int difR = (int) ((255 - getR(blurColor)) / amount);
        double dR = Math.abs(difR - R);
        if(dR > threshold)R += difR;

        int difG = (int) ((255 - getG(blurColor)) / amount);
        double dG = Math.abs(difG - G);
        if(dG > threshold)G += difG;

        int difB = (int) ((255 - getB(blurColor)) / amount);
        double dB = Math.abs(difB - B);
        if(dB > threshold)B += difB;

        if(R>255)R=255;if(R<0)R=0;
        if(G>255)G=255;if(G<0)G=0;
        if(B>255)B=255;if(B<0)B=0;

        return Color.rgb(R, G, B);
    }

    private void algorithm() {
        InitParams();

        Log.i("upd", ((Integer)bitmap.getWidth()).toString() + " x " +
                ((Integer)bitmap.getHeight()).toString());

        Bitmap blurred = (new GaussianBlur(bitmap, radius)).algorithm();
//        Bitmap blurred = ColorFIltersCollection.fastBlur(bitmap, (int)radius, 1);

//        for (int w = 0; w < blurred.getWidth(); w++) {
//            for (int h = 0; h < blurred.getHeight(); h++) {
//                int now = bitmap.getPixel(w, h);
//                int R = (int) ((255 - Color.red(now)) / amount);
//                int G = (int) ((255 - Color.green(now)) / amount);
//                int B = (int) ((255 - Color.blue(now)) / amount);
//                blurred.setPixel(w, h,
//                        Color.rgb(R, G, B));
//            }
//        }
//
//        Bitmap unsharpMask = difference(bitmap, blurred);
//        Bitmap highContrast = increaseContrast(bitmap, amount);

        for (int w = 0; w < bitmap.getWidth(); w++) {
            for (int h = 0; h < bitmap.getHeight(); h++) {
                int origColor = bitmap.getPixel(w, h);
                int blurColor = blurred.getPixel(w, h);
//                int contrastColor = highContrast.getPixel(w, h);

                bitmap.setPixel(w, h, fixColor(origColor, blurColor));
            }
        }
    }

    private void InitParams() {

        this.amount = ((SeekBar) activity.findViewById(R.id.seekBarAmount)).getProgress();
        this.radius = ((SeekBar) activity.findViewById(R.id.seekBarRadius)).getProgress();
        this.threshold = ((SeekBar) activity.findViewById(R.id.seekBarThreshold)).getProgress();

        amount += 50;
        radius = radius / 50.0;
        threshold /= 10;

        amount = 20;
        radius = 5.0;
        threshold = 0;

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

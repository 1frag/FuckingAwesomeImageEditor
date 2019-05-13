package com.example.image_editor;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;

public class GaussianBlur {

    private Bitmap mSource;
    private Bitmap mTarget;

    private double mRadius;
    private int mHeight;
    private int mWidth;

    private ArrayList<Double> boxesForGauss(double sigma, int n)  // standard deviation, number of boxes
    {
        double wIdeal = Math.sqrt((12 * sigma * sigma / n) + 1);  // Ideal averaging filter width
        double wl = Math.floor(wIdeal);
        if (wl % 2 == 0) wl--;
        double wu = wl + 2;

        double mIdeal = (12 * sigma * sigma - n * wl * wl - 4 * n * wl - 3 * n) / (-4 * wl - 4);
        double m = Math.round(mIdeal);
        ArrayList<Double> sizes = new ArrayList<>();
        for (int i = 0; i < n; i++) sizes.add(i < m ? wl : wu);
        return sizes;
    }

    private void boxBlurH(double r) {
        double iarr = 1 / (r + r + 1);

        for (int i = 0; i < mHeight; i++) {
            int tix = 0;
            int lix = 0;
            int rix = tix + (int) r, riy = i;
            int fv = mSource.getPixel(tix, i);
            int lv = mSource.getPixel(tix + mWidth - 1, i);
            int val = ((int) r + 1) * fv;
            for (int j = 0; j < (int) r; j++)
                val += mSource.getPixel(tix + j, i);
            for (int j = 0; j <= (int) r; j++) {
                val += mSource.getPixel(rix++, riy) - fv;
                mTarget.setPixel(tix++, i, (int) Math.round(val * iarr));
            }
            for (int j = (int) r + 1; j < mWidth - r; j++) {
                val += mSource.getPixel(rix++, riy) - mSource.getPixel(lix++, i);
                mTarget.setPixel(tix++, i, (int) Math.round(val * iarr));
            }
            for (int j = mWidth - (int) r; j < mWidth; j++) {
                val += lv - mSource.getPixel(lix++, i);
                mTarget.setPixel(tix++, i, (int) Math.round(val * iarr));
            }
        }
    }

    private void boxBlurT(double r) {
        double iarr = 1 / (r + r + 1);

        for (int i = 0; i < mWidth; i++) {
            int tiy = 0;
            int liy = 0;
            int riy = (int) r;
            int fv = mSource.getPixel(i, tiy);
            int lv = mSource.getPixel(i, mHeight - 1);
            int val = ((int) r + 1) * fv;
            for (int j = 0; j < r; j++)
                val += mSource.getPixel(i, j);
            for (int j = 0; j <= r; j++, riy++, tiy++) {
                val += mSource.getPixel(i, riy) - fv;
                mTarget.setPixel(i, tiy, (int) Math.round(val * iarr));
            }
            for (int j = (int) r + 1; j < mHeight - r; j++, liy++, riy++, tiy++) {
                val += mSource.getPixel(i, riy) - mSource.getPixel(i, liy);
                mTarget.setPixel(i, tiy, (int) Math.round(val * iarr));
            }
            for (int j = mHeight - (int) r; j < mHeight; j++, liy++, tiy++) {
                val += lv - mSource.getPixel(i, liy);
                mTarget.setPixel(i, tiy, (int) Math.round(val * iarr));
            }
        }
    }


    private void boxBlur(double r) {
        for (int i = 0; i < mHeight; i++)
            for (int j = 0; j < mWidth; j++)
                // wtf exception here
                try {
                    mTarget.setPixel(i, j, mSource.getPixel(i, j));
                }
                catch (Exception e){
                    continue;
                }
        boxBlurH(r);
        boxBlurT(r);
    }

    public Bitmap algorithm() {
        mHeight = this.mSource.getHeight();
        mWidth = this.mSource.getWidth();
        ArrayList<Double> bxs = boxesForGauss(mRadius, 3);
        Log.i("upd", "ok1");
        boxBlur((bxs.get(0) - 1) / 2);
        Log.i("upd", "ok1");
        boxBlur((bxs.get(1) - 1) / 2);
        Log.i("upd", "ok1");
        boxBlur((bxs.get(2) - 1) / 2);
        Log.i("upd", "ok1");
        return this.mTarget;
    }

    GaussianBlur(Bitmap source, double radius) {
        this.mSource = source;
        this.mRadius = radius;
        this.mTarget = source.copy(Bitmap.Config.ARGB_8888, true);
    }
}
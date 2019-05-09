package com.example.image_editor;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;

public class GaussianBlur {

    private Bitmap source;
    private Bitmap target;
    private double radius;
    private int h;
    private int w;

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

        for (int i = 0; i < h; i++) {
            int tix = 0;
            int lix = 0;
            int rix = tix + (int) r, riy = i;
            int fv = source.getPixel(tix, i);
            int lv = source.getPixel(tix + w - 1, i);
            int val = ((int) r + 1) * fv;
            for (int j = 0; j < (int) r; j++)
                val += source.getPixel(tix + j, i);
            for (int j = 0; j <= (int) r; j++) {
                val += source.getPixel(rix++, riy) - fv;
                target.setPixel(tix++, i, (int) Math.round(val * iarr));
            }
            for (int j = (int) r + 1; j < w - r; j++) {
                val += source.getPixel(rix++, riy) - source.getPixel(lix++, i);
                target.setPixel(tix++, i, (int) Math.round(val * iarr));
            }
            for (int j = w - (int) r; j < w; j++) {
                val += lv - source.getPixel(lix++, i);
                target.setPixel(tix++, i, (int) Math.round(val * iarr));
            }
        }
    }

    private void boxBlurT(double r) {
        double iarr = 1 / (r + r + 1);

        for (int i = 0; i < w; i++) {
            int tiy = 0;
            int liy = 0;
            int riy = (int) r;
            int fv = source.getPixel(i, tiy);
            int lv = source.getPixel(i, h - 1);
            int val = ((int) r + 1) * fv;
            for (int j = 0; j < r; j++)
                val += source.getPixel(i, j);
            for (int j = 0; j <= r; j++, riy++, tiy++) {
                val += source.getPixel(i, riy) - fv;
                target.setPixel(i, tiy, (int) Math.round(val * iarr));
            }
            for (int j = (int) r + 1; j < h - r; j++, liy++, riy++, tiy++) {
                val += source.getPixel(i, riy) - source.getPixel(i, liy);
                target.setPixel(i, tiy, (int) Math.round(val * iarr));
            }
            for (int j = h - (int) r; j < h; j++, liy++, tiy++) {
                val += lv - source.getPixel(i, liy);
                target.setPixel(i, tiy, (int) Math.round(val * iarr));
            }
        }
    }

    private void boxBlur(double r) {
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                target.setPixel(i, j, source.getPixel(i, j));
        boxBlurH(r);
        boxBlurT(r);
    }

    public Bitmap algorithm() {
        h = this.source.getHeight();
        w = this.source.getWidth();
        ArrayList<Double> bxs = boxesForGauss(radius, 3);
        Log.i("upd", "ok1");
        boxBlur((bxs.get(0) - 1) / 2);
        Log.i("upd", "ok1");
        boxBlur((bxs.get(1) - 1) / 2);
        Log.i("upd", "ok1");
        boxBlur((bxs.get(2) - 1) / 2);
        Log.i("upd", "ok1");
        return this.target;
    }

    GaussianBlur(Bitmap source, double radius) {
        this.source = source;
        this.radius = radius;
        this.target = source.copy(Bitmap.Config.ARGB_8888, true);
    }
}
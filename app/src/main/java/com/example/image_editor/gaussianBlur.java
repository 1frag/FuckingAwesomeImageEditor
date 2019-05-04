package com.example.image_editor;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class gaussianBlur {

    private Bitmap source; // scl
    private Bitmap target; // tcl
    private double radius;

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

    private int scl(int a) {
        int w = this.source.getWidth();
        return this.source.getPixel(a % w, a / w);
    }

    private void tcl(int a, int val) {
        int w = this.target.getWidth();
        this.target.setPixel(a % w, a / w, val);
    }

    private void boxBlurH(double r) {
        int h = this.source.getHeight();
        int w = this.source.getWidth();
        double iarr = 1 / (r + r + 1);

        for (int i = 0; i < h; i++) {
            int ti = i * w, li = ti, ri = ti + (int) r;
            int fv = scl(ti), lv = scl(ti + w - 1), val = ((int) r + 1) * fv;
            for (int j = 0; j < (int) r; j++)
                val += scl(ti + j);
            for (int j = 0; j <= (int) r; j++) {
                val += scl(ri++) - fv;
                tcl(ti++, (int) Math.round(val * iarr));
            }
            for (int j = (int) r + 1; j < w - r; j++) {
                val += scl(ri++) - scl(li++);
                tcl(ti++, (int) Math.round(val * iarr));
            }
            for (int j = w - (int) r; j < w; j++) {
                val += lv - scl(li++);
                tcl(ti++, (int) Math.round(val * iarr));
            }
        }
    }

    private void boxBlurT(double r) {
        int h = this.source.getHeight();
        int w = this.source.getWidth();
        double iarr = 1 / (r + r + 1);

        for (int i = 0; i < w; i++) {
            int ti = i, li = ti, ri = ti + (int) r * w;
            int fv = scl(ti), lv = scl(ti + w * (h - 1)), val = ((int) r + 1) * fv;
            for (int j = 0; j < r; j++)
                val += scl(ti + j * w);
            for (int j = 0; j <= r; j++) {
                val += scl(ri) - fv;
                tcl(ti, (int) Math.round(val * iarr));
                ri += w;
                ti += w;
            }
            for (int j = (int) r + 1; j < h - r; j++) {
                val += scl(ri) - scl(li);
                tcl(ti, (int) Math.round(val * iarr));
                li += w;
                ri += w;
                ti += w;
            }
            for (int j = h - (int) r; j < h; j++) {
                val += lv - scl(li);
                tcl(ti, (int) Math.round(val * iarr));
                li += w;
                ti += w;
            }
        }
    }

    private void boxBlur(double r) {
        int h = this.source.getHeight();
        int w = this.source.getWidth();
        for (int i = 0; i < h * w; i++)
            tcl(i, scl(i));
        boxBlurH(r);
        boxBlurT(r);
    }

    public Bitmap algorithm() {
        ArrayList<Double> bxs = boxesForGauss(radius, 3);
        boxBlur((bxs.get(0) - 1) / 2);
        boxBlur((bxs.get(1) - 1) / 2);
        boxBlur((bxs.get(2) - 1) / 2);
        return this.target;
    }

    gaussianBlur(Bitmap source, double radius) {
        this.source = source;
        this.radius = radius;
        this.target = source.copy(Bitmap.Config.ARGB_8888, true);
    }
}

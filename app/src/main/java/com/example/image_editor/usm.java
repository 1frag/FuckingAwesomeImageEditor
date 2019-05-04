package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Color;

public class usm {

    private Bitmap bitmap;
    private int amount;
    private int radius;
    private int threshold;

    private Bitmap algorithm() {

        Bitmap retval = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap blurred = (new gaussianBlur(bitmap, radius)).algorithm();
        Bitmap unsharpMask = difference(bitmap, blurred);
        Bitmap highContrast = increaseContrast(bitmap, amount);

        for (int w = 0; w < bitmap.getWidth(); w++) {
            for (int h = 0; h < bitmap.getHeight(); h++) {
                int origColor = bitmap.getPixel(w, h);
                int contrastColor = highContrast.getPixel(w, h);

                int difference = contrastColor - origColor;
                double percent = luminanceAsPercent(unsharpMask.getPixel(w, h));

                double delta = difference * percent;

                if (Math.abs(delta) > threshold)
                    retval.setPixel(w, h,
                            retval.getPixel(w, h) + (int)delta);
            }
        }
        return retval;
    }

    private double luminanceAsPercent(int pixel) {
        int R = Color.red(pixel);
        int G = Color.green(pixel);
        int B = Color.blue(pixel);
        return ((0.2126 * R) + (0.7152 * G) + (0.0722 * B)) / 255.0;
    }

    private Bitmap increaseContrast(Bitmap bitmap, int amount) {
        Bitmap result = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        double F = (259.0 * (amount + 255)) / (255.0 * (259 - amount));
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                int now = bitmap.getPixel(i, j);
               t int R = Color.red(now);
                R = (int) (F * (R - 128) + 128);
                int G = Color.green(now);
                G = (int) (F * (G - 128) + 128);
                int B = Color.blue(now);
                B = (int) (F * (B - 128) + 128);
                result.setPixel(i, j,
                        Color.rgb(R, G, B));
            }
        }
        return result;
    }

    private Bitmap difference(Bitmap bitmap, Bitmap blurred) {
        Bitmap result = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                result.setPixel(i, j,
                        bitmap.getPixel(i, j) - blurred.getPixel(i, j));
            }
        }
        return result;
    }

    private Bitmap ImportantShit(Bitmap bitmap, int amount, int radius, int threshold) {

        this.bitmap = bitmap;
        this.amount = amount;
        this.radius = radius;
        this.threshold = threshold;
        return algorithm();
    }

}

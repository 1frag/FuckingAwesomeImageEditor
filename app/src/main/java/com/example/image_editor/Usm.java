package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class Usm extends Conductor {

    private Button mRunUsmButton;

    private SeekBar mSeekBarAmount;
    private SeekBar mSeekBarRadius;
    private SeekBar mSeekBarThreshold;

    private TextView mTextAmount;
    private TextView mTextRadius;
    private TextView mTextThreshold;

    private double mAmount = 1;
    private double mRadius = 1;
    private int mThreshold = 0;

    Usm(MainActivity activity) {
        super(activity);
    }

    @Override
    void touchToolbar() {

        super.touchToolbar();
        prepareToRun(R.layout.sharpness_menu);
        setHeader(mainActivity.getResources().getString(R.string.name_unsharp_masking));

        mSeekBarAmount = mainActivity.findViewById(R.id.seekbar_amount);
        mSeekBarAmount.setMax(100);
        mSeekBarThreshold = mainActivity.findViewById(R.id.seekbar_threshold);
        mSeekBarThreshold.setMax(255);
        mSeekBarRadius = mainActivity.findViewById(R.id.seekbar_radius);
        mSeekBarRadius.setMax(50);

        mTextAmount = mainActivity.findViewById(R.id.text_amount_usm);
        mTextThreshold = mainActivity.findViewById(R.id.text_threshold_usm);
        mTextRadius = mainActivity.findViewById(R.id.text_radius_usm);

        mRunUsmButton = mainActivity.findViewById(R.id.button_start_usm);

        configRunUsmButton(mRunUsmButton);
        configRadiusSeekBar(mSeekBarRadius);
        configThresholdSeekBar(mSeekBarThreshold);
        configAmountSeekBar(mSeekBarAmount);
    }

    @Override
    public void lockInterface() {
        super.lockInterface();
        mRunUsmButton.setEnabled(false);
        mSeekBarAmount.setEnabled(false);
        mSeekBarRadius.setEnabled(false);
        mSeekBarThreshold.setEnabled(false);
    }

    @Override
    public void unlockInterface() {
        super.unlockInterface();
        mRunUsmButton.setEnabled(true);
        mSeekBarAmount.setEnabled(true);
        mSeekBarRadius.setEnabled(true);
        mSeekBarThreshold.setEnabled(true);
    }

    private void configRunUsmButton(final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long startTime = System.currentTimeMillis();
                AsyncTaskConductor asyncTask = new AsyncTaskConductor() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        algorithm();
                        return mainActivity.getBitmap();
                    }
                };
                asyncTask.execute();
                mainActivity.algorithmExecuted = true;
                mainActivity.imageChanged = true;
                long endTime = System.currentTimeMillis();
                Log.i("upd", "already");
                System.out.println("That took " + (endTime - startTime) + " milliseconds");
            }
        });

    }

    private void configRadiusSeekBar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRadius = progress;
                String txt = mainActivity.getResources().getString(R.string.radius_is);
                mTextRadius.setText(String.format(txt, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void configAmountSeekBar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAmount = progress + 1;
                String txt = mainActivity.getResources().getString(R.string.amount_is);
                mTextAmount.setText(String.format(txt, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void configThresholdSeekBar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mThreshold = progress;
                String txt = mainActivity.getResources().getString(R.string.threshold_is);
                mTextThreshold.setText(String.format(txt, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private int getR(int color) {
        return ((((1 << 8) - 1) << 16) & color) >> 16;
    }

    private int getG(int color) {
        return ((((1 << 8) - 1) << 8) & color) >> 8;
    }

    private int getB(int color) {
        return ((1 << 8) - 1) & color;
    }

    private int fixColor(int origColor, int blurColor) {
        int R = getR(origColor);
        int G = getG(origColor);
        int B = getB(origColor);

        int difR = (int) ((255 - getR(blurColor)) / mAmount);
        double dR = Math.abs(difR - R);
        if (dR > mThreshold) R += difR;

        int difG = (int) ((255 - getG(blurColor)) / mAmount);
        double dG = Math.abs(difG - G);
        if (dG > mThreshold) G += difG;

        int difB = (int) ((255 - getB(blurColor)) / mAmount);
        double dB = Math.abs(difB - B);
        if (dB > mThreshold) B += difB;

        if (R > 255) R = 255;
        if (R < 0) R = 0;
        if (G > 255) G = 255;
        if (G < 0) G = 0;
        if (B > 255) B = 255;
        if (B < 0) B = 0;

        return Color.rgb(R, G, B);
    }

    private void algorithm() {

        Bitmap blurred = ColorFIltersCollection.fastBlur(mainActivity.getBitmap(), (int) mRadius, 1);

        for (int w = 0; w < mainActivity.getBitmap().getWidth(); w++) {
            for (int h = 0; h < mainActivity.getBitmap().getHeight(); h++) {
                int origColor = mainActivity.getBitmap().getPixel(w, h);
                int blurColor = blurred.getPixel(w, h);

                mainActivity.getBitmap().setPixel(w, h, fixColor(origColor, blurColor));
            }
        }
    }

}

package com.example.image_editor;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class Usm extends Controller {

    private Button mRunUsmButton;

    private SeekBar mSeekBarAmount;
    private SeekBar mSeekBarRadius;
    private SeekBar mSeekBarThreshold;

    private TextView mTextAmount;
    private TextView mTextRadius;
    private TextView mTextThreshold;

    private String TAG = "upd/Usm";

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
        configMethodInfoButton(
                mainActivity.findViewById(R.id.button_help),
                R.drawable.help_usm);
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
                @SuppressLint("StaticFieldLeak")
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
                mRadius = progress + 1;
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

    private int changeContrast(int pix, double contrast) {
        double newPix = ((((pix / 255f) - 0.5) * contrast) + 0.5) * 255f;
        if (newPix < 0) {
            newPix = 0.0;
        } else if (newPix > 255) {
            newPix = 255.0;
        }
        return (int) newPix;
    }

    private void algorithm() {
        // to discard previous changes
        mainActivity.resetBitmap();

        Bitmap blurred = ColorFIltersCollection.fastBlur(mainActivity.getBitmap(), (int) mRadius, 1);
        double contrast = Math.pow((100 + mAmount) / 100, 2.0);
        int cnt = 0;

        for (int w = 0; w < mainActivity.getBitmap().getWidth(); w++) {
            for (int h = 0; h < mainActivity.getBitmap().getHeight(); h++) {

                int red = Color.red(mainActivity.getPixelBitmap(w, h));
                int green = Color.green(mainActivity.getPixelBitmap(w, h));
                int blue = Color.blue(mainActivity.getPixelBitmap(w, h));

                int diffR = Math.abs( Math.min(
                        red - (int) (Color.red(blurred.getPixel(w, h)) * 1.2), 0));
                int diffG = Math.abs( Math.min(
                        green - (int) (Color.green(blurred.getPixel(w, h)) * 1.2), 0));
                int diffB = Math.abs( Math.min(
                        blue - (int) (Color.blue(blurred.getPixel(w, h)) * 1.2), 0));

                if (red != changeContrast(red, contrast)) cnt++;
                else if (green != changeContrast(green, contrast)) cnt++;
                else if (blue != changeContrast(blue, contrast)) cnt++;

                if (diffR > mThreshold) cnt--;
                else if (diffG > mThreshold) cnt--;
                else if (diffB > mThreshold) cnt--;

                if (diffR > mThreshold) red = changeContrast(red, contrast);
                if (diffG > mThreshold) green = changeContrast(green, contrast);
                if (diffB > mThreshold) blue = changeContrast(blue, contrast);

                mainActivity.getBitmap().setPixel(w, h, Color.rgb(red, green, blue));

            }
        }
        Log.i(TAG, String.format("%d", cnt));
    }

}

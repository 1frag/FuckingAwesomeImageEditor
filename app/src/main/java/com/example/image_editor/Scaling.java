package com.example.image_editor;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class Scaling extends Conductor {

    private Button mResetScalingButton;
    private Button mApplyScalingButton;

    private TextView mTextScaling;
    private TextView mTextWidth;
    private TextView mTextHeight;

    private SeekBar mSeekBarScaling;

    private int mScalingValue = 100;
    
    Scaling(MainActivity activity) {
        super(activity);
    }

    @Override
    void touchToolbar() {
        super.touchToolbar();
        prepareToRun(R.layout.scaling_menu);
        setHeader(mainActivity.getResources().getString(R.string.name_scaling));

        mResetScalingButton = mainActivity.findViewById(R.id.button_reset_scaling);
        mApplyScalingButton = mainActivity.findViewById(R.id.button_apply_scaling);
        mTextScaling = mainActivity.findViewById(R.id.text_scale_size);
        mTextHeight = mainActivity.findViewById(R.id.text_current_height);
        mTextWidth = mainActivity.findViewById(R.id.text_current_width);

        mSeekBarScaling = mainActivity.findViewById(R.id.seekbar_scaling);
        mSeekBarScaling.setMax(200);
        mSeekBarScaling.setProgress(100);

        configResetButton(mResetScalingButton);
        configApplyButton(mApplyScalingButton);
        configScalingSeekBar(mSeekBarScaling);

        mTextWidth.setText(String.format(mainActivity.getResources().getString(R.string.width_is), bitmap.getWidth()));
        mTextHeight.setText(String.format(mainActivity.getResources().getString(R.string.height_is), bitmap.getHeight()));
        mTextScaling.setText(String.format(mainActivity.getResources().getString(R.string.scale_is), 1f));

    }

    @Override
    public void lockInterface(){
        super.lockInterface();
        mSeekBarScaling.setEnabled(false);
        mResetScalingButton.setEnabled(false);
        mApplyScalingButton.setEnabled(false);
    }

    @Override
    public void unlockInterface(){
        super.unlockInterface();
        mSeekBarScaling.setEnabled(true);
        mResetScalingButton.setEnabled(true);
        mApplyScalingButton.setEnabled(true);
    }

    private void configResetButton(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(beforeChanges);
                mTextWidth.setText(String.format(mainActivity.getResources().getString(R.string.width_is), beforeChanges.getWidth()));
                mTextHeight.setText(String.format(mainActivity.getResources().getString(R.string.height_is), beforeChanges.getHeight()));
                mTextScaling.setText(String.format(mainActivity.getResources().getString(R.string.scale_is), 1));
                mSeekBarScaling.setProgress(100);
            }
        });
    }

    private void configApplyButton(final Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("StaticFieldLeak") AsyncTaskConductor scalingAsync = new AsyncTaskConductor(){
                    @Override
                    protected Bitmap doInBackground(String... params){
                        bitmap = algorithm(beforeChanges, (float) mScalingValue/100);
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextWidth.setText(String.format(mainActivity.getResources().getString(R.string.width_is), bitmap.getWidth()));
                                mTextHeight.setText(String.format(mainActivity.getResources().getString(R.string.height_is), bitmap.getHeight()));
                            }
                        });
                        return bitmap;
                    }
                };
                scalingAsync.execute();
            }
        });
    }

    private void configScalingSeekBar(SeekBar seekBar){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mScalingValue = progress;
                mTextScaling.setText(String.format(mainActivity.getResources().getString(R.string.scale_is), ((float) mScalingValue/100)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                return;
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) { return; }
        });
    }

    Bitmap algorithm(Bitmap now, float coef) {
        if (coef < 0.12){
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity.getApplicationContext(),
                            mainActivity.getResources().getString(R.string.warning_scale), Toast.LENGTH_SHORT).show();
                }
            });
            return now;
        }
        int w = now.getWidth();
        int h = now.getHeight();

        // likewise in linear algebra
        if (coef > 1) now = ColorFIltersCollection.resizeBilinear(now, w, h, (int)(w*coef), (int)(h*coef));
        else now = ColorFIltersCollection.resizeBicubic(now, (int)(w*coef), mainActivity.getApplicationContext());

        return now;
    }
}

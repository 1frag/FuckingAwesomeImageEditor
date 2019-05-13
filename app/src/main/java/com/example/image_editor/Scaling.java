package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class Scaling extends Conductor {

    private Bitmap mBitmap;
    private Bitmap mOriginal;

    private Button mResetScalingButton;
    private Button mApplyScalingButton;

    private TextView mTextScaling;
    private TextView mTextWidth;
    private TextView mTextHeight;

    private SeekBar mSeekBarScaling;

    private ImageView mImageView;
    private MainActivity mainActivity;

    private int mScalingValue = 100;
    
    Scaling(MainActivity activity) {
        super(activity);
        this.mainActivity = activity;
        this.mImageView = activity.getImageView();
    }

    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.scaling_menu);

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

        mOriginal = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        mBitmap = mOriginal.copy(Bitmap.Config.ARGB_8888, true);

        mTextWidth.setText("Width: " + mBitmap.getWidth());
        mTextHeight.setText("Height: " + mBitmap.getHeight());
    }

    // TODO: lock buttons
    private void configResetButton(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.setImageBitmap(mOriginal);
            }
        });
    }

    private void configApplyButton(final Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskConductor scalingAsync = new AsyncTaskConductor(){
                    @Override
                    protected Bitmap doInBackground(String... params){
                        mBitmap = algorithm(mOriginal, (float) mScalingValue/100);
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextWidth.setText("Width: " + mBitmap.getWidth());
                                mTextHeight.setText("Height: " + mBitmap.getHeight());
                            }
                        });
                        return mBitmap;
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
                mTextScaling.setText("Scale: " + ((float) mScalingValue/100));
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
                    Toast.makeText(mainActivity.getApplicationContext(), "Dude, it's too small", Toast.LENGTH_SHORT).show();
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

package com.example.image_editor;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class Scaling extends Controller {

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
        configMethodInfoButton(
                mainActivity.findViewById(R.id.button_help),
                R.drawable.help_scaling);

        mTextWidth.setText(String.format(mainActivity.getResources().getString(R.string.width_is), mainActivity.getBitmap().getWidth()));
        mTextHeight.setText(String.format(mainActivity.getResources().getString(R.string.height_is), mainActivity.getBitmap().getHeight()));
        mTextScaling.setText(String.format(mainActivity.getResources().getString(R.string.scale_is), 1f));

    }

    @Override
    public void lockInterface() {
        super.lockInterface();
        mSeekBarScaling.setEnabled(false);
        mResetScalingButton.setEnabled(false);
        mApplyScalingButton.setEnabled(false);
    }

    @Override
    public void unlockInterface() {
        super.unlockInterface();
        mSeekBarScaling.setEnabled(true);
        mResetScalingButton.setEnabled(true);
        mApplyScalingButton.setEnabled(true);
    }

    private void configResetButton(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(mainActivity.getBitmapBefore());
                mTextWidth.setText(String.format(mainActivity.getResources().getString(R.string.width_is), mainActivity.getBitmapBefore().getWidth()));
                mTextHeight.setText(String.format(mainActivity.getResources().getString(R.string.height_is), mainActivity.getBitmapBefore().getHeight()));
                mTextScaling.setText(String.format(mainActivity.getResources().getString(R.string.scale_is), 1));
                mSeekBarScaling.setProgress(100);
            }
        });
    }

    private void configApplyButton(final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("StaticFieldLeak")
                AsyncTaskConductor scalingAsync = new AsyncTaskConductor() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        Bitmap bufBitmap = algorithm((float) mScalingValue / 100);
                        if (bufBitmap.getHeight() > 3200 || bufBitmap.getWidth() > 3200){
                            mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mainActivity.getApplicationContext(),
                                            "Whoops! Your picture is bigger than our ImageView! " +
                                                    "Try again on smaller parameters.",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            bufBitmap = mainActivity.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
                        }
                        return bufBitmap;
                    }
                    @Override
                    protected void onPostExecute(final Bitmap result){
                        super.onPostExecute(result);
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextWidth.setText(String.format(mainActivity.getResources().getString(R.string.width_is), result.getWidth()));
                                mTextHeight.setText(String.format(mainActivity.getResources().getString(R.string.height_is), result.getHeight()));
                            }
                        });
                    }
                };
                scalingAsync.execute();
            }
        });
    }

    private void configScalingSeekBar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mScalingValue = progress;
                mTextScaling.setText(String.format(mainActivity.getResources().getString(R.string.scale_is), ((float) mScalingValue / 100)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                return;
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                return;
            }
        });
    }

    private Bitmap algorithm(float coef) {
        if (coef < 0.12) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity.getApplicationContext(),
                            mainActivity.getResources().getString(R.string.warning_scale), Toast.LENGTH_SHORT).show();
                }
            });
            return mainActivity.getBitmap();
        }
        int w = mainActivity.getBitmap().getWidth();
        int h = mainActivity.getBitmap().getHeight();

        // likewise in linear algebra
        if (coef < 1)
            return ColorFIltersCollection.
                resizeBilinear(mainActivity.getBitmap().copy(Bitmap.Config.ARGB_8888, true),
                        w, h, (int) (w * coef), (int) (h * coef));
        else
            return ColorFIltersCollection.resizeBicubic(
                    mainActivity.getBitmap().copy(Bitmap.Config.ARGB_8888, true), (int) (w * coef),
                    mainActivity.getApplicationContext());

    }
}

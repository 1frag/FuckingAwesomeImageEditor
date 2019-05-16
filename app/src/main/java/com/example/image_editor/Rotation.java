package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class Rotation extends Conductor {

    private Button mApplyRotateButton;
    private ImageButton mResetRotateButton;
    private ImageButton mRotate90Button;
    private ImageButton mMirrorVButton;
    private ImageButton mMirrorHButton;
    private ImageButton mCropButton;

    private SeekBar mSeekBarAngle;

    private TextView mTextViewAngle;

    private int mCurrentAngle = 0;
    private int mProgress = 45;

    Rotation(MainActivity activity) {
        super(activity);
    }

    @Override
    void touchToolbar() {
        super.touchToolbar();
        prepareToRun(R.layout.rotate_menu);
        setHeader("Rotation");

        mRotate90Button = mainActivity.findViewById(R.id.button_rotate90);
        mResetRotateButton = mainActivity.findViewById(R.id.button_reset_seekbar);
        mApplyRotateButton = mainActivity.findViewById(R.id.button_apply_rotate);
        mMirrorHButton = mainActivity.findViewById(R.id.button_mirrorH);
        mMirrorVButton = mainActivity.findViewById(R.id.button_mirrorV);
        mCropButton = mainActivity.findViewById(R.id.button_crop);

        mTextViewAngle = mainActivity.findViewById(R.id.text_angle);

        mSeekBarAngle = mainActivity.findViewById(R.id.seekbar_rotate);
        mSeekBarAngle.setProgress(45);
        mSeekBarAngle.setMax(90);

        configRotationSeekBar(mSeekBarAngle);
        configRotate90Button(mRotate90Button);
        configResetButton(mResetRotateButton);
        configApplyButton(mApplyRotateButton);
        configMirrorHorizontalButton(mMirrorHButton);
        configMirrorVerticalButton(mMirrorVButton);
        configCropButton(mCropButton);

        mTextViewAngle.setText("Angle: " + (mSeekBarAngle.getProgress() - 45));
    }

    @Override
    public void lockInterface() {
        super.lockInterface();
        mRotate90Button.setEnabled(false);
        mResetRotateButton.setEnabled(false);
        mSeekBarAngle.setEnabled(false);
        mApplyRotateButton.setEnabled(false);
    }

    @Override
    public void unlockInterface() {
        super.unlockInterface();
        mRotate90Button.setEnabled(true);
        mResetRotateButton.setEnabled(true);
        mSeekBarAngle.setEnabled(true);
        mApplyRotateButton.setEnabled(true);
    }

    private void configRotationSeekBar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                mProgress = progressValue;
                mTextViewAngle.setText("Angle: " + (mProgress - 45));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                return;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println(mProgress);
            }
        });

    }

    private void configResetButton(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(beforeChanges);
                mCurrentAngle = 0;
                mSeekBarAngle.setProgress(45);
                mTextViewAngle.setText("Angle: " + (mSeekBarAngle.getProgress() - 45));
            }
        });
    }

    private void configApplyButton(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskConductor asyncRotate = new AsyncTaskConductor() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        bitmap = rotateOnAngle(mProgress - 45);
                        return bitmap;
                    }
                };
                asyncRotate.execute();
            }
        });

    }

    private void configRotate90Button(final ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentAngle += 90;
                AsyncTaskConductor asyncRotate = new AsyncTaskConductor() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        bitmap = rotateOnAngle(mCurrentAngle);
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextViewAngle.setText("Angle: " + mCurrentAngle);
                            }
                        });
                        return bitmap;
                    }
                };
                asyncRotate.execute();
            }
        });
    }

    // TODO: below
    private void configMirrorHorizontalButton(ImageButton button) {

    }

    private void configMirrorVerticalButton(ImageButton button) {

    }

    private void configCropButton(ImageButton button) {

    }

    private DPoint getPoint23(int angle, double sina,
                              double x, double y,
                              double w, double h) {
        if (angle <= 90) return new DPoint(h * sina, 0);
        if (angle <= 180) return new DPoint(x, h * sina);
        if (angle <= 270) return new DPoint(x - h * sina, y);
        return new DPoint(0, y - h * sina);
    }

    private DPoint getPoint33(int angle, double sina,
                              double x, double y,
                              double w, double h) {
        if (angle <= 90) return new DPoint(x, w * sina);
        if (angle <= 180) return new DPoint(x - w * sina, y);
        if (angle <= 270) return new DPoint(0, y - w * sina);
        return new DPoint(w * sina, 0);
    }

    private Bitmap rotateOnAngle(int angle) {

        if (angle < 0) angle += 360;
        double a = (double) angle * Math.PI / 180.0;
        double sina = Math.sin(a);
        double cosa = Math.cos(a);

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int x = (int) (Math.abs((double) w * cosa) + Math.abs((double) h * sina) + 2);
        int y = (int) (Math.abs((double) w * sina) + Math.abs((double) h * cosa) + 2);

        DPoint p11 = new DPoint(w / 2.0, h / 2.0);
        DPoint p12 = new DPoint(0, 0);
        DPoint p13 = new DPoint(w, 0);
        DPoint p21 = new DPoint(x / 2.0, y / 2.0);
        DPoint p22 = getPoint23(angle, Math.abs(sina), x, y, w, h);
        DPoint p23 = getPoint33(angle, Math.abs(sina), x, y, w, h);

        ExecutorAffineTransformations solver;
        solver = new ExecutorAffineTransformations(
                p21, p22, p23, p11, p12, p13);

        if (!solver.prepare()) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity.getApplicationContext(),
                            "Points on one line", Toast.LENGTH_SHORT).show();
                }
            });
            return bitmap;
        }

        final Bitmap btmp = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        btmp.eraseColor(Color.WHITE);

        for (int i = 0; i < btmp.getWidth(); i++) {
            for (int j = 0; j < btmp.getHeight(); j++) {
                DPoint image = solver.calc(i, j);
                w = (int) Math.round(image.x);
                h = (int) Math.round(image.y);
                if (0 > w || w >= bitmap.getWidth()) continue;
                if (0 > h || h >= bitmap.getHeight()) continue;
                btmp.setPixel(i, j, bitmap.getPixel(w, h));
            }
        }

        return btmp;
    }
}

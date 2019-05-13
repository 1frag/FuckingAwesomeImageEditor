package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.View.OnTouchListener;

import static java.lang.Math.abs;

public class Retouch extends Conductor implements OnTouchListener {

    private Bitmap mBufferedBitmap;
    private Bitmap mBitmap;
    private Bitmap mMask;
    private Bitmap mOriginal;

    private Button mApplyRetouchButton;

    private Canvas mBufferCanvas;

    private TextView mTextViewBrushSize;
    private TextView mTextViewBlurRadius;

    private SeekBar mSeekBarBrushSize;
    private SeekBar mSeekBarBlurRadius;

    private ImageView mImageView;
    private MainActivity mainActivity;

    private int mBrushSize = 1;
    private int mBlurRadius = 1;

    Retouch(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        mainActivity = activity;
        mImageView = activity.getImageView();
    }

    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.retouch_menu);

        mApplyRetouchButton = mainActivity.findViewById(R.id.button_apply_retouch);

        mTextViewBrushSize = mainActivity.findViewById(R.id.text_brush);
        mTextViewBlurRadius = mainActivity.findViewById(R.id.text_radius);

        mSeekBarBlurRadius = mainActivity.findViewById(R.id.seek_bar_blur_radius);
        mSeekBarBlurRadius.setMax(20);

        mSeekBarBrushSize = mainActivity.findViewById(R.id.seek_bar_brush_sIze);
        mSeekBarBrushSize.setMax(50);

        configApplyButton(mApplyRetouchButton);
        configRadiusSeekBar(mSeekBarBlurRadius);
        configBrushSeekBar(mSeekBarBrushSize);

        mBitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        mBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mOriginal = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mBufferedBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mMask = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        mImageView.setImageBitmap(mBitmap);
        mImageView.setOnTouchListener(this);
    }

    private void configApplyButton(final Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskConductor asyncTask = new AsyncTaskConductor(){
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setEnabled(false);
                            }
                        });
                        algorithm();
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mImageView.setImageBitmap(mBitmap);
                                button.setEnabled(true);
                            }
                        });
                        return mBitmap;
                    }
                };
                asyncTask.execute();
            }
        });
    }

    private void configRadiusSeekBar(SeekBar seekBar){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                mBlurRadius = progresValue + 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                return;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mTextViewBlurRadius.setText("Radius: " + mBlurRadius);
                System.out.println(mBlurRadius);
            }
        });

    }

    private void configBrushSeekBar(SeekBar seekBar){
        mSeekBarBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                mBrushSize = progresValue + 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                return;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mTextViewBrushSize.setText("Brush: " + mBrushSize);
                System.out.println(mBrushSize);
            }
        });
    }

    private boolean canPutRect(int rad, int mx, int my) {
        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= mBitmap.getWidth() ||
                        0 > my + j || my + j >= mBitmap.getHeight()) {
                    continue;
                }
                if (abs(i) + abs(j) <= rad) {
                    if (mBitmap.getPixel(mx + i, my + j) == Color.rgb(10, 255, 10) ||
                            mBitmap.getPixel(mx + i, my + j) == Color.rgb(255, 10, 10)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void errorTouched() {
        // TODO: handle this
        return;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int mx = (int) event.getX();
        int my = (int) event.getY();

        int rad = mBrushSize;
        if (!canPutRect(rad, mx, my)) {
            errorTouched();
            return false;
        }

        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= mBitmap.getWidth()) {
                    continue;
                }
                if (0 > my + j || my + j >= mBitmap.getHeight()) {
                    continue;
                }
                if (Math.sqrt(i*i + j*j) <= rad) {
                    mBitmap.setPixel(mx + i, my + j, Color.RED);
                    mMask.setPixel(mx + i, my + j, Color.BLUE);
                }
            }
        }

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImageView.invalidate();
            }
        });

        return true;
    }

    private void algorithm(){
        // PorterDuff mode
        PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;

        Paint paintSRC_IN = new Paint();
        paintSRC_IN.setXfermode(new PorterDuffXfermode(mode));

        PorterDuff.Mode mode2 = PorterDuff.Mode.DST_ATOP;

        Paint paintATOP = new Paint();
        paintATOP.setXfermode(new PorterDuffXfermode(mode2));

        // blurred mBitmap
        mBufferedBitmap = ColorFIltersCollection.fastBlur(mOriginal, mBlurRadius, 1);

        mBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mBufferCanvas = new Canvas(mBitmap);

        mBufferCanvas.drawBitmap(mMask, 0, 0, null);
        mBufferCanvas.drawBitmap(mBufferedBitmap, 0, 0, paintSRC_IN);
        mBufferCanvas.drawBitmap(mOriginal, 0, 0, paintATOP);
    }
}

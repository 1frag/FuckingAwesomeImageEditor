package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import static java.lang.Math.abs;

public class Retouch extends Conductor implements OnTouchListener {

    /* I'm scared to touch this bitmaps */
    private Bitmap mBufferedBitmap;
    private Bitmap mMask;
    private Bitmap mOriginal;

    private Button mApplyRetouchButton;

    private Canvas mBufferCanvas;

    private TextView mTextViewBrushSize;
    private TextView mTextViewBlurRadius;

    private SeekBar mSeekBarBrushSize;
    private SeekBar mSeekBarBlurRadius;

    private int mBrushSize = 1;
    private int mBlurRadius = 1;

    Retouch(MainActivity activity) {
        super(activity);
    }

    @Override
    void touchToolbar() {
        super.touchToolbar();
        prepareToRun(R.layout.retouch_menu);
        setHeader(mainActivity.getResources().getString(R.string.retouch));

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

        mOriginal = mainActivity.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
        mBufferedBitmap = mainActivity.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
        mMask = Bitmap.createBitmap(mainActivity.getBitmap().getWidth(),
                mainActivity.getBitmap().getHeight(),
                Bitmap.Config.ARGB_8888);

//        imageView.setImageBitmap(mainActivity.getBitmap()); todo: check
        imageView.setOnTouchListener(this);
    }

    @Override
    public void lockInterface(){
        super.lockInterface();
        mApplyRetouchButton.setEnabled(false);
        mSeekBarBlurRadius.setEnabled(false);
        mSeekBarBrushSize.setEnabled(false);
    }

    @Override
    public void unlockInterface(){
        super.unlockInterface();
        mApplyRetouchButton.setEnabled(true);
        mSeekBarBlurRadius.setEnabled(true);
        mSeekBarBrushSize.setEnabled(true);
    }

    private void configApplyButton(final Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskConductor asyncTask = new AsyncTaskConductor(){
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        algorithm();
                        return mainActivity.getBitmap();
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
                String txt = mainActivity.getResources().getString(R.string.radius_is);
                mTextViewBlurRadius.setText(String.format(txt, mBlurRadius));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                return;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println(mBlurRadius);
            }
        });

    }

    private void configBrushSeekBar(SeekBar seekBar){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                mBrushSize = progresValue + 1;
                String txt = mainActivity.getResources().getString(R.string.brush_is);
                mTextViewBrushSize.setText(String.format(txt, mBrushSize));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                return;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println(mBlurRadius);
            }
        });
    }

    private boolean canPutRect(int rad, int mx, int my) {
        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= mainActivity.getBitmap().getWidth() ||
                        0 > my + j || my + j >= mainActivity.getBitmap().getHeight()) {
                    continue;
                }
                if (abs(i) + abs(j) <= rad) {
                    if (mainActivity.getBitmap().getPixel(mx + i, my + j) == Color.rgb(10, 255, 10) ||
                            mainActivity.getBitmap().getPixel(mx + i, my + j) == Color.rgb(255, 10, 10)) {
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

        float scalingX = imageView.getWidth() / (float) mainActivity.getBitmap().getWidth();
        float scalingY = imageView.getHeight() / (float) mainActivity.getBitmap().getHeight();
        int mx = (int) (event.getX() / scalingX);
        int my = (int) (event.getY() / scalingY);

        int rad = mBrushSize;
        if (!canPutRect(rad, mx, my)) {
            errorTouched();
            return false;
        }

        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= mainActivity.getBitmap().getWidth()) {
                    continue;
                }
                if (0 > my + j || my + j >= mainActivity.getBitmap().getHeight()) {
                    continue;
                }
                if (Math.sqrt(i*i + j*j) <= rad) {
                    mainActivity.getBitmap().setPixel(mx + i, my + j, Color.RED);
                    mMask.setPixel(mx + i, my + j, Color.BLUE);
                }
            }
        }

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.invalidate();
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

        mBufferCanvas = new Canvas(mainActivity.getBitmap());

        mBufferCanvas.drawBitmap(mMask, 0, 0, null);
        mBufferCanvas.drawBitmap(mBufferedBitmap, 0, 0, paintSRC_IN);
        mBufferCanvas.drawBitmap(mOriginal, 0, 0, paintATOP);
    }
}

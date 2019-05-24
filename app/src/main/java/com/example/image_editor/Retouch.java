package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import static java.lang.Math.abs;

public class Retouch extends Controller implements OnTouchListener {

    private ArrayList<Pixel> mRemPixels = new ArrayList<>();

    private Button mApplyRetouchButton;
    private ImageButton mClearButton;

    private TextView mTextViewBrushSize;
    private TextView mTextViewBlurRadius;

    private SeekBar mSeekBarBrushSize;
    private SeekBar mSeekBarBlurRadius;

    private Canvas mCanvas;
    private Paint mPaint;

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
        mClearButton = mainActivity.findViewById(R.id.clear_btn);

        mTextViewBrushSize = mainActivity.findViewById(R.id.text_brush);
        mTextViewBlurRadius = mainActivity.findViewById(R.id.text_radius);

        mSeekBarBlurRadius = mainActivity.findViewById(R.id.seek_bar_blur_radius);
        mSeekBarBlurRadius.setMax(20);

        mSeekBarBrushSize = mainActivity.findViewById(R.id.seek_bar_brush_sIze);
        mSeekBarBrushSize.setMax(50);

        configApplyButton(mApplyRetouchButton);
        configRadiusSeekBar(mSeekBarBlurRadius);
        configBrushSeekBar(mSeekBarBrushSize);
        configClearButton(mClearButton);

        mainActivity.resetDrawing();
        imageView.setImageBitmap(mainActivity.getBitmapDrawing());

        mCanvas = new Canvas(mainActivity.getBitmapDrawing());
        mPaint = new Paint();
        mPaint.setColor(0x55FF0000); // RED

        imageView.setOnTouchListener(this);
    }

    @Override
    public void lockInterface() {
        super.lockInterface();
        mApplyRetouchButton.setEnabled(false);
        mClearButton.setEnabled(false);
        mSeekBarBlurRadius.setEnabled(false);
        mSeekBarBrushSize.setEnabled(false);
        imageView.setOnTouchListener(null);
    }

    @Override
    public void unlockInterface() {
        super.unlockInterface();
        mApplyRetouchButton.setEnabled(true);
        mClearButton.setEnabled(true);
        mSeekBarBlurRadius.setEnabled(true);
        mSeekBarBrushSize.setEnabled(true);
        imageView.setOnTouchListener(this);
    }

    private void configApplyButton(final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskConductor asyncTask = new AsyncTaskConductor() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        algorithm();
                        return mainActivity.getBitmapDrawing();
                    }
                    @Override
                    protected void onPostExecute(Bitmap result){
                        super.onPostExecute(result);
                        // это не костыль, это логика (с)
                        mainActivity.resetDrawing();
                        imageView.setImageBitmap(mainActivity.getBitmapDrawing());
                        mainActivity.invalidateImageView();
                        mainActivity.imageChanged = false;
                        mCanvas.setBitmap(mainActivity.getBitmapDrawing());
                        mRemPixels.clear();
                    }
                };
                asyncTask.execute();
            }
        });
    }

    private void configClearButton(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.resetDrawing();
                imageView.setImageBitmap(mainActivity.getBitmapDrawing());
                mainActivity.invalidateImageView();
                mainActivity.imageChanged = false;
                mCanvas.setBitmap(mainActivity.getBitmapDrawing());
                mRemPixels.clear();
            }
        });
    }

    private void configRadiusSeekBar(SeekBar seekBar) {
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

    private void configBrushSeekBar(SeekBar seekBar) {
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
        mCanvas.drawCircle(mx, my, rad, mPaint);

        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= mainActivity.getBitmap().getWidth()) {
                    continue;
                }
                if (0 > my + j || my + j >= mainActivity.getBitmap().getHeight()) {
                    continue;
                }
                if (Math.sqrt(i * i + j * j) <= rad) {
                    mRemPixels.add(new Pixel(mx + i, my + j,
                            mainActivity.getBitmap()
                                    .getPixel(mx + i, my + j)));
                }
            }
        }

        mainActivity.imageChanged = true;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.invalidate();
            }
        });
        return true;
    }

    private void algorithm() {
        // blurred bitmap
        Bitmap bufferedBitmap = ColorFIltersCollection.fastBlur(
                mainActivity.getBitmapBefore().copy(
                        Bitmap.Config.ARGB_8888, true), mBlurRadius, 1);

        for (int i = 0; i < mRemPixels.size(); i++) {
            Pixel e = mRemPixels.get(i);
            mainActivity.getBitmap().setPixel(e.getX(),
                    e.getY(), bufferedBitmap.getPixel(e.getX(),
                            e.getY()));
        }
        mainActivity.resetDrawing();
        mRemPixels.clear();

        mainActivity.invalidateImageView();
        mainActivity.imageChanged = false;
        mainActivity.algorithmExecuted = true;
    }
}

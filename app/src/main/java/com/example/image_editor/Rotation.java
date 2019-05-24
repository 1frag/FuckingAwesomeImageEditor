package com.example.image_editor;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Rotation extends Controller implements View.OnTouchListener {

    private Button mApplyRotateButton;
    private ImageButton mResetRotateButton;
    private ImageButton mRotate90Button;
    private ImageButton mMirrorVButton;
    private ImageButton mMirrorHButton;
    private ImageButton mCropButton;
    private ImageButton mClearCropButton;

    private SeekBar mSeekBarAngle;

    private TextView mTextViewAngle;

    private ArrayList<DPoint> mPointsArray = new ArrayList<>();

    private int mCurrentAngleDiv90 = 0;
    private int mCurrentAngleMod90 = 0;

    private boolean mCropOption = false;

    private Canvas mCanvas; // for border drawing
    private Paint mPaint;
    private Bitmap mBufferBitmap; // for correct crop

    Rotation(MainActivity activity) {
        super(activity);
    }

    @Override
    void touchToolbar() {
        super.touchToolbar();
        prepareToRun(R.layout.rotate_menu);
        setHeader(mainActivity.getResources().getString(R.string.name_rotation));

        mRotate90Button = mainActivity.findViewById(R.id.button_rotate90);
        mResetRotateButton = mainActivity.findViewById(R.id.button_reset_seekbar);
        mApplyRotateButton = mainActivity.findViewById(R.id.button_apply_rotate);
        mMirrorHButton = mainActivity.findViewById(R.id.button_mirrorH);
        mMirrorVButton = mainActivity.findViewById(R.id.button_mirrorV);
        mCropButton = mainActivity.findViewById(R.id.button_crop);
        mClearCropButton = mainActivity.findViewById(R.id.button_clear_crop);

        mTextViewAngle = mainActivity.findViewById(R.id.text_angle);

        mSeekBarAngle = mainActivity.findViewById(R.id.seekbar_rotate);
        mSeekBarAngle.setProgress(90);
        mSeekBarAngle.setMax(180);

        mBufferBitmap = mainActivity.getBitmap().copy(Bitmap.Config.ARGB_8888, true);

        configRotationSeekBar(mSeekBarAngle);
        configRotate90Button(mRotate90Button);
        configResetButton(mResetRotateButton);
        configApplyButton(mApplyRotateButton);
        configMirrorHorizontalButton(mMirrorHButton);
        configMirrorVerticalButton(mMirrorVButton);
        configCropButton(mCropButton);
        configClearCropButton(mClearCropButton);

        String txt = mainActivity.getResources().getString(R.string.angle_is);
        mTextViewAngle.setText(String.format(txt, getCurrentAngle()));
    }

    @Override
    public void lockInterface() {
        super.lockInterface();
        mRotate90Button.setEnabled(false);
        mResetRotateButton.setEnabled(false);
        mSeekBarAngle.setEnabled(false);
        mApplyRotateButton.setEnabled(false);
        mMirrorHButton.setEnabled(false);
        mMirrorVButton.setEnabled(false);
        mCropButton.setEnabled(false);
    }

    @Override
    public void unlockInterface() {
        super.unlockInterface();
        mRotate90Button.setEnabled(true);
        mResetRotateButton.setEnabled(true);
        mSeekBarAngle.setEnabled(true);
        mApplyRotateButton.setEnabled(true);
        mMirrorHButton.setEnabled(true);
        mMirrorVButton.setEnabled(true);
        mCropButton.setEnabled(true);
    }

    private void configRotationSeekBar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                if (!fromUser) return;
                mCurrentAngleMod90 = progressValue - 90;
                String txt = mainActivity.getResources().getString(R.string.angle_is);
                mTextViewAngle.setText(String.format(txt, getCurrentAngle()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println(mCurrentAngleMod90);
            }
        });

    }

    private void configResetButton(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.getImageView().setImageBitmap(mainActivity.getBitmapBefore());
                mCurrentAngleDiv90 = 0;
                mCurrentAngleMod90 = 0;
                mSeekBarAngle.setProgress(90);
                String txt = mainActivity.getResources().getString(R.string.angle_is);
                mTextViewAngle.setText(String.format(txt, 0));
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configApplyButton(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("StaticFieldLeak")
                AsyncTaskConductor asyncRotate = new AsyncTaskConductor() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
//                        mainActivity.resetBitmap();
                        Bitmap bufBitmap = rotateOnAngle(getCurrentAngle());
                        mBufferBitmap = bufBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        return bufBitmap;
                    }
                };
                if (mCropOption){
                    Log.i("msg", Integer.toString(mPointsArray.size()));
                    mainActivity.getImageView().setOnTouchListener(null);
                    mCropOption = false;

                    if (mPointsArray.size() < 2){
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mainActivity.getApplicationContext(),
                                        "You don't set any points, dude",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        mPointsArray.clear();
                        return;
                    }

                    final DPoint dot1 = mPointsArray.get(0);
                    final DPoint dot2 = mPointsArray.get(1);
                    @SuppressLint("StaticFieldLeak")
                    AsyncTaskConductor asyncTask = new AsyncTaskConductor() {
                        @Override
                        protected Bitmap doInBackground(String... params) {
                            Bitmap bufBitmap = cropAlgo(dot1, dot2);
                            return bufBitmap;
                        }
                        @Override
                        protected void onPostExecute(Bitmap result){
                            super.onPostExecute(result);
                            mainActivity.getImageView().setImageBitmap(result);
                            mainActivity.setBitmapFromImageView();
                            mainActivity.invalidateImageView();
                        }
                    };
                    asyncTask.execute();
                    mPointsArray.clear();
                } else asyncRotate.execute();
            }
        });

    }

    private void configRotate90Button(final ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentAngleDiv90++;
                mCurrentAngleDiv90 %= 4;
                String txt = mainActivity.getResources().getString(R.string.angle_is);
                mTextViewAngle.setText(String.format(txt, getCurrentAngle()));
            }
        });
    }

    private void configMirrorHorizontalButton(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("StaticFieldLeak")
                AsyncTaskConductor asyncTask = new AsyncTaskConductor() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        mainActivity.setBitmap(horizontalSymmetry());
                        mBufferBitmap = mainActivity.getBitmap();
                        return mainActivity.getBitmap();
                    }
                };
                asyncTask.execute();
            }
        });
    }

    private void configMirrorVerticalButton(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("StaticFieldLeak")
                AsyncTaskConductor asyncTask = new AsyncTaskConductor() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        mainActivity.setBitmap(verticalSymmetry());
                        mBufferBitmap = mainActivity.getBitmap();
                        return mainActivity.getBitmap();
                    }
                };
                asyncTask.execute();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configCropButton(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCropOption) {
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mainActivity.getApplicationContext(),
                                    "Set two points on picture to crop and click on apply button",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                    mainActivity.getImageView().setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            float scalingX = mainActivity.getImageView().getWidth() /
                                    (float) mainActivity.getBitmap().getWidth();
                            float scalingY = mainActivity.getImageView().getHeight() /
                                    (float) mainActivity.getBitmap().getHeight();
                            final int mx = (int) (event.getX() / scalingX);
                            final int my = (int) (event.getY() / scalingY);

                            mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mPointsArray.size() < 2) {
                                        mPointsArray.add(new DPoint(mx, my));
                                        drawCircle(mx, my, 15, Color.BLACK);
                                        mainActivity.invalidateImageView();
                                    }
                                    if (mPointsArray.size() == 2) drawCropBorder();
                                }
                            });

                            mainActivity.imageChanged = true;

                            Log.i("msg", mx + " " + my);
                            return false;
                        }
                    });
                    mCropOption = true;
                }
            }
        });
    }

    private void configClearCropButton(ImageButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("StaticFieldLeak")
                AsyncTaskConductor asyncTask = new AsyncTaskConductor(){
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        return mBufferBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    }
                };
                asyncTask.execute();

                mPointsArray.clear();
                mainActivity.imageChanged = false;
            }
        });
    }

    private int getCurrentAngle() {

        int cur = (mCurrentAngleDiv90 * 90 + mCurrentAngleMod90 + 3600) % 360;
        if (cur > 180) return cur - 360;
        else return cur;
    }

    private DPoint getPoint23(int angle, double x, double y,
                              double w, double h) {
        double a = (double) (angle % 90) * Math.PI / 180.0;
        double sina = Math.sin(a);
        double cosa = Math.cos(a);

        if (angle < 90) return new DPoint(0, w * sina);
        if (angle < 180) return new DPoint(w * sina, y);
        if (angle < 270) return new DPoint(x, y - w * sina);
        return new DPoint(x - w * sina, 0);
    }

    private DPoint getPoint33(int angle, double x, double y,
                              double w, double h) {
        double a = (double) (angle % 90) * Math.PI / 180.0;
        double sina = Math.sin(a);
        double cosa = Math.cos(a);

        if (angle < 90) return new DPoint(w * cosa, 0);
        if (angle < 180) return new DPoint(0, y - w * cosa);
        if (angle < 270) return new DPoint(x - w * cosa, y);
        return new DPoint(x, w * cosa);
    }

    private Bitmap cropAlgo(DPoint point1, DPoint point2){
        int startX = (point1.x > point2.x) ? (int)point2.x : (int)point1.x;
        int startY = (point1.y > point2.y) ? (int)point2.y : (int)point1.y;
        int finishX = (point1.x >= point2.x) ? (int)point1.x : (int)point2.x;
        int finishY = (point1.y >= point2.y) ? (int)point1.y : (int)point2.y;

        Bitmap bufBitmap = Bitmap.createBitmap(finishX - startX,
                finishY - startY, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < bufBitmap.getWidth(); i++)
            for (int j = 0; j < bufBitmap.getHeight(); j++)
                bufBitmap.setPixel(i, j, mBufferBitmap.getPixel(startX+i, startY+j));
        return bufBitmap;
    }

    private Bitmap rotateOnAngle(int angle) {
        if (angle < 0) angle += 360;

        double a = (double) angle * Math.PI / 180.0;
        double sina = Math.sin(a);
        double cosa = Math.cos(a);

        int w = mainActivity.getBitmap().getWidth();
        int h = mainActivity.getBitmap().getHeight();

        int x = (int) (Math.abs((double) w * cosa) + Math.abs((double) h * sina) + 2);
        int y = (int) (Math.abs((double) w * sina) + Math.abs((double) h * cosa) + 2);

        DPoint p11 = new DPoint(w / 2.0, h / 2.0);
        DPoint p12 = new DPoint(0, 0);
        DPoint p13 = new DPoint(w, 0);
        DPoint p21 = new DPoint(x / 2.0, y / 2.0);
        DPoint p22 = getPoint23(angle, x, y, w, h);
        DPoint p23 = getPoint33(angle, x, y, w, h);

        Log.i("upd", String.format("1) %s %s", p22.x, p22.y));
        Log.i("upd", String.format("2) %s %s", p23.x, p23.y));
        Log.i("upd", String.format("angle = %s", angle));

        ExecutorAffineTransformations solver;
        solver = new ExecutorAffineTransformations(
                p21, p22, p23, p11, p12, p13);

        if (!solver.prepare()) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity.getApplicationContext(),
                            mainActivity.getResources().getString(R.string.points_on_one_line),
                            Toast.LENGTH_SHORT).show();
                }
            });
            return mainActivity.getBitmap();
        }

        final Bitmap btmp = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        btmp.eraseColor(Color.WHITE);

        for (int i = 0; i < btmp.getWidth(); i++) {
            for (int j = 0; j < btmp.getHeight(); j++) {
                DPoint image = solver.calc(i, j);
                w = (int) Math.round(image.x);
                h = (int) Math.round(image.y);
                if (0 > w || w >= mainActivity.getBitmap().getWidth()) continue;
                if (0 > h || h >= mainActivity.getBitmap().getHeight()) continue;
                btmp.setPixel(i, j, mainActivity.getBitmap().getPixel(w, h));
            }
        }

        return btmp;
    }

    private Bitmap verticalSymmetry() {
        Bitmap orig = mainActivity.getBitmap();
        Bitmap bufBitmap = orig.copy(Bitmap.Config.ARGB_8888, true);

        int w = bufBitmap.getWidth();
        int h = bufBitmap.getHeight();

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int p = orig.getPixel(i, j);
                bufBitmap.setPixel(w - i - 1, j, p);
            }
        }
        return bufBitmap;
    }

    private Bitmap horizontalSymmetry() {
        Bitmap orig = mainActivity.getBitmap();
        Bitmap bufBitmap = orig.copy(Bitmap.Config.ARGB_8888, true);

        int w = bufBitmap.getWidth();
        int h = bufBitmap.getHeight();

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int p = orig.getPixel(i, j);
                bufBitmap.setPixel(i, h - j - 1, p);
            }
        }
        return bufBitmap;
    }

    private void drawCircle(int mx, int my, int r, int color) {
        for (int x = mx - r; x <= mx + r; x++) {
            for (int y = my - r; y <= my + r; y++) {
                if ((x - mx) * (x - mx) + (y - my) * (y - my) <= r * r) {
                    if (0 > x || x >= mainActivity.getBitmap().getWidth()) continue;
                    if (0 > y || y >= mainActivity.getBitmap().getHeight()) continue;
                    mainActivity.setPixelBitmap(x, y, color);
                    mainActivity.imageChanged = true;
                }
            }
        }
    }

    private void drawCropBorder(){
        mCanvas = new Canvas(mainActivity.getBitmap());
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(9);

        int x1 =(int) mPointsArray.get(0).x;
        int x2 =(int) mPointsArray.get(1).x;
        int y1 =(int) mPointsArray.get(0).y;
        int y2 =(int) mPointsArray.get(1).y;

        int dX = Math.abs(x1-x2);
        int dY = Math.abs(y1-y2);

        if (y1 < y2){
            mCanvas.drawLine(x1, y1, x1, y1+dY, mPaint);
            mCanvas.drawLine(x2, y2, x2, y2-dY, mPaint);
        }
        else{
            mCanvas.drawLine(x1, y1, x1, y1-dY, mPaint);
            mCanvas.drawLine(x2, y2, x2, y2+dY, mPaint);
        }

        if (x1 < x2){
            mCanvas.drawLine(x1, y1, x1+dX, y1, mPaint);
            mCanvas.drawLine(x2, y2, x2-dX, y2, mPaint);
        }
        else{
            mCanvas.drawLine(x1, y1, x1-dX, y1, mPaint);
            mCanvas.drawLine(x2, y2, x2+dX, y2, mPaint);
        }
    }

    // suppress the warning
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}

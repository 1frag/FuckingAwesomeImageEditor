package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class Algem extends Controller implements View.OnTouchListener {

    private ImageButton mAddPointsButton;
    private ImageButton mClearButton;
    private Button mStartAlgemButton;

    private Canvas mCanvas;
    private Paint mPaint;

    private ArrayList<DPoint> mPointsArray = new ArrayList<>();
    private DPoint mPreviousPoint = null;

    private int N, mTypeEvent;

    Algem(MainActivity activity) {
        super(activity);
    }

    @Override
    void touchToolbar() {
        super.touchToolbar();
        prepareToRun(R.layout.spline_menu);
        setHeader(mainActivity.getResources().getString(R.string.interpolation_slines));

        mStartAlgemButton = mainActivity.findViewById(R.id.button_start_splain);
        mAddPointsButton = mainActivity.findViewById(R.id.button_add_points);
        mClearButton = mainActivity.findViewById(R.id.clear_btn);

        configDrawPointsButton(mAddPointsButton);
        configStartAlgoButton(mStartAlgemButton);
        configClearButton(mClearButton);

        imageView.setImageBitmap(mainActivity.getBitmap());
        mCanvas = new Canvas(mainActivity.getBitmap());

        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(12);

        imageView.setOnTouchListener(this);
    }

    @Override
    public void lockInterface() {
        super.lockInterface();
        mAddPointsButton.setEnabled(false);
        mStartAlgemButton.setEnabled(false);
        mClearButton.setEnabled(false);
        imageView.setOnTouchListener(null);
    }

    @Override
    public void unlockInterface() {
        super.unlockInterface();
        mAddPointsButton.setEnabled(true);
        mStartAlgemButton.setEnabled(true);
        mClearButton.setEnabled(true);
        imageView.setOnTouchListener(this);
    }

    private void configDrawPointsButton(ImageButton btn1) {
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPoint(v);
            }
        });
    }

    private void configStartAlgoButton(final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskConductor splainTask = new AsyncTaskConductor() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        algorithm();
                        return mainActivity.getBitmap();
                    }
                    @Override
                    protected void onPostExecute(Bitmap result){
                        super.onPostExecute(result);
                        // to resume lines drawing
                        imageView.setImageBitmap(mainActivity.getBitmap());
                        mainActivity.invalidateImageView();
                        mainActivity.imageChanged = false;
                        mCanvas.setBitmap(mainActivity.getBitmap());
                    }
                };
                splainTask.execute();
            }
        });
    }

    private void configClearButton(ImageButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(mainActivity.getBitmapBefore());
                mainActivity.setBitmapFromImageview();
                mPointsArray.clear();
                mPreviousPoint = null;
                mainActivity.imageChanged = false;
            }
        });
    }

    // legacy code below :D
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        float scalingX = imageView.getWidth() / (float) mainActivity.getBitmap().getWidth();
        float scalingY = imageView.getHeight() / (float) mainActivity.getBitmap().getHeight();
        int mx = (int) (event.getX() / scalingX);
        int my = (int) (event.getY() / scalingY);

        if (mTypeEvent == 1 && event.getAction() == 0) {
            drawCircle(mx, my, 15, Color.BLACK);
            if (mPreviousPoint != null){
                mCanvas.drawLine((float)mPreviousPoint.x, (float)mPreviousPoint.y, (float)mx, (float)my, mPaint);
            }
            mPreviousPoint = new DPoint(mx, my);
            mPointsArray.add(new DPoint(mx, my));
            mainActivity.invalidateImageView();
            mainActivity.imageChanged = true;
            return true;
        }
        return false;
    }

    private void drawCircle(int mx, int my, int r, int color) {
        for (int x = mx - r; x <= mx + r; x++) {
            for (int y = my - r; y <= my + r; y++) {
                if ((x - mx) * (x - mx) + (y - my) * (y - my) <= r * r) {
                    if (0 > x || x >= mainActivity.getWidthBitmap()) continue;
                    if (0 > y || y >= mainActivity.getHeightBitmap()) continue;
                    mainActivity.setPixelBitmap(x, y, color);
                    mainActivity.imageChanged = true;
                }
            }
        }
    }

    public void setPoint(View view) {
        mTypeEvent = 1;
    }

    private DPoint pntSum(DPoint a, DPoint b) {
        return new DPoint(a.x + b.x, a.y + b.y);
    }

    private DPoint pntMul(double a, DPoint b) {
        return new DPoint(a * b.x, a * b.y);
    }

    private double a(int ind) {
        if (ind == 0) return 0;
        if (ind == N - 1) return 2;
        return 1;
    }

    private double b(int ind) {
        if (ind == 0) return 2;
        if (ind == N - 1) return 7;
        return 4;
    }

    private double c(int ind) {
        if (ind == 0) return 1;
        if (ind == N - 1) return 0;
        return 1;
    }

    private ArrayList<DPoint> thomasAlgorithm() {
        ArrayList<DPoint> D = new ArrayList<>();
        ArrayList<Double> B = new ArrayList<>();
        ArrayList<DPoint> dX = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            dX.add(new DPoint(0, 0));
            B.add(0.0);
        }
        B.set(0, 2.0);
        B.add(0.0);

        D.add(pntSum(mPointsArray.get(0), pntMul(2, mPointsArray.get(1))));
        for (int i = 1; i < N - 1; i++) {
            D.add(pntSum(pntMul(4, mPointsArray.get(i)), pntMul(2, mPointsArray.get(i + 1))));
        }
        D.add(pntSum(pntMul(8, mPointsArray.get(N - 1)), mPointsArray.get(N)));

        for (int i = 1; i < N; i++) {
            double m = a(i) / b(i - 1);
            B.set(i, b(i) - m * c(i - 1));
            D.set(i, pntSum(D.get(i), pntMul(-m, D.get(i - 1))));
        }
        //D(N) / B(N)
        double xnnx = D.get(N - 1).x / B.get(N - 1);
        double xnny = D.get(N - 1).y / B.get(N - 1);
        dX.set(N - 1, new DPoint(xnnx, xnny));
        for (int i = N - 2; i >= 0; i--) {
            //X(i) = (D(i) - C(i) * X(i + 1)) / B(i)
            xnnx = (D.get(i).x - c(i) * dX.get(i + 1).x) / B.get(i);
            xnny = (D.get(i).y - c(i) * dX.get(i + 1).y) / B.get(i);
            dX.set(i, new DPoint(xnnx, xnny));
        }
        return dX;
    }

    private void algorithm() {
        // in first we have:
        //
        // 2 * P(1, 0  ) + 1 * P(1, 1 )                 = mPointsArray(0)+2*mPointsArray(1)
        // 1 * P(1, i-1) + 4 * P(1, i ) + 1 * P(1, i+1) = 4*mPointsArray(i)+2*mPointsArray(i+1), for i in [1, N-2]
        //                 2 * P(1,N-2) + 7 * P(1, N-1) = 8*mPointsArray(N-1)+mPointsArray(N)
        N = mPointsArray.size() - 1;
        ArrayList<DPoint> P1;
        try {
            P1 = thomasAlgorithm();
        } catch (IndexOutOfBoundsException e) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // todo: а не сдохнет?) (*mainActivity.runOnUiThread*)
                    // TODO: а почему должен? :D
                    Toast.makeText(mainActivity.getApplicationContext(), mainActivity.getResources().getString(R.string.warning_no_points), Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        ArrayList<DPoint> P2 = otherMatem(P1);
        drawSpline(P1, P2);
    }

    private void clearGap() {
        mainActivity.resetBitmap();

        for (int i = 0; i < mPointsArray.size(); i++) {
            int mx = (int) mPointsArray.get(i).x;
            int my = (int) mPointsArray.get(i).y;
            drawCircle(mx, my, 15, Color.BLACK);
        }
    }

    private void drawSpline(ArrayList<DPoint> p1, ArrayList<DPoint> p2) {
        clearGap();
        for (int i = 0; i < N; i++) {
            int step = 1000;
            for (int j = 0; j <= step; j++) {
                // B(t) = (1-t)^3 * P0 + 3*t(t-1)^2*P1 + 3*t^2*(t-1)*P2 + t^3*P3
                double t = (double) j / step;
                DPoint r1 = pntMul((1 - t) * (1 - t) * (1 - t), mPointsArray.get(i));
                DPoint r2 = pntMul((1 - t) * (1 - t) * t, p1.get(i));
                DPoint r3 = pntMul((1 - t) * t * t, p2.get(i));
                DPoint r4 = pntMul(t * t * t, mPointsArray.get(i + 1));
                int nx = (int) (r1.x + 3 * r2.x + 3 * r3.x + r4.x);
                int ny = (int) (r1.y + 3 * r2.y + 3 * r3.y + r4.y);
                if (nx < 0 || nx >= mainActivity.getWidthBitmap()) continue;
                if (ny < 0 || ny >= mainActivity.getHeightBitmap()) continue;
//                mBitmap.setPixel(nx, ny, Color.BLACK);
                drawCircle(nx, ny, 5, Color.BLACK);
            }
        }
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.invalidate();
            }
        });
    }

    private ArrayList<DPoint> otherMatem(ArrayList<DPoint> P1) {
        ArrayList<DPoint> answer = new ArrayList<>();
        for (int i = 0; i <= N - 2; i++) {
            double nx = 2 * mPointsArray.get(i + 1).x - P1.get(i + 1).x;
            double ny = 2 * mPointsArray.get(i + 1).y - P1.get(i + 1).y;
            answer.add(new DPoint(nx, ny));
        }
        double nx = (mPointsArray.get(N).x + P1.get(N - 1).x) / 2;
        double ny = (mPointsArray.get(N).y + P1.get(N - 1).y) / 2;
        answer.add(new DPoint(nx, ny));
        return answer;
    }

    public void changeside(View view) {
        // todo: xepnZ hOW Tu du?? -0_0-
    }
}

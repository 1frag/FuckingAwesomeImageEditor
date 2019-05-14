package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class Algem extends Conductor implements View.OnTouchListener {

    private ImageButton mAddPointsButton;
    private Button mStartAlgemButton;
    // TODO: button for lines

    private ArrayList<DPoint> mPointsArray = new ArrayList<>();

    private int N, mTypeEvent;

    Algem(MainActivity activity) {
        super(activity);
    }

    @Override
    void touchToolbar() {
        super.touchToolbar();
        prepareToRun(R.layout.spline_menu);
        setHeader("Splines and lines");

        mStartAlgemButton = mainActivity.findViewById(R.id.button_start_splain);
        mAddPointsButton = mainActivity.findViewById(R.id.button_add_points);

        configDrawPointsButton(mAddPointsButton);
        configStartAlgoButton(mStartAlgemButton);

        imageView.setOnTouchListener(this);
    }

    @Override
    public void lockInterface(){
        super.lockInterface();
        mAddPointsButton.setEnabled(false);
        mStartAlgemButton.setEnabled(false);
    }

    @Override
    public void unlockInterface(){
        super.unlockInterface();
        mAddPointsButton.setEnabled(true);
        mStartAlgemButton.setEnabled(true);
    }

    private void configDrawPointsButton(ImageButton btn1) {
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setpoint(v);
            }
        });
    }

    private void configStartAlgoButton(final Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskConductor splainTask = new AsyncTaskConductor(){
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        algorithm();
                        return bitmap;
                    }
                };
                splainTask.execute();
            }
        });
    }

    // legacy code below :D
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int mx = (int) event.getX();
        int my = (int) event.getY();
        if (mTypeEvent == 1 && event.getAction() == 0) {

//            View npoint = new View(mainActivity);
//            npoint.findViewById(R.id.mv_point);
//            npoint.setVisibility(View.VISIBLE);
//            npoint.setLayoutParams(new LinearLayout.LayoutParams(30, 30));

            DrawCircle(mx, my, 15, Color.BLACK);
            mPointsArray.add(new DPoint(mx, my));
            imageView.invalidate();
            return true;
        }
        return false;
    }

    private void DrawCircle(int mx, int my, int r, int color) {
        for (int x = mx - r; x <= mx + r; x++) {
            for (int y = my - r; y <= my + r; y++) {
                if ((x - mx) * (x - mx) + (y - my) * (y - my) <= r * r){
                    if(0>x || x>= bitmap.getWidth())continue;
                    if(0>y || y>= bitmap.getHeight())continue;
                    bitmap.setPixel(x, y, color);
                }
            }
        }
    }

    public void setpoint(View view) {
        mTypeEvent = 1;
    }

    public DPoint pntSum(DPoint a, DPoint b) {
        return new DPoint(a.x + b.x, a.y + b.y);
    }

    public DPoint pntMul(double a, DPoint b) {
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

    private ArrayList<DPoint> Thomas_algorithm() {
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
        try{
            P1 = Thomas_algorithm();
        } catch (IndexOutOfBoundsException e){
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity.getApplicationContext(), "No points, dude", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        ArrayList<DPoint> P2 = Other_matem(P1);
        DrawSpline(P1, P2);
    }

    private void DrawSpline(ArrayList<DPoint> p1, ArrayList<DPoint> p2) {
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
                if (nx < 0 || nx >= bitmap.getWidth()) continue;
                if (ny < 0 || ny >= bitmap.getHeight()) continue;
//                mBitmap.setPixel(nx, ny, Color.BLACK);
                DrawCircle(nx, ny, 5, Color.BLACK);
            }
        }
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.invalidate();
            }
        });
    }

    private ArrayList<DPoint> Other_matem(ArrayList<DPoint> P1) {
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

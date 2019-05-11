package com.example.image_editor;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class Algem extends Conductor implements View.OnTouchListener {

    private ImageView imageView;
    private Bitmap bitmap;
    private int typeEvent;
    private ArrayList<DPoint> K = new ArrayList<>();
    private MainActivity activity;
    private int n;

    private Button startAlgo;

    Algem(MainActivity activity) {
        super(activity);
        this.activity = activity;
        this.imageView = activity.getImageView();
    }

    void touchToolbar() {
        // btn1 -> draw point
        // btn2 -> do interpolation
        super.touchToolbar();
        PrepareToRun(R.layout.spline_menu);

        startAlgo = activity.findViewById(R.id.algo_spline);

        configDrawPointsButton((ImageButton) activity.findViewById(R.id.add_points));
        configStartAlgoButton(startAlgo);

        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(bitmap);
        imageView.setOnTouchListener(this);

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
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setEnabled(false);
                            }
                        });
                        algorithm();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setEnabled(true);
                            }
                        });
                        return bitmap;
                    }
                };
                splainTask.execute();
            }
        });
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int mx = (int) event.getX();
        int my = (int) event.getY();
        if (typeEvent == 1 && event.getAction() == 0) {

//            View npoint = new View(activity);
//            npoint.findViewById(R.id.mv_point);
//            npoint.setVisibility(View.VISIBLE);
//            npoint.setLayoutParams(new LinearLayout.LayoutParams(30, 30));

            DrawCircle(mx, my, 15, Color.BLACK);
            K.add(new DPoint(mx, my));
            imageView.invalidate();
            return true;
        }
        return false;
    }

    private void DrawCircle(int mx, int my, int r, int color) {
        for (int x = mx - r; x <= mx + r; x++) {
            for (int y = my - r; y <= my + r; y++) {
                if ((x - mx) * (x - mx) + (y - my) * (y - my) <= r * r){
                    if(0>x || x>=bitmap.getWidth())continue;
                    if(0>y || y>=bitmap.getHeight())continue;
                    bitmap.setPixel(x, y, color);
                }
            }
        }
    }

    public void setpoint(View view) {
        typeEvent = 1;
    }

    public DPoint pntSum(DPoint a, DPoint b) {
        return new DPoint(a.x + b.x, a.y + b.y);
    }

    public DPoint pntMul(double a, DPoint b) {
        return new DPoint(a * b.x, a * b.y);
    }

    private double a(int ind) {
        if (ind == 0) return 0;
        if (ind == n - 1) return 2;
        return 1;
    }

    private double b(int ind) {
        if (ind == 0) return 2;
        if (ind == n - 1) return 7;
        return 4;
    }

    private double c(int ind) {
        if (ind == 0) return 1;
        if (ind == n - 1) return 0;
        return 1;
    }

    private ArrayList<DPoint> Thomas_algorithm() {
        ArrayList<DPoint> D = new ArrayList<>();
        ArrayList<Double> B = new ArrayList<>();
        ArrayList<DPoint> dX = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            dX.add(new DPoint(0, 0));
            B.add(0.0);
        }
        B.set(0, 2.0);
        B.add(0.0);

        D.add(pntSum(K.get(0), pntMul(2, K.get(1))));
        for (int i = 1; i < n - 1; i++) {
            D.add(pntSum(pntMul(4, K.get(i)), pntMul(2, K.get(i + 1))));
        }
        D.add(pntSum(pntMul(8, K.get(n - 1)), K.get(n)));

        for (int i = 1; i < n; i++) {
            double m = a(i) / b(i - 1);
            B.set(i, b(i) - m * c(i - 1));
            D.set(i, pntSum(D.get(i), pntMul(-m, D.get(i - 1))));
        }
        //D(N) / B(N)
        double xnnx = D.get(n - 1).x / B.get(n - 1);
        double xnny = D.get(n - 1).y / B.get(n - 1);
        dX.set(n - 1, new DPoint(xnnx, xnny));
        for (int i = n - 2; i >= 0; i--) {
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
        // 2 * P(1, 0  ) + 1 * P(1, 1 )                 = K(0)+2*K(1)
        // 1 * P(1, i-1) + 4 * P(1, i ) + 1 * P(1, i+1) = 4*K(i)+2*K(i+1), for i in [1, n-2]
        //                 2 * P(1,n-2) + 7 * P(1, n-1) = 8*K(n-1)+K(n)
        n = K.size() - 1;
        ArrayList<DPoint> P1 = Thomas_algorithm();
        ArrayList<DPoint> P2 = Other_matem(P1);
        DrawSpline(P1, P2);
    }

    private void DrawSpline(ArrayList<DPoint> p1, ArrayList<DPoint> p2) {
        for (int i = 0; i < n; i++) {
            int step = 1000;
            for (int j = 0; j <= step; j++) {
                // B(t) = (1-t)^3 * P0 + 3*t(t-1)^2*P1 + 3*t^2*(t-1)*P2 + t^3*P3
                double t = (double) j / step;
                DPoint r1 = pntMul((1 - t) * (1 - t) * (1 - t), K.get(i));
                DPoint r2 = pntMul((1 - t) * (1 - t) * t, p1.get(i));
                DPoint r3 = pntMul((1 - t) * t * t, p2.get(i));
                DPoint r4 = pntMul(t * t * t, K.get(i + 1));
                int nx = (int) (r1.x + 3 * r2.x + 3 * r3.x + r4.x);
                int ny = (int) (r1.y + 3 * r2.y + 3 * r3.y + r4.y);
                if (nx < 0 || nx >= bitmap.getWidth()) continue;
                if (ny < 0 || ny >= bitmap.getHeight()) continue;
//                bitmap.setPixel(nx, ny, Color.BLACK);
                DrawCircle(nx, ny, 5, Color.BLACK);
            }
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.invalidate();
            }
        });
    }

    private ArrayList<DPoint> Other_matem(ArrayList<DPoint> P1) {
        ArrayList<DPoint> answer = new ArrayList<>();
        for (int i = 0; i <= n - 2; i++) {
            double nx = 2 * K.get(i + 1).x - P1.get(i + 1).x;
            double ny = 2 * K.get(i + 1).y - P1.get(i + 1).y;
            answer.add(new DPoint(nx, ny));
        }
        double nx = (K.get(n).x + P1.get(n - 1).x) / 2;
        double ny = (K.get(n).y + P1.get(n - 1).y) / 2;
        answer.add(new DPoint(nx, ny));
        return answer;
    }

    public void changeside(View view) {
        // todo: xepnZ hOW Tu du?? -0_0-
    }
}

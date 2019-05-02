package com.example.image_editor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class algem extends AppCompatActivity implements View.OnTouchListener {

    private ImageView imageView;
    private String path;
    private Bitmap bitmap;
    private Canvas canvas;
    private int typeEvent;
    private ArrayList<Point> K;
    private int n;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_algem);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        typeEvent = 0;
        K = new ArrayList<>();

        Intent intent = getIntent();
        this.path = intent.getStringExtra("Image");

        this.imageView = findViewById(R.id.imageScaling);

        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(this.path)));
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            canvas = new Canvas(bitmap);
            imageView.setImageBitmap(bitmap);
            imageView.setOnTouchListener(this);
        } catch (
                IOException e) {
            e.printStackTrace();
            Toast.makeText(algem.this, "Failed!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int mx = (int) event.getX();
        int my = (int) event.getY();
        if (typeEvent == 1) {
            canvas.drawCircle(mx, my, 15, new Paint(Color.BLACK));
            K.add(new Point(mx, my));
            imageView.invalidate();
            return true;
        }
        return false;
    }

    public void setpoint(View view) {
        typeEvent = 1;
    }

    public Point pntSum(Point a, Point b) {
        return new Point(a.x + b.x, a.y + b.y);
    }

    public Point pntMul(int a, Point b) {
        return new Point(a * b.x, a * b.y);
    }

    private int a(int ind) {
        if (ind == 1) return 2;
        if (ind == n) return 7;
        return 4;
    }

    private int c(int ind) {
        return ind == n ? 2 : 1;
    }

    @SuppressLint("Assert")
    public void algorithm(View view) {
        // in first we have:
        //
        // 2 * P(1, 0  ) + 1 * P(1, 1 )                 = K(0)+2*K(1)
        // 1 * P(1, i-1) + 4 * P(1, i ) + 1 * P(1, i+1) = 4*K(i)+2*K(i+1), for i in [1, n-2]
        //                 2 * P(1,n-2) + 7 * P(1, n-1) = 8*K(n-1)+K(n)
        // note: a(1)=2, a(i in [2, n-1])=4, a(n)=7;
        // note: b(i in [1, n-1])=1;
        // note: c(i in [2, n-1])=1, c[n]=2;
        n = K.size() - 1;
        assert n > 0;
        int w;
        ArrayList<Point> D = new ArrayList<>();
        ArrayList<Integer> B = new ArrayList<>(n);
        ArrayList<DPoint> X = new ArrayList<>(n);

        D.add(pntSum(K.get(0), pntMul(2, K.get(1))));
        for (int i = 1; i <= n - 2; i++) {
            D.add(pntSum(pntMul(4, K.get(i)), pntMul(2, K.get(i + 1))));
        }
        D.add(pntSum(pntMul(8, K.get(n - 1)), K.get(n)));

        for (int i = 2; i <= n; i++) {
            w = a(i);
            B.set(i, 1 - w * c(i - 1));
            D.set(i, pntSum(D.get(i), pntMul(-w, D.get(i - 1))));
        }
        //D(N) / B(N)
        double xnnx = (double) D.get(n).x / B.get(n);
        double xnny = (double) D.get(n).y / B.get(n);
        X.set(n, new DPoint(xnnx, xnny));
        for (int i = n - 1; i >= 1; i--) {
            //X(i) = (D(i) - C(i) * X(i + 1)) / B(i)
            xnnx = (D.get(n).x - c(i) * X.get(i + 1).x) / B.get(n);
            xnny = (D.get(n).y - c(i) * X.get(i + 1).y) / B.get(n);
            X.set(i, new DPoint(xnnx, xnny));
        }
    }

    public void changeside(View view) {
        // todo: xepnZ hOW Tu du?? -0_0-
    }
}

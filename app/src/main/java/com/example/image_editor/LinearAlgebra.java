package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Vector;
import java.util.zip.DeflaterOutputStream;

import static java.lang.Integer.min;

public class LinearAlgebra extends Conductor implements View.OnTouchListener {

    private ImageView imageView;
    private Bitmap bitmap;
    private MainActivity activity;
    private DPoint p11, p12, p13;
    private DPoint p21, p22, p23;
    private Button startAlgo;

    class Solver {
        public Double a, b, c, d, e, f;

        public DPoint calc(DPoint m) {
            return new DPoint(
                    a * m.x + c * m.y + e,
                    b * m.x + d * m.y + f
            );
        }

        Solver(ArrayList<ArrayList<Double>> ace, ArrayList<ArrayList<Double>> bdf) {
            this.a = ace.get(0).get(0);
            this.c = ace.get(1).get(0);
            this.e = ace.get(2).get(0);
            this.b = bdf.get(0).get(0);
            this.d = bdf.get(1).get(0);
            this.f = bdf.get(2).get(0);
        }
    }

    LinearAlgebra(MainActivity activity) {
        super(activity);
        this.activity = activity;
        this.imageView = activity.getImageView();
    }

    void touchToolbar() {
        // btn1 -> draw point
        // btn2 -> do interpolation
        super.touchToolbar();
        PrepareToRun(R.layout.linear_algebra_menu);

        Button btn_start = activity.findViewById(R.id.algo_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initParams();
                algorithm();
                imageView.invalidate();
            }
        });

        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(bitmap);
        imageView.setOnTouchListener(this);
    }

    void initParams() {
        p11 = new DPoint(1, 3);
        p12 = new DPoint(1, 5);
        p13 = new DPoint(8, 5);

        p21 = new DPoint(10, 30);
        p22 = new DPoint(142, 50);
        p23 = new DPoint(80, 50);
    }

    private Double determinant2(Double a11, Double a12, Double a21, Double a22) {
        return a11 * a22 - a12 * a21;
    }

    private Double determinant3(ArrayList<ArrayList<Double>> A) {
        return A.get(0).get(0) * determinant2(
                A.get(1).get(1), A.get(1).get(2),
                A.get(2).get(1), A.get(2).get(2)) -
                A.get(0).get(1) * determinant2(
                        A.get(1).get(0), A.get(1).get(2),
                        A.get(2).get(0), A.get(2).get(2)) +
                A.get(0).get(2) * determinant2(
                        A.get(1).get(0), A.get(1).get(1),
                        A.get(2).get(0), A.get(2).get(1));
    }

    //matrix of algebraic complements
    private ArrayList<ArrayList<Double>> moac3(ArrayList<ArrayList<Double>> A) {
        ArrayList<ArrayList<Double>> finish = A;
        int[] x11 = {1, 0, 0};
        int[] x12 = {1, 0, 0};
        int[] x21 = {2, 2, 1};
        int[] x22 = {2, 2, 1};
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                finish.get(i).set(j, determinant2(
                        A.get(x11[i]).get(x11[j]), A.get(x12[i]).get(x12[j]),
                        A.get(x21[i]).get(x21[j]), A.get(x22[i]).get(x22[j])
                ));
            }
        }
        return finish;
    }

    private ArrayList<ArrayList<Double>> inverse(ArrayList<ArrayList<Double>> start) {
        Double det = determinant3(start);
        if (det == 0) {
            // catch this!
            Log.i("upd", "det == 0!");
            return null;
        }
        ArrayList<ArrayList<Double>> finish = reverse(moac3(start));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                finish.get(i).set(j, finish.get(i).get(j) / det);
            }
        }
        return finish;
    }

    private ArrayList<ArrayList<Double>> reverse(ArrayList<ArrayList<Double>> A) {
        ArrayList<ArrayList<Double>> finish = A;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                finish.get(i).set(j, A.get(j).get(i));
            }
        }
        return finish;
    }

    private ArrayList<ArrayList<Double>> mulMatrix(ArrayList<ArrayList<Double>> A, ArrayList<ArrayList<Double>> B) {
        ArrayList<ArrayList<Double>> C = new ArrayList<>();
        for (int i = 0; i < A.size(); i++) {
            C.add(new ArrayList<Double>());
            for (int j = 0; j < B.get(0).size(); j++) {
                C.get(i).add(0.0);
            }
        }

        for (int i = 0; i < A.size(); i++) {
            for (int j = 0; j < B.get(0).size(); j++) {
                for (int r = 0; r < Math.min(A.get(i).size(), B.size()); r++) {
                    C.get(i).set(j, C.get(i).get(j) + A.get(i).get(r) * B.get(r).get(j));
                }
            }
        }

        return C;
    }

    private ArrayList<ArrayList<Double>> getFirstPoints() {
        ArrayList<ArrayList<Double>> A = new ArrayList<>();
        A.add(new ArrayList<Double>());
        A.add(new ArrayList<Double>());
        A.add(new ArrayList<Double>());
        A.get(0).add(p11.x);
        A.get(0).add(p11.y);
        A.get(0).add(1.0);
        A.get(1).add(p12.x);
        A.get(1).add(p12.y);
        A.get(1).add(1.0);
        A.get(2).add(p13.x);
        A.get(2).add(p13.y);
        A.get(2).add(1.0);
        return inverse(A);
    }

    private ArrayList<ArrayList<Double>> getSecondInACE() {
        ArrayList<ArrayList<Double>> B = new ArrayList<>();
        B.add(new ArrayList<Double>());
        B.get(0).add(p21.x);
        B.get(0).add(p22.x);
        B.get(0).add(p23.x);
        return B;
    }

    private ArrayList<ArrayList<Double>> getSecondInBDF() {
        ArrayList<ArrayList<Double>> B = new ArrayList<>();
        B.add(new ArrayList<Double>());
        B.get(0).add(p21.y);
        B.get(0).add(p22.y);
        B.get(0).add(p23.y);
        return B;
    }

    private void algorithm() {
        Solver solver = new Solver(
                mulMatrix(
                        getFirstPoints(),
                        getSecondInACE()),
                mulMatrix(
                        getFirstPoints(),
                        getSecondInBDF()));
        Log.i("upd", "mes!");
        Bitmap btmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                DPoint image = solver.calc(new DPoint(i, j));
                btmp.setPixel(i, j, bitmap.getPixel(
                        (int) image.x, (int) image.y));
            }
        }
        Log.i("upd", "nice!");
        bitmap = btmp;

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}

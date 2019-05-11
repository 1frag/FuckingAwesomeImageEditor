package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class LinearAlgebra extends Conductor implements View.OnTouchListener {

    private ImageView imageView;
    private Bitmap bitmap;
    private MainActivity activity;
    private DPoint p11, p12, p13;
    private DPoint p21, p22, p23;

    class Solver {
        private Double a, b, c, d, e, f;

        public DPoint calc(DPoint m) {
            return new DPoint(
                    a * m.x + c * m.y + e,
                    b * m.x + d * m.y + f
            );
        }

        Solver(Matrix3x1 ace, Matrix3x1 bdf) {
            this.a = ace.get(0);
            this.c = ace.get(1);
            this.e = ace.get(2);
            this.b = bdf.get(0);
            this.d = bdf.get(1);
            this.f = bdf.get(2);
        }
    }

    LinearAlgebra(MainActivity activity) {
        super(activity);
        this.activity = activity;
        this.imageView = activity.getImageView();
    }

    void touchToolbar() {
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
        p23 = new DPoint(80, 10);
    }

    //matrix of algebraic complements
    private Matrix3x3 moac3(Matrix3x3 A) {
        Matrix3x3 finish = new Matrix3x3();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                finish.set(i, j, A.det2(i, j) * Math.pow(-1, i + j));
            }
        }
        return finish;
    }

    private Matrix3x3 inverse(Matrix3x3 start) {
        double det = start.det3();
        if (det == 0) {
            // catch this!
            Log.i("upd", "det == 0!");
            return null;
        }
        Matrix3x3 finish = reverse(moac3(start));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                finish.set(i, j, finish.get(i, j) / det);
            }
        }
        return finish;
    }

    private Matrix3x3 reverse(Matrix3x3 A) {
        Matrix3x3 finish = new Matrix3x3();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                finish.set(i, j, A.get(j, i));
            }
        }
        return finish;
    }

    private Matrix3x1 mulMatrix(Matrix3x3 A, Matrix3x1 B) {
        Matrix3x1 C = new Matrix3x1();

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                for (int r = 0; r < 3; r++)
                    C.set(i, j, C.get(i, j) + A.get(i, r) * B.get(r, j));

        return C;
    }

    private Matrix3x3 getFirstPoints() {
        return inverse(new Matrix3x3(
                p11.x, p11.y, 1.0,
                p12.x, p12.y, 1.0,
                p13.x, p13.y, 1.0));
    }

    private Matrix3x1 getSecondInACE() {
        return new Matrix3x1(p21.x, p22.x, p23.x);
    }

    private Matrix3x1 getSecondInBDF() {
        return new Matrix3x1(p21.y, p22.y, p23.y);
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
        Bitmap btmp = bitmap.copy(Bitmap.Config.ARGB_8888,
                true);
        for (int i = 0; i < bitmap.getWidth(); i++) {
            Log.i("upd", "45!");
            for (int j = 0; j < bitmap.getHeight(); j++) {
                DPoint image = solver.calc(new DPoint(i, j));
                int w = (int) image.x;
                int h = (int) image.y;
                if (0 > w || w >= btmp.getWidth()) continue;
                if (0 > h || h >= btmp.getHeight()) continue;
                btmp.setPixel(i, j, bitmap.getPixel(w, h));
            }
        }
        imageView.setImageBitmap(btmp);
        Log.i("upd", "nice!");

    }

    private void test() {
        Matrix3x3 a = new Matrix3x3(
                2, 5, 7,
                6, 3, 4,
                5, -2, -3
        );
        Matrix3x3 b = inverse(a);
        Log.i("upd", "qwe");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}

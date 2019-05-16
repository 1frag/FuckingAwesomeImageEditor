package com.example.image_editor;

import android.util.Log;

public class ExecutorAffineTransformations {
    private Double a, b, c, d, e, f;
    private Matrix3x1 mSecond_x, mSecond_y;
    private Matrix3x3 mFirst;
    private boolean mError = true;

    public DPoint calc(DPoint m) {
        if (mError) return null;
        return new DPoint(
                a * m.x + c * m.y + e,
                b * m.x + d * m.y + f
        );
    }

    public DPoint calc(double x, double y) {
        if (mError) return null;
        return new DPoint(
                a * x + c * y + e,
                b * x + d * y + f
        );
    }

    ExecutorAffineTransformations(DPoint p11, DPoint p12, DPoint p13,
                                  DPoint p21, DPoint p22, DPoint p23) {
        // Solver's initialization
        mFirst = new Matrix3x3(
                p11.x, p11.y, 1.0,
                p12.x, p12.y, 1.0,
                p13.x, p13.y, 1.0);

        mSecond_x = new Matrix3x1(p21.x, p22.x, p23.x);

        mSecond_y = new Matrix3x1(p21.y, p22.y, p23.y);

        this.a = p21.x;
        this.c = p22.x;
        this.e = p23.x;
        this.b = p21.y;
        this.d = p22.y;
        this.f = p23.y;
    }

    public boolean prepare() {
        Matrix3x3 multiplier = inverse(mFirst);
        if (multiplier == null) {
            Log.i("upd", "det == 0!");
            return false;
        }

        Matrix3x1 ace = mulMatrix(multiplier, mSecond_x);
        Matrix3x1 bdf = mulMatrix(multiplier, mSecond_y);

        this.a = ace.get(0);
        this.c = ace.get(1);
        this.e = ace.get(2);
        this.b = bdf.get(0);
        this.d = bdf.get(1);
        this.f = bdf.get(2);
        mError = false;

        return true;

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
}

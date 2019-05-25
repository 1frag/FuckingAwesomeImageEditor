package com.example.image_editor.matrix;

public class Matrix3x3 {

    private double
            a11, a12, a13,
            a21, a22, a23,
            a31, a32, a33;

    public Matrix3x3(double a11, double a12, double a13,
                     double a21, double a22, double a23,
                     double a31, double a32, double a33) {
        this.a11 = a11;
        this.a12 = a12;
        this.a13 = a13;
        this.a21 = a21;
        this.a22 = a22;
        this.a23 = a23;
        this.a31 = a31;
        this.a32 = a32;
        this.a33 = a33;
    }

    public Matrix3x3() {
        this.a11 = this.a12 = this.a13 = 0;
        this.a21 = this.a22 = this.a23 = 0;
        this.a31 = this.a32 = this.a33 = 0;
    }

    public void set(int i, int j, double val) {
        if (i == 0 && j == 0) a11 = val;
        if (i == 0 && j == 1) a12 = val;
        if (i == 0 && j == 2) a13 = val;
        if (i == 1 && j == 0) a21 = val;
        if (i == 1 && j == 1) a22 = val;
        if (i == 1 && j == 2) a23 = val;
        if (i == 2 && j == 0) a31 = val;
        if (i == 2 && j == 1) a32 = val;
        if (i == 2 && j == 2) a33 = val;
    }

    public double get(int i, int j) {
        if (i == 0 && j == 0) return a11;
        if (i == 0 && j == 1) return a12;
        if (i == 0 && j == 2) return a13;
        if (i == 1 && j == 0) return a21;
        if (i == 1 && j == 1) return a22;
        if (i == 1 && j == 2) return a23;
        if (i == 2 && j == 0) return a31;
        if (i == 2 && j == 1) return a32;
        if (i == 2 && j == 2) return a33;
        return 404;
    }

    public double det2(int i, int j) {
        int[] f = {1, 2, 0, 2, 0, 1};

        return get(f[2 * i], f[2 * j]) * get(f[2 * i + 1], f[2 * j + 1]) -
                get(f[2 * i], f[2 * j + 1]) * get(f[2 * i + 1], f[2 * j]);
    }

    public double det3() {
        return get(0, 0) * det2(0, 0) -
                get(0, 1) * det2(0, 1) +
                get(0, 2) * det2(0, 2);
    }

}

package com.example.image_editor.matrix;

public class Matrix3x1 {

    private double a11, a21, a31;

    public Matrix3x1(Double a11, Double a21, Double a31) {
        this.a11 = a11;
        this.a21 = a21;
        this.a31 = a31;
    }

    public double get(int i, int j) {
        if (i == 0 && j == 0) return a11;
        if (i == 1 && j == 0) return a21;
        if (i == 2 && j == 0) return a31;
        return 404;
    }

    public double get(int row){
        if(row == 0)return a11;
        if(row == 1)return a21;
        if(row == 2)return a31;
        return 404;
    }

    public void set(int i, int j, double val) {
        if (i == 0 && j == 0) a11 = val;
        if (i == 1 && j == 0) a21 = val;
        if (i == 2 && j == 0) a31 = val;
    }

    public Matrix3x1() {
        this.a11 = this.a21 = this.a31 = 0;
    }
}

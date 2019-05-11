package com.example.image_editor;

public class Matrix3x1 {

    private double a11, a21, a31;

    Matrix3x1(Double a11, Double a21, Double a31) {
        this.a11 = a11;
        this.a21 = a21;
        this.a31 = a31;
    }

    double get(int i, int j) {
        if (i == 0 && j == 0) return a11;
        if (i == 1 && j == 0) return a21;
        if (i == 2 && j == 0) return a31;
        return 404;
    }

    double get(int row){
        if(row == 0)return a11;
        if(row == 1)return a21;
        if(row == 2)return a31;
        return 404;
    }

    void set(int i, int j, double val) {
        if (i == 0 && j == 0) a11 = val;
        if (i == 1 && j == 0) a21 = val;
        if (i == 2 && j == 0) a31 = val;
    }

    Matrix3x1() {
        this.a11 = this.a21 = this.a31 = 0;
    }
}

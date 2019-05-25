package com.example.image_editor;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;

import java.util.Comparator;

import static java.lang.Math.abs;

class PointComparator implements Comparator<Point> {

    private int INF = (int) (1e9 + 7);
    private Point mTarget;
    private int[][] mG;

    private int h(Point a){
        return abs(a.x - mTarget.x) + abs(a.y - mTarget.y);
    }

    private int Y(Point a) {
        if (a.x == INF)
            return INF;
        return -(mG[a.x][a.y]-h(a));
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public int compare(Point a, Point b) {
        return Integer.compare(Y(b), Y(a));
    }

    PointComparator(Point atarget, int n, int m){
        mTarget = atarget;
        mG = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                mG[i][j] = 1000000;
            }
        }
    }

    void setInG(Point r, int val){
        mG[r.x][r.y] = val;
    }

    int getInG(Point r){
        return mG[r.x][r.y];
    }
}

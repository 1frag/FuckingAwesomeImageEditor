package com.example.image_editor;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;

import java.util.Comparator;

import static java.lang.Math.abs;

public class PointComparator implements Comparator<Point> {

    private int INF = (int) (1e9 + 7);
    private Point target;
    private int[][] G;

    private int h(Point a){
        return abs(a.x - target.x) + abs(a.y - target.y);
    }

    private int Y(Point a) {
        if (a.x == INF)
            return INF;
        return -(G[a.x][a.y]+h(a));
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public int compare(Point a, Point b) {
        return Integer.compare(Y(b), Y(a));
    }

    public PointComparator(Point atarget, int n, int m){
        target = atarget;
        G = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                G[i][j] = 100000;
            }
        }
    }

    public void setInG(Point r, int val){
        G[r.x][r.y] = val;
    }

    public int getInG(Point r){
        return G[r.x][r.y];
    }
}

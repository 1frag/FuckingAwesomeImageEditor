package com.example.image_editor;

public class Pixel {
    private int x;
    private int y;
    private int color;
    Pixel(int ax, int ay, int acolor){
        x = ax;
        y = ay;
        color = acolor;
    }
    public int getX(){return x;}
    public int getY(){return y;}
    public int getColor(){return color;}
}

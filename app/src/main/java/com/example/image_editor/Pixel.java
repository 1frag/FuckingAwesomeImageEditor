package com.example.image_editor;

class Pixel {
    private int x;
    private int y;
    private int color;
    Pixel(int ax, int ay, int acolor){
        x = ax;
        y = ay;
        color = acolor;
    }
    int getX(){return x;}
    int getY(){return y;}
    int getColor(){return color;}
}

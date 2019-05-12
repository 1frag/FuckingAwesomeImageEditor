package com.example.image_editor;

import android.graphics.Bitmap;

import java.util.Stack;

public class History {

    private Stack<Bitmap> history;
    private Stack<Bitmap> buffer;
    private Bitmap originalBitmap;

    public History() {
        history = new Stack<Bitmap>();
        buffer = new Stack<Bitmap>();
    }

    public void addBitmap(Bitmap bitmap){
        history.push(bitmap);
        buffer.clear();
    }

    // put current bitmap in buffer
    public Bitmap popBitmap(){
        Bitmap bitmap;
        try {
            bitmap = history.pop(); // if history is empty
        }                           // return original
        catch (Exception e){
            return originalBitmap;
        }
        buffer.push(bitmap);

        return bitmap;
    }

    public Bitmap takeFromBuffer(){
        Bitmap bitmap;
        try {
            bitmap = buffer.pop();
        }catch (Exception e){
            return null;
        }
        history.push(bitmap);
        return bitmap;
    }

    public Bitmap showHead(){
        Bitmap bitmap = history.peek();
        return bitmap;
    }

    public void clearAllAndSetOriginal(Bitmap bitmap){
        history.clear();
        buffer.clear();
        originalBitmap = bitmap;
    }
}

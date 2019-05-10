package com.example.image_editor;

import android.graphics.Bitmap;

import java.util.Stack;

// a lot of null exceptions here
public class History {

    private Stack<Bitmap> history;
    private Stack<Bitmap> buffer;

    public History() {
        history = new Stack<Bitmap>();
        buffer = new Stack<Bitmap>();
    }

    public void addBitmap(Bitmap bitmap){
        history.push(bitmap);
        buffer.clear();
    }

    // put current bitmap in buffer
    public Bitmap popBitmap(Bitmap currentBitmap){
        Bitmap bitmap;
        try {
            bitmap = history.pop();
        }
        catch (Exception e){
            return null;
        }
        buffer.push(currentBitmap);

        return bitmap;
    }

    public Bitmap takeFromBuffer(){
        Bitmap bitmap;
        try {
            bitmap = buffer.pop();
        }catch (Exception e){
            return null;
        }
        return bitmap;
    }

    public Bitmap showHead(){
        Bitmap bitmap = history.peek();
        return bitmap;
    }
}

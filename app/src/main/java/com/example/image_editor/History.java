package com.example.image_editor;

import android.graphics.Bitmap;

import java.util.Stack;

// class to work with bitmap history
public class History {

    private Stack<Bitmap> mHistory;
    private Stack<Bitmap> mBuffer;
    private Bitmap mOriginalBitmap;

    public History() {
        mHistory = new Stack<Bitmap>();
        mBuffer = new Stack<Bitmap>();
    }

    public void addBitmap(Bitmap bitmap){
        mHistory.push(bitmap);
        mBuffer.clear();
    }

    // put current bitmap in mBuffer
    public Bitmap popBitmap(){
        Bitmap bitmap;
        try {
            bitmap = mHistory.pop(); // if mHistory is empty
        }                           // return original
        catch (Exception e){
            return mOriginalBitmap;
        }
        mBuffer.push(bitmap);

        return bitmap;
    }

    public Bitmap takeFromBuffer(){
        Bitmap bitmap;
        try {
            bitmap = mBuffer.pop();
        }catch (Exception e){
            return null;
        }
        mHistory.push(bitmap);
        return bitmap;
    }

    public Bitmap showHead(){
        Bitmap bitmap = mHistory.peek();
        return bitmap;
    }

    public void clearAllAndSetOriginal(Bitmap bitmap){
        mHistory.clear();
        mBuffer.clear();
        mOriginalBitmap = bitmap;
    }
}

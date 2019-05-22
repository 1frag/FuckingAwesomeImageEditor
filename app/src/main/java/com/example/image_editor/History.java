package com.example.image_editor;

import android.graphics.Bitmap;

import java.util.EmptyStackException;
import java.util.Stack;

// class to work with bitmap history
public class History {

    private Stack<Bitmap> mHistory;
    private Stack<Bitmap> mBuffer;
    private Bitmap mOriginalBitmap;

    private int mCounter = 0;

    private static final int STACK_SIZE = 10;

    public History() {
        mHistory = new Stack<Bitmap>();
        mBuffer = new Stack<Bitmap>();
    }

    public void addBitmap(Bitmap bitmap){
        mHistory.push(bitmap.copy(Bitmap.Config.ARGB_8888, true));
        mCounter += 1;
        if (mCounter == STACK_SIZE) dropStack();
        mBuffer.clear();
    }

    // put current bitmap in mBuffer
    public Bitmap popBitmap(){
        Bitmap bitmap;
        try {
            bitmap = mHistory.pop(); // if mHistory is empty
        }                           // return original
        catch (EmptyStackException e){
            return mOriginalBitmap;
        }
        mBuffer.push(bitmap);
        mCounter -= 1;

        return bitmap;
    }

    public Bitmap takeFromBuffer(){
        Bitmap bitmap;
        try {
            bitmap = mBuffer.pop();
        }catch (EmptyStackException e){
            return null;
        }
        mHistory.push(bitmap.copy(Bitmap.Config.ARGB_8888, true));
        mCounter += 1;
        if (mCounter == STACK_SIZE) dropStack();

        return bitmap;
    }

    public Bitmap showHead(){
        Bitmap bitmap;
        try {
            bitmap = mHistory.peek();
        } catch (EmptyStackException e){
            return mOriginalBitmap;
        }
        return bitmap;
    }

    public void clearAllAndSetOriginal(Bitmap bitmap){
        mHistory.clear();
        mBuffer.clear();
        mOriginalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mCounter = 1;
    }

    private void dropStack(){
        mOriginalBitmap = mHistory.remove(0);
        mCounter -= 1;
    }
}

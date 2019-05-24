package com.example.image_editor;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface ImageEditorListner {

    public Bitmap getBitmap();
    public ImageView getImageView();
    public void setPixelBitmap(int x, int y, int color);
    public MainActivity getMainActivity();
    public int getPixelBitmap(int x, int y);
    public void resetBitmap();
    public void invalidateImageView();
    public void setAlgorithmExecuted(boolean val);
    public void setImageChanged(boolean val);

}

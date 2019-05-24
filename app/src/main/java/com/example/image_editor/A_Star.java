package com.example.image_editor;

import android.graphics.Color;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.example.image_editor.fragments.FragmentAStar;

import java.util.ArrayList;

import static java.lang.Math.abs;

public class A_Star extends Controller implements OnTouchListener {

    private String TAG = "upd/A_Star";

    private Point mPointStart, mPointFinish;
    private Point[][] mPar;
    private ArrayList<Pixel> mRemStart, mRemFinish, mRemWall;

    private FragmentAStar fragment = new FragmentAStar();

    private Integer mTypeDraw = 0;
    private boolean mStartIsSet = false;
    private boolean mFinishIsSet = false;

    A_Star(MainActivity activity) {
        super(activity);
        mRemStart = new ArrayList<>();
        mRemFinish = new ArrayList<>();
        mRemWall = new ArrayList<>();
    }

    @Override
    void touchToolbar() {
        super.touchToolbar();
//        prepareToRun(new FragmentAStar());
        prepareToRun(fragment);
        setHeader(mainActivity.getResources().getString(R.string.a_star_name));

        imageView.setImageBitmap(mainActivity.getBitmap());
        imageView.invalidate();
        imageView.setOnTouchListener(this);
    }

    @Override
    public void lockInterface() {
        super.lockInterface();
        fragment.lockInterface();
        imageView.setOnTouchListener(null);
    }

    @Override
    public void unlockInterface() {
        super.unlockInterface();
        fragment.unlockInterface();
        imageView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mainActivity.imageChanged = true;

        float scalingX = imageView.getWidth() / (float) mainActivity.getBitmap().getWidth();
        float scalingY = imageView.getHeight() / (float) mainActivity.getBitmap().getHeight();
        int mx = (int) (event.getX() / scalingX);
        int my = (int) (event.getY() / scalingY);

        if (mTypeDraw == 3) {
            return drawWall(mx, my);
        } else if (mTypeDraw == 2) {
            return drawFinish(mx, my);
        } else if (mTypeDraw == 1) {
            return drawStart(mx, my);
        } else
            return false;
    }

    private boolean canPutRect(int rad, int mx, int my) {
        if ((mPointFinish == null) ||
                ((mx - mPointFinish.x) * (mx - mPointFinish.x) + (my - mPointFinish.y) + (my - mPointFinish.y) >= 45 * 45)) {
            if ((mPointStart == null) ||
                    ((mx - mPointStart.x) * (mx - mPointStart.x) + (my - mPointStart.y) + (my - mPointStart.y) >= 45 * 45)) {
                return true;
            }
        }
        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= mainActivity.getBitmap().getWidth() ||
                        0 > my + j || my + j >= mainActivity.getBitmap().getHeight()) {
                    continue;
                }
                if (abs(i) + abs(j) <= rad) {
                    if (mainActivity.getBitmap().getPixel(mx + i, my + j) == Color.rgb(10, 255, 10) ||
                            mainActivity.getBitmap().getPixel(mx + i, my + j) == Color.rgb(255, 10, 10)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean drawStart(int mx, int my) {
        int rad = 30;
        if (mStartIsSet) return false;
        if (!canPutRect(rad, mx, my)) {
            return false;
        }
        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= mainActivity.getBitmap().getWidth()) {
                    continue;
                }
                if (0 > my + j || my + j >= mainActivity.getBitmap().getHeight()) {
                    continue;
                }
                if (abs(i) + abs(j) <= rad) {
                    Pixel now = new Pixel(mx + i, my + j, mainActivity.getBitmap().getPixel(mx + i, my + j));
                    mRemStart.add(now);
                    // sometimes that happened
                    mainActivity.getBitmap().setPixel(mx + i, my + j, Color.rgb(10, 255, 10));
                    mainActivity.imageChanged = true;
                }
            }
        }
        mPointStart = new Point(mx, my);
        mStartIsSet = true;
        imageView.invalidate();
        return true;
    }

    private boolean drawFinish(int mx, int my) {
        int rad = 30;
        if (mFinishIsSet) return false;
        if (!canPutRect(rad, mx, my)) {
            return false;
        }
        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= mainActivity.getBitmap().getWidth()) {
                    continue;
                }
                if (0 > my + j || my + j >= mainActivity.getBitmap().getHeight()) {
                    continue;
                }
                // abs(i) + abs(j) <= rad
                if (abs(i) + abs(j) <= rad) {
                    Pixel now = new Pixel(mx + i, my + j, mainActivity.getBitmap().getPixel(mx + i, my + j));
                    mRemFinish.add(now);
                    // sometimes that happened
                    try {
                        mainActivity.getBitmap().setPixel(mx + i, my + j, Color.rgb(255, 10, 10));
                    } catch (IllegalStateException e){
                        break;
                    }
                    mainActivity.imageChanged = true;
                }
            }
        }
        mPointFinish = new Point(mx, my);
        mFinishIsSet = true;
        imageView.invalidate();
        return true;
    }

    private boolean conditionInWall(int i, int j, int rad) {
        if (fragment.settings.type_wall == R.id.rb_romb)
            return abs(i) + abs(j) <= rad;
        if (fragment.settings.type_wall == R.id.rb_square)
            return true;
        if (fragment.settings.type_wall == R.id.rb_circul)
            return i * i + j * j <= rad * rad;
        return true;
    }

    private boolean drawWall(int mx, int my) {
        int rad = fragment.settings.size_wall;
        if (!canPutRect(rad, mx, my)) {
            return false;
        }
        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= mainActivity.getBitmap().getWidth()) {
                    continue;
                }
                if (0 > my + j || my + j >= mainActivity.getBitmap().getHeight()) {
                    continue;
                }
                if (conditionInWall(i, j, rad)) {
                    Pixel now = new Pixel(mx + i, my + j, mainActivity.getBitmap().getPixel(mx + i, my + j));
                    mRemWall.add(now);
                    mainActivity.getBitmap().setPixel(mx + i, my + j, fragment.settings.color_wall);
                    mainActivity.imageChanged = true;
                }
            }
        }
        imageView.invalidate();
        return true;
    }


}
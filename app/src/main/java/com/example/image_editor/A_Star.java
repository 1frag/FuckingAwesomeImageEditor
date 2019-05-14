package com.example.image_editor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.View.OnTouchListener;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;

import static java.lang.Math.abs;

public class A_Star extends Conductor implements OnTouchListener {

    private Bitmap mBitmap;

    private ImageButton mChangeStartButton;
    private ImageButton mChangeEndButton;
    private ImageButton mSetWallButton;
    private Button mStartAlgoButton;
    private AppCompatImageButton mSettingsButton;

    private Point mPointStart, mPointFinish;
    private Point[][] mPar;
    private ArrayList<Pixel> mRemStart, mRemFinish;

    private MainActivity mainActivity;
    private ImageView mImageView;

    private Integer mTypeDraw = 0;
    private boolean mStartIsSet = false, mFinishIsSet = false;

    A_Star(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        mainActivity = activity;
        mImageView = activity.getImageView();
        mRemStart = new ArrayList<>();
        mRemFinish = new ArrayList<>();
    }

    @Override
    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.a_star_menu);

        mChangeStartButton = mainActivity.findViewById(R.id.button_start_a_star);
        mChangeEndButton = mainActivity.findViewById(R.id.button_finish_a_star);
        mSetWallButton = mainActivity.findViewById(R.id.button_set_wall);
        mStartAlgoButton = mainActivity.findViewById(R.id.button_start_algo_a_star);
        mSettingsButton = mainActivity.findViewById(R.id.button_settings_a_star);

        configDoAlgoButton(mStartAlgoButton);
        configWallButton(mSetWallButton);
        configFinishButton(mChangeEndButton);
        configStartButton(mChangeStartButton);
        configSettingsButton(mSettingsButton);

        mBitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();

        mBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mImageView.setImageBitmap(mBitmap);
        mImageView.setOnTouchListener(this);
    }

    @Override
    public void lockInterface(){
        super.lockInterface();
        mChangeStartButton.setEnabled(false);
        mChangeEndButton.setEnabled(false);
        mSetWallButton.setEnabled(false);
        mStartAlgoButton.setEnabled(false);
        mSettingsButton.setEnabled(false);
    }

    @Override
    public void unlockInterface(){
        super.unlockInterface();
        mChangeStartButton.setEnabled(true);
        mChangeEndButton.setEnabled(true);
        mSetWallButton.setEnabled(true);
        mStartAlgoButton.setEnabled(true);
        mSettingsButton.setEnabled(true);
    }

    private void configWallButton(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWall(v);
            }
        });
    }

    private void configFinishButton(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTo(v);
            }
        });
    }

    private void configStartButton(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFrom(v);
            }
        });
    }

    private void configDoAlgoButton(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mStartIsSet || !mFinishIsSet){
                    Toast.makeText(mainActivity.getApplicationContext(),
                            "You need to set start and finish first!", Toast.LENGTH_SHORT).show();
                    return;
                }
                touchRun();
            }
        });
    }

    private void configSettingsButton(View settings) {
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingsDialog().setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        AlertDialog alertDialog = (AlertDialog) dialog;
                        //TODO: here
                        RadioButton rgRool = alertDialog.findViewById(R.id.rgRool);
//                        rgRool.

                    }
                });
            }
        });
    }

    private Dialog openSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        LayoutInflater inflater = mainActivity.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.a_star_settings, null))
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO: apply button
                        Log.i("upd", "mew");
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO: cancel button
                    }
                });
        return builder.create();
    }
    
    private void setFrom(View view) {
        mTypeDraw = 1;
        if (mStartIsSet) {
            for (int i = 0; i < mRemStart.size(); i++) {
                mBitmap.setPixel(mRemStart.get(i).getX(),
                        mRemStart.get(i).getY(),
                        mRemStart.get(i).getColor());
            }
            mRemStart.clear();
            mStartIsSet = false;
        }
    }

    private void setTo(View view) {
        mTypeDraw = 2;
        if (mFinishIsSet) {
            for (int i = 0; i < mRemFinish.size(); i++) {
                mBitmap.setPixel(mRemFinish.get(i).getX(),
                        mRemFinish.get(i).getY(),
                        mRemFinish.get(i).getColor());
            }
            mRemFinish.clear();
            mFinishIsSet = false;
        }
    }

    private void setWall(View view) {
        mTypeDraw = 3;
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
                if (0 > mx + i || mx + i >= mBitmap.getWidth() ||
                        0 > my + j || my + j >= mBitmap.getHeight()) {
                    continue;
                }
                if (abs(i) + abs(j) <= rad) {
                    if (mBitmap.getPixel(mx + i, my + j) == Color.rgb(10, 255, 10) ||
                            mBitmap.getPixel(mx + i, my + j) == Color.rgb(255, 10, 10)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void errorTouched() {
        // TODO: handle this
    }

    // TODO: override методы по канону должны быть вверху записаны
    // algorithm part goes here
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int mx = (int) event.getX();
        int my = (int) event.getY();

        mx -= (mImageView.getWidth() - mBitmap.getWidth()) / 2.0;
        my -= (mImageView.getHeight() - mBitmap.getHeight()) / 2.0;

        Log.i("upd", ((Integer) (mx)).toString() + " " + ((Integer) (my)).toString());
        if (mTypeDraw == 3) {
            int rad = 15;
            if (!canPutRect(rad, mx, my)) {
                errorTouched();
                return false;
            }
            for (int i = -rad; i <= rad; i++) {
                for (int j = -rad; j <= rad; j++) {
                    if (0 > mx + i || mx + i >= mBitmap.getWidth()) {
                        continue;
                    }
                    if (0 > my + j || my + j >= mBitmap.getHeight()) {
                        continue;
                    }
                    if (abs(i) + abs(j) <= rad) {
                        Pixel now = new Pixel(mx + i, my + j, mBitmap.getPixel(mx + i, my + j));
                        mRemFinish.add(now);
                        mBitmap.setPixel(mx + i, my + j, Color.WHITE);
                    }
                }
            }
            mImageView.invalidate();
            return true;

        } else if (mTypeDraw == 2) {
            int rad = 30;
            if (mFinishIsSet) return false;
            if (!canPutRect(rad, mx, my)) {
                errorTouched();
                return false;
            }
            for (int i = -rad; i <= rad; i++) {
                for (int j = -rad; j <= rad; j++) {
                    if (0 > mx + i || mx + i >= mBitmap.getWidth()) {
                        continue;
                    }
                    if (0 > my + j || my + j >= mBitmap.getHeight()) {
                        continue;
                    }
                    if (abs(i) + abs(j) <= rad) {
                        Pixel now = new Pixel(mx + i, my + j, mBitmap.getPixel(mx + i, my + j));
                        mRemFinish.add(now);
                        mBitmap.setPixel(mx + i, my + j, Color.rgb(255, 10, 10));
                    }
                }
            }
            mPointFinish = new Point(mx, my);
            mFinishIsSet = true;
            mImageView.invalidate();
            return true;

        } else if (mTypeDraw == 1) {
            int rad = 30;
            if (mStartIsSet) return false;
            if (!canPutRect(rad, mx, my)) {
                errorTouched();
                return false;
            }
            for (int i = -rad; i <= rad; i++) {
                for (int j = -rad; j <= rad; j++) {
                    if (0 > mx + i || mx + i >= mBitmap.getWidth()) {
                        continue;
                    }
                    if (0 > my + j || my + j >= mBitmap.getHeight()) {
                        continue;
                    }
                    if (abs(i) + abs(j) <= rad) {
                        Pixel now = new Pixel(mx + i, my + j, mBitmap.getPixel(mx + i, my + j));
                        mRemStart.add(now);
                        mBitmap.setPixel(mx + i, my + j, Color.rgb(10, 255, 10));
                    }
                }
            }
            mPointStart = new Point(mx, my);
            mStartIsSet = true;
            mImageView.invalidate();
            return true;

        } else {
            // TODO: message: wtf u don't click button
        }
        return false;
    }

    private boolean check() {
        // TODO: handle this
        return true;
    }

    private boolean cor(Point a) {
        return mBitmap.getPixel(a.x, a.y) != Color.WHITE;
    }

    private ArrayList<Point> reconstructPath() {
        ArrayList<Point> res = new ArrayList<>();
        Point now = mPointFinish;
        while (now != mPointStart) {
            res.add(now);
            now = mPar[now.x][now.y];
        }
        res.add(mPointStart);
        Collections.reverse(res);
        return res;
    }

    public ArrayList<Point> algorithm(int n, int m) {

        boolean[][] in_open = new boolean[n][m];
        mPar = new Point[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                in_open[i][j] = false;
            }
        }

        int[] dirx = {0, 0, 1, -1};
        int[] diry = {1, -1, 0, 0};

        HashSet<Point> closedset = new HashSet<>();
        PointComparator myComp = new PointComparator(mPointFinish, n, m);
        PriorityQueue<Point> openset =
                new PriorityQueue<>(10, myComp);
        openset.add(mPointStart);
        myComp.setInG(mPointStart, 0);

        while (!openset.isEmpty()) {
            Point x = openset.poll();

            if (x.x == mPointFinish.x && x.y == mPointFinish.y) {
                return reconstructPath();
            }
            closedset.add(x);
            for (int i = 0; i < 4; i++) {
                boolean tentative_is_better = false;
                Point y = new Point(dirx[i] + x.x, diry[i] + x.y);
                if (y.x < 0 || y.x >= n) continue;
                if (y.y < 0 || y.y >= m) continue;
                if (!cor(y)) continue;
                if (closedset.contains(y)) continue;
                int tentative_g_score = myComp.getInG(x) + 1;
                if (!in_open[y.x][y.y]) {
                    tentative_is_better = true;
                    in_open[y.x][y.y] = true;
                    openset.add(y);
                } else if (tentative_g_score < myComp.getInG(y)) {
                    tentative_is_better = true;
                }
                if (tentative_is_better) {
                    mPar[y.x][y.y] = x;
                    myComp.setInG(y, tentative_g_score);
                }
            }
        }
        return new ArrayList<>();
    }

    void touchRun() {
        if (!check()) {
            return;
        }

        final int n = mBitmap.getWidth();
        final int m = mBitmap.getHeight();

        AsyncTaskConductor asyncTask = new AsyncTaskConductor() {
            @Override
            protected Bitmap doInBackground(String... params) {
                ArrayList<Point> answer = algorithm(n, m);

                for (int i = 0; i < answer.size(); i++) {
                    mBitmap.setPixel(answer.get(i).x,
                            answer.get(i).y,
                            Color.YELLOW);
                }

                return mBitmap;
            }
        };

        asyncTask.execute();
    }

    // TODO: below
    class Settings {
        private int rool, type_wall, size_wall;
        private int color_wall, size_path, color_path;

        Settings() {
            rool = 0;
            type_wall = 0;
            size_wall = 15;
            color_wall = 0xFFFFFF;
            size_path = 5;
            color_path = 0x00FFFF;
        }
    }
}
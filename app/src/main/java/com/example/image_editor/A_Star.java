package com.example.image_editor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;

import yuku.ambilwarna.AmbilWarnaDialog;

import static java.lang.Math.abs;

public class A_Star extends Conductor implements OnTouchListener {

    private Button mStartAlgoButton;
    private ImageButton mChangeStartButton;
    private ImageButton mChangeEndButton;
    private ImageButton mSetWallButton;
    private AppCompatImageButton mSettingsButton;

    private Point mPointStart, mPointFinish;
    private Point[][] mPar;
    private ArrayList<Pixel> mRemStart, mRemFinish, mRemWall;

    private Settings mSettings;

    private Integer mTypeDraw = 0;
    private boolean mStartIsSet = false, mFinishIsSet = false;

    A_Star(MainActivity activity) {
        super(activity);
        mRemStart = new ArrayList<>();
        mRemFinish = new ArrayList<>();
        mRemWall = new ArrayList<>();
    }

    @Override
    void touchToolbar() {
        super.touchToolbar();
        prepareToRun(R.layout.a_star_menu);
        setHeader("A* algorithm");

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
        mSettings = new Settings();

        imageView.setImageBitmap(bitmap);
        imageView.setOnTouchListener(this);
    }

    @Override
    public void lockInterface() {
        super.lockInterface();
        mChangeStartButton.setEnabled(false);
        mChangeEndButton.setEnabled(false);
        mSetWallButton.setEnabled(false);
        mStartAlgoButton.setEnabled(false);
        mSettingsButton.setEnabled(false);
    }

    @Override
    public void unlockInterface() {
        super.unlockInterface();
        mChangeStartButton.setEnabled(true);
        mChangeEndButton.setEnabled(true);
        mSetWallButton.setEnabled(true);
        mStartAlgoButton.setEnabled(true);
        mSettingsButton.setEnabled(true);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        float scalingX = imageView.getWidth() / (float) bitmap.getWidth();
        float scalingY = imageView.getHeight() / (float) bitmap.getHeight();
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

    private boolean drawStart(int mx, int my) {
        int rad = 30;
        if (mStartIsSet) return false;
        if (!canPutRect(rad, mx, my)) {
            errorTouched();
            return false;
        }
        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= bitmap.getWidth()) {
                    continue;
                }
                if (0 > my + j || my + j >= bitmap.getHeight()) {
                    continue;
                }
                if (abs(i) + abs(j) <= rad) {
                    Pixel now = new Pixel(mx + i, my + j, bitmap.getPixel(mx + i, my + j));
                    mRemStart.add(now);
                    bitmap.setPixel(mx + i, my + j, Color.rgb(10, 255, 10));
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
            errorTouched();
            return false;
        }
        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= bitmap.getWidth()) {
                    continue;
                }
                if (0 > my + j || my + j >= bitmap.getHeight()) {
                    continue;
                }
                // abs(i) + abs(j) <= rad
                if (abs(i) + abs(j) <= rad) {
                    Pixel now = new Pixel(mx + i, my + j, bitmap.getPixel(mx + i, my + j));
                    mRemFinish.add(now);
                    bitmap.setPixel(mx + i, my + j, Color.rgb(255, 10, 10));
                }
            }
        }
        mPointFinish = new Point(mx, my);
        mFinishIsSet = true;
        imageView.invalidate();
        return true;
    }

    private boolean drawWall(int mx, int my) {
        int rad = mSettings.size_wall;
        if (!canPutRect(rad, mx, my)) {
            errorTouched();
            return false;
        }
        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= bitmap.getWidth()) {
                    continue;
                }
                if (0 > my + j || my + j >= bitmap.getHeight()) {
                    continue;
                }
                if (conditionInWall(i, j, rad)) {
                    Pixel now = new Pixel(mx + i, my + j, bitmap.getPixel(mx + i, my + j));
                    mRemWall.add(now);
                    bitmap.setPixel(mx + i, my + j, mSettings.color_wall);
                }
            }
        }
        imageView.invalidate();
        return true;
    }

    private boolean conditionInWall(int i, int j, int rad) {
        if (mSettings.type_wall == R.id.rb_romb)
            return abs(i) + abs(j) <= rad;
        if (mSettings.type_wall == R.id.rb_square)
            return true;
        if (mSettings.type_wall == R.id.rb_circul)
            return i * i + j * j <= rad * rad;
        return true;
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
                if (!mStartIsSet || !mFinishIsSet) {
                    Toast.makeText(mainActivity.getApplicationContext(),
                            "You need to set start and finish first!", Toast.LENGTH_SHORT).show();
                    return;
                }
                touchRun();
                imageView.invalidate();
            }
        });
    }

    private void configSettingsButton(View settings) {
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = openSettingsDialog();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        final AlertDialog alertDialog = (AlertDialog) dialog;

                        //set according msetting
                        RadioGroup rg_rool = alertDialog.findViewById(R.id.rg_rool);
                        rg_rool.check(mSettings.rool);
                        RadioGroup rg_walls = alertDialog.findViewById(R.id.rg_walls);
                        rg_walls.check(mSettings.type_wall);
                        SeekBar seekbar_size_wall = alertDialog.findViewById(R.id.seekbar_size_wall);
                        seekbar_size_wall.setProgress(mSettings.size_wall);
                        SeekBar seekbar_size_path = alertDialog.findViewById(R.id.seekbar_size_path);
                        seekbar_size_path.setProgress(mSettings.size_wall);
                        Button button_color_path = alertDialog.findViewById(R.id.button_color_path);
                        button_color_path.setBackgroundColor(mSettings.color_path);
                        Button button_color_wall = alertDialog.findViewById(R.id.button_color_wall);
                        button_color_wall.setBackgroundColor(mSettings.color_wall);
                        // end setting

                        alertDialog.findViewById(R.id.button_color_wall)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        configSelectColorWall(alertDialog);
                                    }
                                });

                        alertDialog.findViewById(R.id.button_color_path)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        configSelectColorPath(alertDialog);
                                    }
                                });

                    }
                });
                dialog.show();
            }
        });
    }

    private void configSelectColorWall(final AlertDialog alertDialog) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(mainActivity,
                mSettings.color_wall,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        if (mSettings.color_wall != color)
                            redrawWall(color);
                        mSettings.color_wall = color;
                        alertDialog.findViewById(R.id.button_color_wall)
                                .setBackgroundColor(color);
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // cancel was selected by the user
                    }
                });
        dialog.show();
    }

    private void redrawWall(int color) {
        for (int i = 0; i < mRemWall.size(); i++) {
            bitmap.setPixel(mRemWall.get(i).getX(),
                    mRemWall.get(i).getY(), color);
        }
        imageView.invalidate();
    }

    private void configSelectColorPath(final AlertDialog alertDialog) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(mainActivity,
                mSettings.color_path,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        mSettings.color_path = color;
                        alertDialog.findViewById(R.id.button_color_path)
                                .setBackgroundColor(color);
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // cancel was selected by the user
                    }
                });
        dialog.show();
    }

    private Dialog openSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        LayoutInflater inflater = mainActivity.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.a_star_settings, null))
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        AlertDialog alertDialog = (AlertDialog) dialog;
                        mSettings.rool = ((RadioGroup) alertDialog
                                .findViewById(R.id.rg_rool)).getCheckedRadioButtonId();
                        mSettings.type_wall = ((RadioGroup) alertDialog
                                .findViewById(R.id.rg_walls)).getCheckedRadioButtonId();
                        mSettings.size_path = ((SeekBar) alertDialog
                                .findViewById(R.id.seekbar_size_path)).getProgress();
                        mSettings.size_wall = ((SeekBar) alertDialog
                                .findViewById(R.id.seekbar_size_wall)).getProgress();
                        mSettings.color_path = ((ColorDrawable) alertDialog
                                .findViewById(R.id.button_color_path)
                                .getBackground())
                                .getColor();
                        mSettings.color_wall = ((ColorDrawable) alertDialog
                                .findViewById(R.id.button_color_wall)
                                .getBackground())
                                .getColor();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // nothing to say, just exit
                    }
                });
        return builder.create();
    }

    private void setFrom(View view) {
        mTypeDraw = 1;
        if (mStartIsSet) {
            for (int i = 0; i < mRemStart.size(); i++) {
                bitmap.setPixel(mRemStart.get(i).getX(),
                        mRemStart.get(i).getY(),
                        mRemStart.get(i).getColor());
            }
            mRemStart.clear();
            mStartIsSet = false;
        }
        imageView.invalidate();
    }

    private void setTo(View view) {
        mTypeDraw = 2;
        if (mFinishIsSet) {
            for (int i = 0; i < mRemFinish.size(); i++) {
                bitmap.setPixel(mRemFinish.get(i).getX(),
                        mRemFinish.get(i).getY(),
                        mRemFinish.get(i).getColor());
            }
            mRemFinish.clear();
            mFinishIsSet = false;
            imageView.invalidate();
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
                if (0 > mx + i || mx + i >= bitmap.getWidth() ||
                        0 > my + j || my + j >= bitmap.getHeight()) {
                    continue;
                }
                if (abs(i) + abs(j) <= rad) {
                    if (bitmap.getPixel(mx + i, my + j) == Color.rgb(10, 255, 10) ||
                            bitmap.getPixel(mx + i, my + j) == Color.rgb(255, 10, 10)) {
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

    private int[] getDirX() {
        if (mSettings.rool == R.id.rb_four)
            return (new int[]{0, 0, 1, -1});
        return (new int[]{1, 1, -1, -1, -1, 1, 0, 0});
    }

    private int[] getDirY() {
        if (mSettings.rool == R.id.rb_four)
            return (new int[]{1, -1, 0, 0});
        return (new int[]{1, -1, 1, -1, 0, 0, -1, 1});
    }

    private ArrayList<Point> algorithm(int n, int m) {

        boolean[][] in_open = new boolean[n][m];
        boolean[][] is_cor = new boolean[n][m];
        mPar = new Point[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                in_open[i][j] = false;
                is_cor[i][j] = true;
            }
        }

        for (int i = 0; i < mRemWall.size(); i++) {
            int nx = mRemWall.get(i).getX();
            int ny = mRemWall.get(i).getY();

            if(bitmap.getPixel(nx+1,ny) == mSettings.color_wall &&
                    bitmap.getPixel(nx-1,ny) == mSettings.color_wall &&
                    bitmap.getPixel(nx,ny+1) == mSettings.color_wall &&
                    bitmap.getPixel(nx,ny-1) == mSettings.color_wall){
                continue;
            }

            int rad = mSettings.size_path;
            for (int ii = -rad; ii <= rad; ii++) {
                for (int jj = -rad; jj <= rad; jj++) {
                    nx = mRemWall.get(i).getX() + ii;
                    ny = mRemWall.get(i).getY() + jj;
                    if (nx < 0 || ny < 0) continue;
                    if (nx > n || ny > m) continue;
                    if (ii * ii + jj * jj <= rad * rad) {
                        is_cor[nx][ny] = false;
                    }
                }
            }
        }

        int[] dirx = getDirX();
        int[] diry = getDirY();

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
            for (int i = 0; i < dirx.length; i++) {
                boolean tentative_is_better = false;
                Point y = new Point(dirx[i] + x.x, diry[i] + x.y);
                if (y.x < 0 || y.x >= n) continue;
                if (y.y < 0 || y.y >= m) continue;
                if (!is_cor[y.x][y.y]) continue;
                if (closedset.contains(y)) continue;
                if (abs(dirx[i]) + abs(diry[i]) == 2 &&
                        mSettings.rool == R.id.rb_eight_with_restrictions) {
                    if (!is_cor[y.x][diry[i] + y.y] &&
                            !is_cor[dirx[i] + y.x][y.y]) continue;
                }
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

    // todo: change seekbar bound FROM 1 TO 5 ::SIZES

    void touchRun() {

        final int n = bitmap.getWidth();
        final int m = bitmap.getHeight();

        @SuppressLint("StaticFieldLeak") AsyncTaskConductor asyncTask = new AsyncTaskConductor() {
            @Override
            protected Bitmap doInBackground(String... params) {
                ArrayList<Point> answer = algorithm(n, m);

                for (int i = 0; i < answer.size(); i++) {
                    int rad = mSettings.size_path;
                    for (int ii = -rad; ii <= rad; ii++) {
                        for (int jj = -rad; jj <= rad; jj++) {
                            bitmap.setPixel(answer.get(i).x + ii,
                                    answer.get(i).y + jj,
                                    mSettings.color_path);
                        }
                    }
                }

                return bitmap;
            }
        };

        asyncTask.execute();
    }

    class Settings {
        public int rool, type_wall, size_wall;
        public int color_wall, size_path, color_path;

        Settings() {
            rool = R.id.rb_four;
            type_wall = R.id.rb_romb;
            size_wall = 30;
            color_wall = Color.WHITE;
            size_path = 5;
            color_path = Color.YELLOW;
        }
    }
}
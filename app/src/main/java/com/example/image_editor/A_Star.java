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
import android.util.Log;
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

public class A_Star extends Controller implements OnTouchListener {

    private Button mStartAlgoButton;
    private ImageButton mChangeStartButton;
    private ImageButton mChangeEndButton;
    private ImageButton mSetWallButton;
    private ImageButton mClearButton;
    private AppCompatImageButton mSettingsButton;
    private String TAG = "upd/A_Star";

    private Point mPointStart, mPointFinish;
    private Point[][] mPar;
    private ArrayList<Pixel> mRemStart, mRemFinish, mRemWall;

    private Settings mSettings;

    private Integer mTypeDraw = 0;
    private boolean mStartIsSet = false;
    private boolean mFinishIsSet = false;

    private float scalingX;
    private float scalingY;
    private int W;
    private int H;

    A_Star(MainActivity activity) {
        super(activity);
        mRemStart = new ArrayList<>();
        mRemFinish = new ArrayList<>();
        mRemWall = new ArrayList<>();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    void touchToolbar() {
        super.touchToolbar();
        prepareToRun(R.layout.a_star_menu);
        setHeader(mainActivity.getResources().getString(R.string.a_star_name));

        mChangeStartButton = mainActivity.findViewById(R.id.button_start_a_star);
        mChangeEndButton = mainActivity.findViewById(R.id.button_finish_a_star);
        mSetWallButton = mainActivity.findViewById(R.id.button_set_wall);
        mStartAlgoButton = mainActivity.findViewById(R.id.button_start_algo_a_star);
        mSettingsButton = mainActivity.findViewById(R.id.button_settings_a_star);
        mClearButton = mainActivity.findViewById(R.id.button_clear);

        configDoAlgoButton(mStartAlgoButton);
        configWallButton(mSetWallButton);
        configFinishButton(mChangeEndButton);
        configStartButton(mChangeStartButton);
        configSettingsButton(mSettingsButton);
        configClearButton(mClearButton);
        configMethodInfoButton(
                mainActivity.findViewById(R.id.button_help),
                R.drawable.help_a_star);
        mSettings = new Settings();

        W = mainActivity.getBitmap().getWidth();
        H = mainActivity.getBitmap().getHeight();
        scalingX = imageView.getWidth() / (float) W;
        scalingY = imageView.getHeight() / (float) H;

        imageView.setImageBitmap(mainActivity.getBitmap());
        imageView.invalidate();
        imageView.setOnTouchListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void lockInterface() {
        super.lockInterface();
        mChangeStartButton.setEnabled(false);
        mChangeEndButton.setEnabled(false);
        mSetWallButton.setEnabled(false);
        mStartAlgoButton.setEnabled(false);
        mSettingsButton.setEnabled(false);
        mClearButton.setEnabled(false);
        imageView.setOnTouchListener(null);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void unlockInterface() {
        super.unlockInterface();
        mChangeStartButton.setEnabled(true);
        mChangeEndButton.setEnabled(true);
        mSetWallButton.setEnabled(true);
        mStartAlgoButton.setEnabled(true);
        mSettingsButton.setEnabled(true);
        mClearButton.setEnabled(true);
        imageView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mainActivity.imageChanged = true;

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
            return false;
        }
        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= W) {
                    continue;
                }
                if (0 > my + j || my + j >= H) {
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
                if (0 > mx + i || mx + i >= W) {
                    continue;
                }
                if (0 > my + j || my + j >= H) {
                    continue;
                }

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

    private boolean drawWall(int mx, int my) {
        int rad = mSettings.sizeWall;
        if (!canPutRect(rad, mx, my)) {
            return false;
        }
        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= W) {
                    continue;
                }
                if (0 > my + j || my + j >= H) {
                    continue;
                }
                if (conditionInWall(i, j, rad)) {
                    Pixel now = new Pixel(mx + i, my + j, mainActivity.getBitmap().getPixel(mx + i, my + j));
                    mRemWall.add(now);
                    mainActivity.getBitmap().setPixel(mx + i, my + j, mSettings.color_wall);
                    mainActivity.imageChanged = true;
                }
            }
        }
        imageView.invalidate();
        return true;
    }

    private boolean conditionInWall(int i, int j, int rad) {
        if (mSettings.typeWall == R.id.rb_romb)
            return abs(i) + abs(j) <= rad;
        if (mSettings.typeWall == R.id.rb_square)
            return true;
        if (mSettings.typeWall == R.id.rb_circul)
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
                            mainActivity.getResources().getString(R.string.warning_set_start_and_finish), Toast.LENGTH_SHORT).show();
                    return;
                }
                touchRun();
                imageView.invalidate();
            }
        });
    }

    private void configClearButton(View btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick");
                mainActivity.resetBitmap();
                imageView.setImageBitmap(mainActivity.getBitmap());
                mainActivity.invalidateImageView();
                mRemStart = new ArrayList<>();
                mRemFinish = new ArrayList<>();
                mRemWall = new ArrayList<>();
                mStartIsSet = false;
                mFinishIsSet = false;
                mainActivity.algorithmExecuted = false;
                mainActivity.imageChanged = false;
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
                        rg_walls.check(mSettings.typeWall);

                        SeekBar seekBarSizeWall = alertDialog.findViewById(R.id.seekbar_size_wall);
                        seekBarSizeWall.setMax(30);
                        seekBarSizeWall.setProgress(mSettings.sizeWall);

                        SeekBar seekBarSizePath = alertDialog.findViewById(R.id.seekbar_size_path);
                        seekBarSizePath.setMax(5);
                        seekBarSizePath.setProgress(mSettings.sizePath);

                        Button button_color_path = alertDialog.findViewById(R.id.button_color_path);
                        button_color_path.setBackgroundColor(mSettings.colorPath);

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
            mainActivity.getBitmap().setPixel(mRemWall.get(i).getX(),
                    mRemWall.get(i).getY(), color);
            mainActivity.imageChanged = true;
        }
        imageView.invalidate();
    }

    private void configSelectColorPath(final AlertDialog alertDialog) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(mainActivity,
                mSettings.colorPath,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        mSettings.colorPath = color;
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
                        mSettings.typeWall = ((RadioGroup) alertDialog
                                .findViewById(R.id.rg_walls)).getCheckedRadioButtonId();
                        mSettings.sizePath = ((SeekBar) alertDialog
                                .findViewById(R.id.seekbar_size_path)).getProgress();
                        mSettings.sizeWall = ((SeekBar) alertDialog
                                .findViewById(R.id.seekbar_size_wall)).getProgress();
                        mSettings.colorPath = ((ColorDrawable) alertDialog
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
                mainActivity.getBitmap().setPixel(mRemStart.get(i).getX(),
                        mRemStart.get(i).getY(),
                        mRemStart.get(i).getColor());
                mainActivity.imageChanged = true;
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
                mainActivity.getBitmap().setPixel(mRemFinish.get(i).getX(),
                        mRemFinish.get(i).getY(),
                        mRemFinish.get(i).getColor());
                mainActivity.imageChanged = true;
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
                if (0 > mx + i || mx + i >= W ||
                        0 > my + j || my + j >= H) {
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
        boolean[][] isCor = new boolean[n][m];
        mPar = new Point[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                in_open[i][j] = false;
                isCor[i][j] = true;
            }
        }

        for (int i = 0; i < mRemWall.size(); i++) {
            int nx = mRemWall.get(i).getX();
            int ny = mRemWall.get(i).getY();

            if (mainActivity.getPixelBitmap(nx + 1, ny) == mSettings.color_wall &&
                    mainActivity.getPixelBitmap(nx - 1, ny) == mSettings.color_wall &&
                    mainActivity.getPixelBitmap(nx, ny + 1) == mSettings.color_wall &&
                    mainActivity.getPixelBitmap(nx, ny - 1) == mSettings.color_wall) {
                continue;
            }

            int rad = mSettings.sizePath;
            for (int ii = -rad; ii <= rad; ii++) {
                for (int jj = -rad; jj <= rad; jj++) {
                    nx = mRemWall.get(i).getX() + ii;
                    ny = mRemWall.get(i).getY() + jj;
                    if (nx < 0 || ny < 0) continue;
                    if (nx >= n || ny >= m) continue;
                    if (ii * ii + jj * jj <= rad * rad) {
                        isCor[nx][ny] = false;
                    }
                }
            }
        }

        int[] dirx = getDirX();
        int[] diry = getDirY();

        HashSet<Point> closedset = new HashSet<>();
        PointComparator myComp = new PointComparator(mPointFinish, n, m);
        PriorityQueue<Point> openset =
                new PriorityQueue<>(30000, myComp);
        openset.add(mPointStart);
        myComp.setInG(mPointStart, 0);

        while (!openset.isEmpty()) {
            Point x = openset.poll();

            if (x.x == mPointFinish.x && x.y == mPointFinish.y) {
                return reconstructPath();
            }
            closedset.add(x);
            for (int i = 0; i < dirx.length; i++) {
                boolean tentativeIsBetter = false;
                Point y = new Point(dirx[i] + x.x, diry[i] + x.y);
                if (y.x < 0 || y.x >= n) continue;
                if (y.y < 0 || y.y >= m) continue;
                if (!isCor[y.x][y.y]) continue;
                if (closedset.contains(y)) continue;
                if ((abs(dirx[i]) + abs(diry[i]) == 2) &&
                        mSettings.rool == R.id.rb_eight_with_restrictions) {
                    if (!isCor[y.x][y.y] &&
                            !isCor[y.x][y.y]) continue;
                }
                int tentativeGScore = myComp.getInG(x) + 1;
                if (!in_open[y.x][y.y]) {
                    tentativeIsBetter = true;
                    in_open[y.x][y.y] = true;
                    openset.add(y);
                } else if (tentativeGScore < myComp.getInG(y)) {
                    tentativeIsBetter = true;
                }
                if (tentativeIsBetter) {
                    mPar[y.x][y.y] = x;
                    myComp.setInG(y, tentativeGScore);
                }
            }
        }
        return new ArrayList<>();
    }

    private void touchRun() {

        final int n = W;
        final int m = H;

        @SuppressLint("StaticFieldLeak")
        AsyncTaskConductor asyncTask = new AsyncTaskConductor() {
            @Override
            protected Bitmap doInBackground(String... params) {
                ArrayList<Point> answer = algorithm(n, m);

                for (int i = 0; i < answer.size(); i++) {
                    int rad = mSettings.sizePath;
                    for (int ii = -rad; ii <= rad; ii++) {
                        for (int jj = -rad; jj <= rad; jj++) {
                            mainActivity.setPixelBitmap(answer.get(i).x + ii,
                                    answer.get(i).y + jj,
                                    mSettings.colorPath);
                        }
                    }
                }
                if (answer.size() == 0) {
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mainActivity.getApplicationContext(),
                                    "Path not found 404",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return mainActivity.getBitmap();
            }
        };

        asyncTask.execute();
        mainActivity.algorithmExecuted = true;
    }

    class Settings {
        int rool, typeWall, sizeWall;
        int color_wall, sizePath, colorPath;

        Settings() {
            rool = R.id.rb_four;
            typeWall = R.id.rb_romb;
            sizeWall = 30;
            color_wall = Color.WHITE;
            sizePath = 5;
            colorPath = Color.YELLOW;
        }
    }
}
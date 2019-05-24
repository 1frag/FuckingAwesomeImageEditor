package com.example.image_editor.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.image_editor.Controller;
import com.example.image_editor.ImageEditorListner;
import com.example.image_editor.Pixel;
import com.example.image_editor.PointComparator;
import com.example.image_editor.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;

import yuku.ambilwarna.AmbilWarnaDialog;

import static java.lang.Math.abs;

public class FragmentAStar extends Fragment {

    private Button mStartAlgoButton;
    private ImageButton mChangeStartButton;
    private ImageButton mChangeEndButton;
    private ImageButton mSetWallButton;
    private ImageButton mClearButton;
    private AppCompatImageButton mSettingsButton;

    private String TAG = "FragmentAStar";

    private ImageEditorListner activity;

    private Point mPointStart, mPointFinish;
    private Point[][] mPar;
    private ArrayList<Pixel> mRemStart, mRemFinish, mRemWall;

    public Settings settings;

    private Integer mTypeDraw = 0;
    private boolean mStartIsSet = false;
    private boolean mFinishIsSet = false;

    private ImageEditorListner iCallback;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            iCallback = (ImageEditorListner) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View result = inflater.inflate(R.layout.a_star_menu, container, false);
        if (getActivity() == null){
            Log.i(TAG, "null!!");
        }

        settings = new Settings();

        mChangeStartButton = getActivity().findViewById(R.id.button_start_a_star);
        mChangeEndButton = getActivity().findViewById(R.id.button_finish_a_star);
        mSetWallButton = getActivity().findViewById(R.id.button_set_wall);
        mStartAlgoButton = getActivity().findViewById(R.id.button_start_algo_a_star);
        mSettingsButton = getActivity().findViewById(R.id.button_settings_a_star);
        mClearButton = getActivity().findViewById(R.id.button_clear);

        configDoAlgoButton(mStartAlgoButton);
        configWallButton(mSetWallButton);
        configFinishButton(mChangeEndButton);
        configStartButton(mChangeStartButton);
        configSettingsButton(mSettingsButton);
        configClearButton(mClearButton);

        // Inflate the layout for this fragment
        return result;
    }

    public void lockInterface() {
        mChangeStartButton.setEnabled(false);
        mChangeEndButton.setEnabled(false);
        mSetWallButton.setEnabled(false);
        mStartAlgoButton.setEnabled(false);
        mSettingsButton.setEnabled(false);
        mClearButton.setEnabled(false);
    }

    public void unlockInterface() {
        mChangeStartButton.setEnabled(true);
        mChangeEndButton.setEnabled(true);
        mSetWallButton.setEnabled(true);
        mStartAlgoButton.setEnabled(true);
        mSettingsButton.setEnabled(true);
        mClearButton.setEnabled(true);
    }

    private void configDoAlgoButton(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mStartIsSet || !mFinishIsSet) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            getActivity().getResources().getString(R.string.warning_set_start_and_finish), Toast.LENGTH_SHORT).show();
                    return;
                }
                touchRun();
                iCallback.getImageView().invalidate();
            }
        });
    }

    void touchRun() {

        final int n = iCallback.getBitmap().getWidth();
        final int m = iCallback.getBitmap().getHeight();

        @SuppressLint("StaticFieldLeak") Controller.AsyncTaskConductor asyncTask = new Controller.AsyncTaskConductor() {
            @Override
            protected Bitmap doInBackground(String... params) {
                ArrayList<Point> answer = algorithm(n, m);

                for (int i = 0; i < answer.size(); i++) {
                    int rad = settings.size_path;
                    for (int ii = -rad; ii <= rad; ii++) {
                        for (int jj = -rad; jj <= rad; jj++) {
                            // если нарисовал пиксель за битмапом, то это вылетает
                            iCallback.setPixelBitmap(answer.get(i).x + ii,
                                    answer.get(i).y + jj,
                                    settings.color_path);
                        }
                    }
                }

                if (answer.size() == 0) {
                    iCallback.getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(iCallback.getMainActivity().getApplicationContext(),
                                    "Path not found 404",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                return iCallback.getBitmap();
            }
        };

        asyncTask.execute();
        iCallback.getMainActivity().algorithmExecuted = true;
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

    public class Settings {
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

    private int[] getDirX() {
        if (settings.rool == R.id.rb_four)
            return (new int[]{0, 0, 1, -1});
        return (new int[]{1, 1, -1, -1, -1, 1, 0, 0});
    }

    private int[] getDirY() {
        if (settings.rool == R.id.rb_four)
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

            if (iCallback.getPixelBitmap(nx + 1, ny) == settings.color_wall &&
                    iCallback.getPixelBitmap(nx - 1, ny) == settings.color_wall &&
                    iCallback.getPixelBitmap(nx, ny + 1) == settings.color_wall &&
                    iCallback.getPixelBitmap(nx, ny - 1) == settings.color_wall) {
                continue;
            }

            int rad = settings.size_path;
            for (int ii = -rad; ii <= rad; ii++) {
                for (int jj = -rad; jj <= rad; jj++) {
                    nx = mRemWall.get(i).getX() + ii;
                    ny = mRemWall.get(i).getY() + jj;
                    if (nx < 0 || ny < 0) continue;
                    if (nx >= n || ny >= m) continue;
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
            // todo: why a* with 8 direction incorrect?
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
                if ((abs(dirx[i]) + abs(diry[i]) == 2) &&
                        settings.rool == R.id.rb_eight_with_restrictions) {
                    if (!is_cor[y.x][y.y] &&
                            !is_cor[y.x][y.y]) continue;
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

    private void configClearButton(View btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick");
                iCallback.resetBitmap();
                iCallback.getImageView().setImageBitmap(iCallback.getBitmap());
                iCallback.invalidateImageView();
                // todo: all to default
                mRemStart = new ArrayList<>();
                mRemFinish = new ArrayList<>();
                mRemWall = new ArrayList<>();
                mStartIsSet = false;
                mFinishIsSet = false;
                iCallback.setAlgorithmExecuted(false);
                iCallback.setImageChanged(false);
            }
        });
    }

    private void configSettingsButton(View setting) {
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = openSettingsDialog();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        final AlertDialog alertDialog = (AlertDialog) dialog;

                        //set according msetting
                        RadioGroup rg_rool = alertDialog.findViewById(R.id.rg_rool);

                        rg_rool.check(settings.rool);

                        RadioGroup rg_walls = alertDialog.findViewById(R.id.rg_walls);
                        rg_walls.check(settings.type_wall);

                        SeekBar seekBarSizeWall = alertDialog.findViewById(R.id.seekbar_size_wall);
                        seekBarSizeWall.setMax(30);
                        seekBarSizeWall.setProgress(settings.size_wall);

                        SeekBar seekBarSizePath = alertDialog.findViewById(R.id.seekbar_size_path);
                        seekBarSizePath.setMax(5);
                        seekBarSizePath.setProgress(settings.size_path);

                        Button button_color_path = alertDialog.findViewById(R.id.button_color_path);
                        button_color_path.setBackgroundColor(settings.color_path);

                        Button button_color_wall = alertDialog.findViewById(R.id.button_color_wall);
                        button_color_wall.setBackgroundColor(settings.color_wall);
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
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(iCallback.getMainActivity(),
                settings.color_wall,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        if (settings.color_wall != color)
                            redrawWall(color);
                        settings.color_wall = color;
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
            iCallback.getBitmap().setPixel(mRemWall.get(i).getX(),
                    mRemWall.get(i).getY(), color);
            iCallback.setImageChanged(true);
        }
        iCallback.invalidateImageView();
    }

    private void configSelectColorPath(final AlertDialog alertDialog) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(iCallback.getMainActivity(),
                settings.color_path,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        settings.color_path = color;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(iCallback.getMainActivity());
        LayoutInflater inflater = iCallback.getMainActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.a_star_settings, null))
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        AlertDialog alertDialog = (AlertDialog) dialog;
                        settings.rool = ((RadioGroup) alertDialog
                                .findViewById(R.id.rg_rool)).getCheckedRadioButtonId();
                        settings.type_wall = ((RadioGroup) alertDialog
                                .findViewById(R.id.rg_walls)).getCheckedRadioButtonId();
                        settings.size_path = ((SeekBar) alertDialog
                                .findViewById(R.id.seekbar_size_path)).getProgress();
                        settings.size_wall = ((SeekBar) alertDialog
                                .findViewById(R.id.seekbar_size_wall)).getProgress();
                        settings.color_path = ((ColorDrawable) alertDialog
                                .findViewById(R.id.button_color_path)
                                .getBackground())
                                .getColor();
                        settings.color_wall = ((ColorDrawable) alertDialog
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
                iCallback.getBitmap().setPixel(mRemStart.get(i).getX(),
                        mRemStart.get(i).getY(),
                        mRemStart.get(i).getColor());
                iCallback.setImageChanged(true);
            }
            mRemStart.clear();
            mStartIsSet = false;
        }
        iCallback.invalidateImageView();
    }

    private void setTo(View view) {
        mTypeDraw = 2;
        if (mFinishIsSet) {
            for (int i = 0; i < mRemFinish.size(); i++) {
                iCallback.getBitmap().setPixel(mRemFinish.get(i).getX(),
                        mRemFinish.get(i).getY(),
                        mRemFinish.get(i).getColor());
                iCallback.setImageChanged(true);
            }
            mRemFinish.clear();
            mFinishIsSet = false;
            iCallback.invalidateImageView();
        }
    }

    private void setWall(View view) {
        mTypeDraw = 3;
    }



}

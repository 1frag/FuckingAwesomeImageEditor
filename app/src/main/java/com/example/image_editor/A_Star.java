package com.example.image_editor;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;

import static java.lang.Math.abs;

public class A_Star extends Conductor implements OnTouchListener {

    private Integer typeDraw = 0;
    private Bitmap bitmap;
    private ArrayList<Pixel> remstart, remfinish;
    private boolean start = false, finish = false;
    private ImageButton change_start;
    private ImageButton change_end;
    private Point pnt_start, pnt_finish;
    private Point[][] par;
    private MainActivity activity;

    private ImageView imageView;

    A_Star(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        this.activity = activity;
        this.imageView = activity.getImageView();
        remstart = new ArrayList<>();
        remfinish = new ArrayList<>();

    }

    private void ConfigWallButton(ImageButton button) {
//        button.setText("set wall");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnsetwall(v);
            }
        });
    }

    private void ConfigFinishButton(ImageButton button) {
//        button.setText("Set finish");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnsetto(v);
            }
        });
    }

    private void ConfigStartButton(ImageButton button) {
//        button.setText("Set start");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnsetfrom(v);
            }
        });
    }

    public void click_finish(View view) {
        Log.i("upd", "complete");
    }

    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.a_star_menu);

        this.change_start = activity.findViewById(R.id.start);
        this.change_end = activity.findViewById(R.id.finish);
        ImageButton btn_wall = activity.findViewById(R.id.wall);
        Button btn_algo = activity.findViewById(R.id.algo_a_star);

        ConfigDoAlgoButton(btn_algo);
        ConfigWallButton(btn_wall);
        ConfigFinishButton(change_end);
        ConfigStartButton(change_start);

        ConfigSettingsButton(activity.findViewById(R.id.settings));

        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(bitmap);
        imageView.setOnTouchListener(this);
    }

    private Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.a_star_settings, null))
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // todo: apply button
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // todo: cancel button
                    }
                });
        return builder.create();
    }

    private void ConfigSettingsButton(View settings) {
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateDialog(null).show();
            }
        });

    }

    private void ConfigDoAlgoButton(Button btn_algo) {
        btn_algo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                touchRun();
            }
        });
    }

    private void btnsetfrom(View view) {
        typeDraw = 1;
        if (start) {
            for (int i = 0; i < remstart.size(); i++) {
                bitmap.setPixel(remstart.get(i).getX(),
                        remstart.get(i).getY(),
                        remstart.get(i).getColor());
            }
            remstart.clear();
//            change_start.setText("set from");
            start = false;
        }
    }

    private void btnsetto(View view) {
        typeDraw = 2;
        if (finish) {
            for (int i = 0; i < remfinish.size(); i++) {
                bitmap.setPixel(remfinish.get(i).getX(),
                        remfinish.get(i).getY(),
                        remfinish.get(i).getColor());
            }
            remfinish.clear();
//            change_end.setText("set to");
            finish = false;
        }
    }

    private void btnsetwall(View view) {
        typeDraw = 3;
    }

    private boolean canPutRect(int rad, int mx, int my) {
        if ((pnt_finish == null) ||
                ((mx - pnt_finish.x) * (mx - pnt_finish.x) + (my - pnt_finish.y) + (my - pnt_finish.y) >= 45 * 45)) {
            if ((pnt_start == null) ||
                    ((mx - pnt_start.x) * (mx - pnt_start.x) + (my - pnt_start.y) + (my - pnt_start.y) >= 45 * 45)) {
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
        // todo: hand this
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int mx = (int) event.getX();
        int my = (int) event.getY();

        mx -= (imageView.getWidth() - bitmap.getWidth()) / 2.0;
        my -= (imageView.getHeight() - bitmap.getHeight()) / 2.0;

        Log.i("upd", ((Integer) (mx)).toString() + " " + ((Integer) (my)).toString());
//        Log.i("UPD", "touch");
        if (typeDraw == 3) {
            int rad = 15;
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
                        remfinish.add(now);
                        bitmap.setPixel(mx + i, my + j, Color.WHITE);
                    }
                }
            }
            imageView.invalidate();
            return true;
        } else if (typeDraw == 2) {
            int rad = 30;
            if (finish) return false;
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
                        remfinish.add(now);
                        bitmap.setPixel(mx + i, my + j, Color.rgb(255, 10, 10));
                    }
                }
            }
            pnt_finish = new Point(mx, my);
            finish = true;
            imageView.invalidate();
//            change_end.setText("delete to");
            return true;
        } else if (typeDraw == 1) {
            int rad = 30;
            if (start) return false;
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
                        remstart.add(now);
                        bitmap.setPixel(mx + i, my + j, Color.rgb(10, 255, 10));
                    }
                }
            }
            pnt_start = new Point(mx, my);
            start = true;
            imageView.invalidate();
//            change_start.setText("delete from");
            return true;
        } else {
            // TODO: message: wtf u don't click button
        }
        return false;
    }

    private boolean check() {
        // todo: hand this
        return true;
    }

    private boolean cor(Point a) {
        return bitmap.getPixel(a.x, a.y) != Color.WHITE;
    }

    private ArrayList<Point> reconstruct_path() {
        ArrayList<Point> res = new ArrayList<>();
        Point now = pnt_finish;
        while (now != pnt_start) {
            res.add(now);
            now = par[now.x][now.y];
        }
        res.add(pnt_start);
        Collections.reverse(res);
        return res;
    }

    public ArrayList<Point> algorithm(int n, int m) {

        boolean[][] in_open = new boolean[n][m];
        par = new Point[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                in_open[i][j] = false;
            }
        }

        int[] dirx = {0, 0, 1, -1};
        int[] diry = {1, -1, 0, 0};

        HashSet<Point> closedset = new HashSet<>();
        PointComparator myComp = new PointComparator(pnt_finish, n, m);
        PriorityQueue<Point> openset =
                new PriorityQueue<>(10, myComp);
        openset.add(pnt_start);
        myComp.setInG(pnt_start, 0);

        while (!openset.isEmpty()) {
            Point x = openset.poll();

            if (x.x == pnt_finish.x && x.y == pnt_finish.y) {
                return reconstruct_path();
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
                    par[y.x][y.y] = x;
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
        int n = bitmap.getWidth();
        int m = bitmap.getHeight();


        AsyncTaskConductor task = new AsyncTaskConductor() {
            @Override
            protected Bitmap doInBackground(String... params) {
                int n = Integer.parseInt(params[1]);
                int m = Integer.parseInt(params[2]);

                ArrayList<Point> answer = algorithm(n, m);

                for (int i = 0; i < answer.size(); i++) {
                    bitmap.setPixel(answer.get(i).x,
                            answer.get(i).y,
                            Color.YELLOW);
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.invalidate();
                    }
                });

                return bitmap;
            }
        };

        task.execute("A*", Integer.toString(n), Integer.toString(m));
//        ArrayList<Point> answer = algorithm(n, m);
//
//        for (int i = 0; i < answer.size(); i++) {
//            bitmap.setPixel(answer.get(i).x,
//                    answer.get(i).y,
//                    Color.YELLOW);
//        }
//
//        imageView.invalidate();
    }

}
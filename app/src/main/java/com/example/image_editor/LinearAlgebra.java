package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class LinearAlgebra extends Conductor {

    private Button mStartAlgoButton;
    private ImageButton mSetStartPointsButton;
    private ImageButton mSetFinishPointsButton;

    private Bitmap mBitmap;
    private ImageView mImageView;
    private MainActivity mainActivity;

    private DPoint p11, p12, p13;
    private DPoint p21, p22, p23;

    class Solver {
        private Double a, b, c, d, e, f;

        public DPoint calc(DPoint m) {
            return new DPoint(
                    a * m.x + c * m.y + e,
                    b * m.x + d * m.y + f
            );
        }

        Solver(Matrix3x1 ace, Matrix3x1 bdf) {
            this.a = ace.get(0);
            this.c = ace.get(1);
            this.e = ace.get(2);
            this.b = bdf.get(0);
            this.d = bdf.get(1);
            this.f = bdf.get(2);
        }
    }

    LinearAlgebra(MainActivity activity) {
        super(activity);
        mainActivity = activity;
        mImageView = activity.getImageView();
    }

    @Override
    void touchToolbar() {
        super.touchToolbar();
        prepareToRun(R.layout.linear_algebra_menu);

        FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();
        ElevationDragFragment fragment = new ElevationDragFragment();
        transaction.replace(R.id.sample_content_fragment, fragment);
        transaction.commit();

        mSetFinishPointsButton = mainActivity.findViewById(R.id.button_finish_points);
        mSetStartPointsButton = mainActivity.findViewById(R.id.button_start_points);
        mStartAlgoButton = mainActivity.findViewById(R.id.button_start_linear_algebra);

        configStartAlgoButton(mStartAlgoButton);
        configSetStartPointsButton(mSetStartPointsButton);
        configSetFinishPointsButton(mSetFinishPointsButton);

        mBitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        mBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mImageView.setImageBitmap(mBitmap);
    }

    @Override
    public void lockInterface(){
        super.lockInterface();
        mStartAlgoButton.setEnabled(false);
    }

    @Override
    public void unlockInterface(){
        super.unlockInterface();
        mStartAlgoButton.setEnabled(true);
    }

    private void configSetStartPointsButton(ImageButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMovingViewFirstGroup();
            }
        });
    }

    private void configSetFinishPointsButton(ImageButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMovingViewSecondGroup();
            }
        });
    }


    private void configStartAlgoButton(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initParams();
                AsyncTaskConductor asyncTask = new AsyncTaskConductor() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        mBitmap = algorithm();
                        return mBitmap;
                    }
                };
                asyncTask.execute();
            }
        });
    }

    private void initMovingViewFirstGroup() {
        // todo: wtf, why?, becouse in touchToolbar circle_i is null
        mainActivity.findViewById(R.id.circle1).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.circle2).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.circle3).setVisibility(View.VISIBLE);

        mainActivity.findViewById(R.id.circle4).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.circle5).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.circle6).setVisibility(View.INVISIBLE);
//        View c1 = mainActivity.findViewById(R.id.circle1);
//        c1.setY(10);
//        c1.setX(15);
//        c1.invalidate();
//        c1.setTop(10);
//        c1.setLeft(15);
//        c1.setBackgroundColor(0x00FFFF);
    }

    private void initMovingViewSecondGroup() {
        mainActivity.findViewById(R.id.circle1).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.circle2).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.circle3).setVisibility(View.INVISIBLE);

        mainActivity.findViewById(R.id.circle4).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.circle5).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.circle6).setVisibility(View.VISIBLE);
    }

    private void initParams() {
        p11 = new DPoint(mainActivity.findViewById(R.id.circle1).getX(), mainActivity.findViewById(R.id.circle1).getY());
        p12 = new DPoint(mainActivity.findViewById(R.id.circle2).getX(), mainActivity.findViewById(R.id.circle2).getY());
        p13 = new DPoint(mainActivity.findViewById(R.id.circle3).getX(), mainActivity.findViewById(R.id.circle3).getY());

        p21 = new DPoint(mainActivity.findViewById(R.id.circle4).getX(), mainActivity.findViewById(R.id.circle4).getY());
        p22 = new DPoint(mainActivity.findViewById(R.id.circle5).getX(), mainActivity.findViewById(R.id.circle5).getY());
        p23 = new DPoint(mainActivity.findViewById(R.id.circle6).getX(), mainActivity.findViewById(R.id.circle6).getY());
    }

    //matrix of algebraic complements
    private Matrix3x3 moac3(Matrix3x3 A) {
        Matrix3x3 finish = new Matrix3x3();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                finish.set(i, j, A.det2(i, j) * Math.pow(-1, i + j));
            }
        }
        return finish;
    }

    private Matrix3x3 inverse(Matrix3x3 start) {
        double det = start.det3();
        if (det == 0) {
            // catch this!
            Log.i("upd", "det == 0!");
            return null;
        }
        Matrix3x3 finish = reverse(moac3(start));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                finish.set(i, j, finish.get(i, j) / det);
            }
        }
        return finish;
    }

    private Matrix3x3 reverse(Matrix3x3 A) {
        Matrix3x3 finish = new Matrix3x3();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                finish.set(i, j, A.get(j, i));
            }
        }
        return finish;
    }

    private Matrix3x1 mulMatrix(Matrix3x3 A, Matrix3x1 B) {
        Matrix3x1 C = new Matrix3x1();

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                for (int r = 0; r < 3; r++)
                    C.set(i, j, C.get(i, j) + A.get(i, r) * B.get(r, j));

        return C;
    }

    private Matrix3x3 getFirstPoints() {
        return inverse(new Matrix3x3(
                p11.x, p11.y, 1.0,
                p12.x, p12.y, 1.0,
                p13.x, p13.y, 1.0));
    }

    private Matrix3x1 getSecondInACE() {
        return new Matrix3x1(p21.x, p22.x, p23.x);
    }

    private Matrix3x1 getSecondInBDF() {
        return new Matrix3x1(p21.y, p22.y, p23.y);
    }

    private Bitmap algorithm() {
        Solver solver;
        try {
            solver = new Solver(
                    mulMatrix(
                            getFirstPoints(),
                            getSecondInACE()),
                    mulMatrix(
                            getFirstPoints(),
                            getSecondInBDF()));

        } catch (NullPointerException e) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity.getApplicationContext(),
                            "Bad point setting", Toast.LENGTH_SHORT).show();
                }
            });
            return mBitmap;
        }

        final Bitmap btmp = mBitmap.copy(Bitmap.Config.ARGB_8888,
                true);
        btmp.eraseColor(Color.WHITE);
        int cnt = 0;

        for (int i = 0; i < mBitmap.getWidth(); i++) {
            for (int j = 0; j < mBitmap.getHeight(); j++) {
                DPoint image = solver.calc(new DPoint(i, j));
                int w = (int) image.x;
                int h = (int) image.y;
                if (0 > w || w >= btmp.getWidth()) continue;
                if (0 > h || h >= btmp.getHeight()) continue;
                btmp.setPixel(i, j, mBitmap.getPixel(w, h));
                if (w != i && h != j)
                    cnt++;
            }
        }
        Log.i("upd", String.format("%s", cnt));

        return btmp;
    }
}
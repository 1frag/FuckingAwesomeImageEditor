package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class LinearAlgebra extends Conductor implements View.OnTouchListener {

    private Bitmap mBitmap;
    private ImageView mImageView;
    private MainActivity mainActivity;

    private DPoint p11, p12, p13;
    private DPoint p21, p22, p23;

    private int cntfix = 0;

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
        this.mainActivity = activity;
        this.mImageView = activity.getImageView();
    }

    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.linear_algebra_menu);

        FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();
        ElevationDragFragment fragment = new ElevationDragFragment();

        FrameLayout layout = mainActivity.findViewById(R.id.sample_content_fragment);
        View circle = new View(mainActivity);
        circle.setBackgroundColor(0x00FF00);
        layout.addView(circle, 100, 100);
        // todo: я не понимаю ну вот создал я его, где он появляется то? =/

//        android:id="@+id/circle"
//        android:layout_width="@dimen/shape_size"
//        android:layout_height="@dimen/shape_size"
//        android:background="@color/Mv_color"


        transaction.replace(R.id.sample_content_fragment, fragment);
        transaction.commit();

        final Button btn_start = mainActivity.findViewById(R.id.button_start_linear_algebra);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initParams();
                AsyncTaskConductor algemAsync = new AsyncTaskConductor(){
                    @Override
                    protected Bitmap doInBackground(String... params){
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_start.setEnabled(false);
                            }
                        });
                        algorithm();
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mImageView.setImageBitmap(mBitmap);
                                btn_start.setEnabled(true);
                            }
                        });
                        return mBitmap;
                    }
                };
                algemAsync.execute();
                mImageView.invalidate();
            }
        });

        mBitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        mBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mImageView.setImageBitmap(mBitmap);
        mImageView.setOnTouchListener(this);
    }

    private void initParams() {
//        p11 = new DPoint(152, 377);
//        p12 = new DPoint(136, 50);
//        p13 = new DPoint(81, 582);
//
//        p21 = new DPoint(101, 309);
//        p22 = new DPoint(142, 502);
//        p23 = new DPoint(80, 101);
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

    private void algorithm() {

        Solver solver = new Solver(
                mulMatrix(
                        getFirstPoints(),
                        getSecondInACE()),
                mulMatrix(
                        getFirstPoints(),
                        getSecondInBDF()));

        final Bitmap btmp = mBitmap.copy(Bitmap.Config.ARGB_8888,
                true);
        btmp.eraseColor(Color.WHITE);

        for (int i = 0; i < mBitmap.getWidth(); i++) {
            for (int j = 0; j < mBitmap.getHeight(); j++) {
                DPoint image = solver.calc(new DPoint(i, j));
                int w = (int) image.x;
                int h = (int) image.y;
                if (0 > w || w >= btmp.getWidth()) continue;
                if (0 > h || h >= btmp.getHeight()) continue;
                btmp.setPixel(i, j, mBitmap.getPixel(w, h));
            }
        }
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImageView.setImageBitmap(btmp);
                mImageView.invalidate();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() != 0)return false;

        if(cntfix==0)p11 = new DPoint(event.getX(), event.getY());
        if(cntfix==1)p12 = new DPoint(event.getX(), event.getY());
        if(cntfix==2)p13 = new DPoint(event.getX(), event.getY());
        if(cntfix==3)p21 = new DPoint(event.getX(), event.getY());
        if(cntfix==4)p22 = new DPoint(event.getX(), event.getY());
        if(cntfix==5)p23 = new DPoint(event.getX(), event.getY());

        if(cntfix<3){
            for(int i=-10;i<=10;i++){
                for(int j=-10;j<=10;j++){
                    mBitmap.setPixel((int)event.getX()+i,(int)event.getY()+j,Color.BLUE);
                }
            }
        }else if(cntfix<6){
            for(int i=-10;i<=10;i++){
                for(int j=-10;j<=10;j++){
                    mBitmap.setPixel((int)event.getX()+i,(int)event.getY()+j,Color.RED);
                }
            }
        }
        mImageView.invalidate();
        cntfix++;
        return true;
    }
}

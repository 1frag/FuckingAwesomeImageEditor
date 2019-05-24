package com.example.image_editor;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class LinearAlgebra extends Controller {

    private Button mStartAlgoButton;

    private DPoint p11, p12, p13;
    private DPoint p21, p22, p23;

    LinearAlgebra(MainActivity activity) {
        super(activity);
    }

    @Override
    void touchToolbar() {
        super.touchToolbar();
        prepareToRun(R.layout.linear_algebra_menu);
        setHeader(mainActivity.getResources().getString(R.string.algem_2_0_name));

        mainActivity.drivingViews.show();

        ImageButton mSetFinishPointsButton = mainActivity.findViewById(R.id.button_finish_points);
        ImageButton mSetStartPointsButton = mainActivity.findViewById(R.id.button_start_points);
        mStartAlgoButton = mainActivity.findViewById(R.id.button_start_linear_algebra);

        configStartAlgoButton(mStartAlgoButton);
        configSetStartPointsButton(mSetStartPointsButton);
        configSetFinishPointsButton(mSetFinishPointsButton);
        configMethodInfoButton(
                mainActivity.findViewById(R.id.button_help),
                R.drawable.help_lin_alg);
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
                Log.i("upd", "qwe");
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
                @SuppressLint("StaticFieldLeak")
                AsyncTaskConductor asyncTask = new AsyncTaskConductor() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        return algorithm();
                    }
                };
                asyncTask.execute();
            }
        });
    }

    private void initMovingViewFirstGroup() {
        mainActivity.findViewById(R.id.circle1).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.circle2).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.circle3).setVisibility(View.VISIBLE);

        mainActivity.findViewById(R.id.circle4).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.circle5).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.circle6).setVisibility(View.INVISIBLE);
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

    private Bitmap algorithm() {
        ExecutorAffineTransformations solver;
        solver = new ExecutorAffineTransformations(
                p11, p12, p13, p21, p22, p23);

        if (!solver.prepare()){
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity.getApplicationContext(),
                            mainActivity.getResources().getString(R.string.points_on_one_line), Toast.LENGTH_SHORT).show();
                }
            });
            return mainActivity.getBitmap();
        }

        final Bitmap btmp = mainActivity.getBitmap().copy(
                Bitmap.Config.ARGB_8888, true);
        btmp.eraseColor(Color.WHITE);
        int cnt = 0;

        for (int i = 0; i < mainActivity.getBitmap().getWidth(); i++) {
            for (int j = 0; j < mainActivity.getBitmap().getHeight(); j++) {
                DPoint image = solver.calc(i, j);
                int w = (int) Math.round(image.x);
                int h = (int) Math.round(image.y);
                if (0 > w || w >= btmp.getWidth()) continue;
                if (0 > h || h >= btmp.getHeight()) continue;
                btmp.setPixel(i, j, mainActivity.getBitmap().getPixel(w, h));
                if (w != i && h != j)
                    cnt++;
            }
        }
        Log.i("upd", String.format("%s", cnt));

        return btmp;
    }
}
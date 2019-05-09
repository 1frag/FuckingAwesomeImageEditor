package com.example.image_editor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class Conductor{

    private MainActivity activity;
    private Bitmap beforeChanges;

    Conductor(MainActivity activity){
        this.activity = activity;
    }

    void touchToolbar(){
        Log.i("upd", "touchToolbar");
        beforeChanges = ((BitmapDrawable)activity.getImageView().getDrawable()).getBitmap();
    }

    public void setDefaultState(View view) {
        activity.inMethod = false;
        LinearLayout placeHolder = activity.findViewById(R.id.method_layout);
        RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);

        placeHolder.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        activity.findViewById(R.id.apply_layout).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.imgUndo).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.imgRedo).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.imgDownload).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.imgCamera).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.imgGallery).setVisibility(View.VISIBLE);
        activity.initClasses((1 << 9) - 1);
        activity.getImageView().setOnTouchListener(null);
    }

    void PrepareToRun(int resourse) {
        activity.inMethod = true;
        LinearLayout placeHolder = activity.findViewById(R.id.method_layout);
        RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);

        placeHolder.setVisibility(View.VISIBLE);

        final LayoutInflater factory = activity.getLayoutInflater();
        final View menu = factory.inflate(resourse, null);
        placeHolder.addView(menu, 0);

        activity.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getImageView().setImageBitmap(beforeChanges);
                activity.getImageView().invalidate();
                // todo: спросить пользователя уверен что изменения не будут применены
                // todo: не спрашивать если изменений не было
                setDefaultState(v);
            }
        });

        activity.findViewById(R.id.apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDefaultState(v);
            }
        });


        activity.findViewById(R.id.recyclerView).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.apply_layout).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.imgUndo).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.imgRedo).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.imgDownload).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.imgCamera).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.imgGallery).setVisibility(View.INVISIBLE);
    }


    class AsyncTaskConductor extends AsyncTask<String, Void, Bitmap>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activity.switchProgressBarVisibilityVisible();
            Toast.makeText(activity.getApplicationContext(), "Thread created", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = ((BitmapDrawable)activity.getImageView().getDrawable()).getBitmap();
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            activity.switchProgressBarVisibilityInvisible();
            ImageView imageView = activity.getImageView();
            imageView.setImageBitmap(result);
            Toast.makeText(activity.getApplicationContext(), "NICE", Toast.LENGTH_SHORT).show();
        }
    }

}

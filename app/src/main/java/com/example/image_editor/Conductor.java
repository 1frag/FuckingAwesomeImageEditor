package com.example.image_editor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class Conductor{

    private MainActivity activity;

    Conductor(MainActivity activity){
        this.activity = activity;
    }

    void touchToolbar(){
        Log.i("upd", "touchToolbar");
    }

    void mainActivityMenuGone(){
        activity.findViewById(R.id.imgUndo).setVisibility(View.GONE);
        activity.findViewById(R.id.imgRedo).setVisibility(View.GONE);
        activity.findViewById(R.id.imgDownload).setVisibility(View.GONE);
        activity.findViewById(R.id.imgCamera).setVisibility(View.GONE);
        activity.findViewById(R.id.imgGallery).setVisibility(View.GONE);
    }

    void mainActivityMenuVisible(){
        activity.findViewById(R.id.imgUndo).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.imgRedo).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.imgDownload).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.imgCamera).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.imgGallery).setVisibility(View.VISIBLE);
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

package com.example.image_editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

class Conductor{

    private MainActivity activity;

    Conductor(MainActivity activity){
        this.activity = activity;
    }

    void touchToolbar(){
        Log.i("upd", "touchToolbar");
    }

    class AsyncTaskConductor extends AsyncTask<Integer, Void, Bitmap>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // will set animation later
            Toast.makeText(activity.getApplicationContext(), "Thread created", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {

            int which = params[0];
            Bitmap result = null;

            Bitmap bitmap = ((BitmapDrawable)activity.getImageView().getDrawable()).getBitmap();
            switch (which) {
                case 0:
                    result = ColorFIltersCollection.movieFilter(bitmap);
                    break;
                case 1:
                    result = ColorFIltersCollection.fastBlur(bitmap);
                    break;
                case 2:
                    result = ColorFIltersCollection.createGrayScale(bitmap);
                    break;
            }
            if (result == null){
                Toast.makeText(activity.getApplicationContext(), "This wasn't supposed to happen.", Toast.LENGTH_LONG).show();
                return bitmap;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            ImageView imageView = activity.getImageView();
            imageView.setImageBitmap(result);
            Toast.makeText(activity.getApplicationContext(), "NICE", Toast.LENGTH_SHORT).show();
        }
    }

}

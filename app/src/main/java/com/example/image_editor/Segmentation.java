package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

class Segmentation extends Conductor {
    private FaceDetector mDetector;
    private String TAG = "Detect Faces";

    Segmentation(MainActivity activity) {
        super(activity);
    }

    void touchToolbar() {
        super.touchToolbar();
        prepareToRun(R.layout.segmentatioon_menu);
        setHeader(mainActivity.getResources().getString(R.string.segmentation));

        ConfigBtnFacesDetect((Button) mainActivity.findViewById(R.id.faces));

        mDetector = new FaceDetector.Builder(mainActivity)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
    }

    private void ConfigBtnFacesDetect(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskConductor asyncTask = new AsyncTaskConductor() {
                    @Override
                    protected Bitmap doInBackground(String... params) {
                        Bitmap bufBitmap = scanFaces();
                        return bufBitmap;
                    }
                };
                asyncTask.execute();
            }
        });
    }

    private Bitmap scanFaces() {
        if (mDetector.isOperational() && mainActivity.getBitmap() != null) {
            Bitmap editedBitmap = Bitmap.createBitmap(
                    mainActivity.getBitmap().getWidth(),
                    mainActivity.getBitmap().getHeight(),
                    Bitmap.Config.ARGB_8888);
            float scale = mainActivity.getResources().getDisplayMetrics().density;
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.rgb(255, 61, 61));
            paint.setTextSize((int) (14 * scale));
            paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
            Canvas canvas = new Canvas(editedBitmap);
            canvas.drawBitmap(mainActivity.getBitmap(), 0, 0, paint);
            Frame frame = new Frame.Builder().setBitmap(editedBitmap).build();
            final SparseArray<Face> faces = mDetector.detect(frame);
            for (int index = 0; index < faces.size(); ++index) {
                Face face = faces.valueAt(index);
                canvas.drawRect(
                        face.getPosition().x,
                        face.getPosition().y,
                        face.getPosition().x + face.getWidth(),
                        face.getPosition().y + face.getHeight(), paint);
            }

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity.getApplicationContext(),
                            String.format(mainActivity.getResources().getString(R.string.faces_found), faces.size()),
                            Toast.LENGTH_SHORT).show();
                }
            });
            return editedBitmap;
        } else {
            Log.i(TAG, "Could not set up the mDetector!");
            return mainActivity.getBitmap();
        }
    }
}

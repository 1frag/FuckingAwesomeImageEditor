package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

// TODO: refactoring
class Segmentation extends Conductor{

    private ImageView mImageView;
    private MainActivity mainActivity;
    private Bitmap mBitmap;
    private FaceDetector mDetector;
    private String TAG = "Detect Faces";

    Segmentation(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        mainActivity = activity;
        mImageView = activity.getImageView();
    }

    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.segmentatioon_menu);

        ConfigBtnFacesDetect((Button) mainActivity.findViewById(R.id.faces));

        mDetector = new FaceDetector.Builder(mainActivity)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        mBitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        mBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mImageView.setImageBitmap(mBitmap);
    }

    private void ConfigBtnFacesDetect(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    scanFaces();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "Problem");
                }
            }
        });
    }

    private void scanFaces() {
        if (mDetector.isOperational() && mBitmap != null) {
            Bitmap editedBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap
                    .getHeight(), mBitmap.getConfig());
            float scale = mainActivity.getResources().getDisplayMetrics().density;
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.rgb(255, 61, 61));
            paint.setTextSize((int) (14 * scale));
            paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
            Canvas canvas = new Canvas(editedBitmap);
            canvas.drawBitmap(mBitmap, 0, 0, paint);
            Frame frame = new Frame.Builder().setBitmap(editedBitmap).build();
            SparseArray<Face> faces = mDetector.detect(frame);
            for (int index = 0; index < faces.size(); ++index) {
                Face face = faces.valueAt(index);
                canvas.drawRect(
                        face.getPosition().x,
                        face.getPosition().y,
                        face.getPosition().x + face.getWidth(),
                        face.getPosition().y + face.getHeight(), paint);
            }

            mImageView.setImageBitmap(editedBitmap);
            Toast.makeText(mainActivity.getApplicationContext(),
                    String.format("%s faces found", faces.size()),
                    Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "Could not set up the mDetector!");
        }
    }

}

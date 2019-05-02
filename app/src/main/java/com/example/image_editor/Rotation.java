package com.example.image_editor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class Rotation extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap bitmap;
    private Bitmap bufferedBitmap;
    private String path;

    private String IMAGE_DIRECTORY = "/demonuts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        this.path = intent.getStringExtra("Image");

        this.imageView = (ImageView) findViewById(R.id.imageRotation);
        try {
            this.bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(this.path)));
        } catch (
                IOException e) {
            e.printStackTrace();
            Toast.makeText(Rotation.this, "Failed!", Toast.LENGTH_SHORT).show();
        }
        this.imageView.setImageBitmap(this.bitmap);

        configApplyButton();
        configSaveButton();
    }

    private void configApplyButton() {
        Button applyButton = (Button) findViewById(R.id.apply_button_rotation);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText angle = (EditText) findViewById(R.id.angle_input);

                int ang = Integer.parseInt(angle.getText().toString());

                rotateOnAngle(ang);
                // TODO: catch exceptions here
            }
        });

    }

    @SuppressLint("Assert")
    private void rotateOnAngle(int angle) {
        int x = bitmap.getWidth();
        int y = bitmap.getHeight();
        double a = (double) (90 - angle % 90) * Math.PI / 180.0;
        double cosa = Math.cos(a);
        double sina = Math.sin(a);
        double AB = x * sina + y * cosa;
        double AD = y * sina + x * cosa;
        Bitmap btmp;
        btmp = Bitmap.createBitmap((int) AB + 2, (int) AD + 2, Bitmap.Config.ARGB_8888);
        btmp = btmp.copy(Bitmap.Config.ARGB_8888, true);
        Log.i("UPD", "hi");
        for (int nx = 0; nx <= (int) AB; nx++) {
            Log.i("UPD", ((Integer)nx).toString());
            for (int ny = 0; ny <= (int) AD; ny++) {
                int w = (int) (nx * sina - ny * cosa + x * cosa * cosa);
                int h = (int) (nx * cosa + ny * sina - x * sina * cosa);
                if(((angle / 90) & 1) == 1){
                    int cnt = w;
                    w = y - h;
                    h = cnt;
                }
                if(((angle / 90) & 2) == 2){
                    h = y - h;
                    w = x - w;
                }
                if(w<0 || w>=bitmap.getWidth())continue;
                if(h<0 || h>=bitmap.getHeight())continue;
                btmp.setPixel(nx, ny, bitmap.getPixel(w, h));
            }
        }
        Log.i("UPD", "end");

        imageView.setImageBitmap(btmp);
        imageView.invalidate();
    }

    private void configSaveButton() {
        Button saveButton = (Button) findViewById(R.id.save_button_rotation);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = saveImage(bufferedBitmap);
                Toast.makeText(Rotation.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        });
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }
}

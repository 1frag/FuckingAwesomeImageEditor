package com.example.image_editor;

import android.content.Intent;
import android.graphics.Bitmap;
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

public class Scaling extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap bitmap;
    private Bitmap bufferedBitmap;
    private String path;

    private String IMAGE_DIRECTORY = "/demonuts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaling);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        this.path = intent.getStringExtra("Image");

        this.imageView = (ImageView) findViewById(R.id.imageScaling);
        try{
            this.bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(this.path)));
        }
        catch (
            IOException e) {
            e.printStackTrace();
            Toast.makeText(Scaling.this, "Failed!", Toast.LENGTH_SHORT).show();
        }
        this.imageView.setImageBitmap(this.bitmap);

        configApplyButton();
        configSaveButton();
    }

    private void configApplyButton(){
        Button applyButton = (Button) findViewById(R.id.apply_button_scaling);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText coef_width =  (EditText) findViewById(R.id.coef_width);
                EditText coef_height =  (EditText) findViewById(R.id.coef_height);
                float w = Float.parseFloat(coef_width.getText().toString());
                float h = Float.parseFloat(coef_height.getText().toString());

                resizeImage(w, h);
                // TODO: catch exceptions here
            }
        });

    }

    private void configSaveButton(){
        Button saveButton = (Button) findViewById(R.id.save_button_scaling);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = saveImage(bufferedBitmap);
                Toast.makeText(Scaling.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        });
    }

    private void resizeImage(float coef_w, float coef_h){
        Bitmap bmOriginal = this.bitmap;
        int width = bmOriginal.getWidth();
        int height = bmOriginal.getHeight();

        int newWidth = Math.round(width*coef_w);
        int newHeight = Math.round(height*coef_h);

        this.bufferedBitmap = Bitmap.createScaledBitmap(bmOriginal, newWidth,
                newHeight, false);
        this.imageView.setImageBitmap(this.bufferedBitmap);
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

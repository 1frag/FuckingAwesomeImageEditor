package com.example.image_editor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class Color_Filters extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap bitmap;
    private Bitmap bufferedBitmap;
    private String path;

    private String IMAGE_DIRECTORY = "/demonuts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color__filters);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Intent intent = getIntent();
        this.path = intent.getStringExtra("Image");

        this.imageView = (ImageView) findViewById(R.id.imageColorFilters);
        try{
            this.bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(this.path)));
        }
        catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(Color_Filters.this, "Failed!", Toast.LENGTH_SHORT).show();
        }
        this.imageView.setImageBitmap(this.bitmap);


         configSelectFilterButton();
         configSaveButton();
    }

    private void configSelectFilterButton(){
        Button selectFilterButton = (Button) findViewById(R.id.filter_selector);
        selectFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
    }

    private void configSaveButton(){
        Button saveButton = (Button) findViewById(R.id.save_button_scaling);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = saveImage(bufferedBitmap);
                Toast.makeText(Color_Filters.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        });
    }

    private void showFilterDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select color filter");
        String[] pictureDialogItems = {
                "Movie",
                "Name 2"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                movieFilter();
                                break;
                            case 1:
                                // do something
                                break;
                        }
                    }
                });
        pictureDialog.show();
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

    private void movieFilter(){
        bufferedBitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), bitmap.getConfig());

        for(int i=0; i<bitmap.getWidth(); i++){
            for(int j=0; j<bitmap.getHeight(); j++){
                int p = bitmap.getPixel(i, j);
                int r = Color.red(p);
                int g = Color.green(p);
                int b = Color.blue(p);

                b = (int) (b*0.8);
                g = (int) (g*0.9);
                bufferedBitmap.setPixel(i, j, Color.argb(Color.alpha(p), r, g, b));
            }
        }
        imageView.setImageBitmap(bufferedBitmap);

    }

}

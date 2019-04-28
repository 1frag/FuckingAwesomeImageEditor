package com.example.image_editor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class Scaling extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap bitmap;
    private String path;

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

    }

    private void configApplyButton(){
        Button filterButton = (Button) findViewById(R.id.apply_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText coef_width =  (EditText) findViewById(R.id.coef_width);
                EditText coef_height =  (EditText) findViewById(R.id.coef_height);
                int w = Integer.parseInt(coef_width.getText().toString());
                int h = Integer.parseInt(coef_height.getText().toString());

                resizeImage(w, h);
            }
        });

    }

    // TODO: add configSaveButton here

    private void resizeImage(int coef_w, int coef_h){
        Bitmap bmOriginal = this.bitmap;
        int width = bmOriginal.getWidth();
        int height = bmOriginal.getHeight();

        int newWidth = width*coef_w;
        int newHeight = height*coef_h;

        Bitmap newBitmap = Bitmap.createScaledBitmap(bmOriginal, newWidth,
                newHeight, false);
        this.imageView.setImageBitmap(newBitmap);
    }
}

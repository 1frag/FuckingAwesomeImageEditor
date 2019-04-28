package com.example.image_editor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

        imageView = (ImageView) findViewById(R.id.imageScaling);
        try{
            this.bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(this.path)));
        }
        catch (
            IOException e) {
            e.printStackTrace();
            Toast.makeText(Scaling.this, "Failed!", Toast.LENGTH_SHORT).show();
        }

        imageView.setImageBitmap(this.bitmap);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}

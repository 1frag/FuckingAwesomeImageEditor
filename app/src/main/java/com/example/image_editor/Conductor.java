package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class Conductor {

    private Button btn4;

    Conductor(Button btn4){
        this.btn4 = btn4;
    }

    void touchToolbar(){
        Log.i("upd", "touchToolbar");
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                touchRun();
            }
        });
    }

    void touchRun(){
        Log.i("upd", "touchRun");
    }

}

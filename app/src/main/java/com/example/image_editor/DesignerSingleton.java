package com.example.image_editor;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class DesignerSingleton {
    // static variable single_instance of type Singleton
    private static DesignerSingleton single_instance = null;

    // variable of type String
    public Button btn1;
    public Button btn2;
    public Button btn3;
    public Button btn4;
    public ImageView imageView;
    public ImageButton imgRedo;
    public ImageButton imgUndo;
    public TextView logger;

    // private constructor restricted to this class itself
    private DesignerSingleton(Button btn1,
                              Button btn2,
                              Button btn3,
                              Button btn4,
                              ImageView imageView,
                              ImageButton imgRedo,
                              ImageButton imgUndo,
                              TextView logger) {
        this.btn1 = btn1;
        this.btn2 = btn2;
        this.btn3 = btn3;
        this.btn4 = btn4;
        this.imageView = imageView;
        this.imgRedo = imgRedo;
        this.imgUndo = imgUndo;
        this.logger = logger;
    }

    // static method to create instance of Singleton class
    public static DesignerSingleton getInstance(Button btn1,
                                                Button btn2,
                                                Button btn3,
                                                Button btn4,
                                                ImageView imageView,
                                                ImageButton imgRedo,
                                                ImageButton imgUndo,
                                                TextView logger) {
        if (single_instance == null)
            single_instance = new DesignerSingleton(btn1,
                    btn2,
                    btn3,
                    btn4,
                    imageView,
                    imgRedo,
                    imgUndo,
                    logger);

        return single_instance;
    }
}

package com.example.image_editor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnTouchListener;

import static java.lang.Math.abs;

public class Retouch extends Conductor implements OnTouchListener {
    private MainActivity activity;

    private Bitmap bitmap;
    private Bitmap mask;
    private Bitmap bufferedBitmap;
    private Bitmap original;

    private Button applyRetouch;

    private ImageView imageView;

    private TextView textViewBrushSize;
    private TextView textViewBlurRadius;

    private SeekBar seekBarBrushSize;
    private SeekBar seekBarBlurRadius;

    private int brushSize = 1;
    private int blurRadius = 1;

    private Canvas bufferCanvas;

    Retouch(MainActivity activity) {
        super(activity);
        // work only with activity_main.xml
        this.activity = activity;
        this.imageView = activity.getImageView();
    }

    void touchToolbar() {
        super.touchToolbar();
        PrepareToRun(R.layout.retouch_menu);

        applyRetouch = activity.findViewById(R.id.btn_apply_retouch);

        textViewBrushSize = activity.findViewById(R.id.text_view_brush_size);
        textViewBlurRadius = activity.findViewById(R.id.text_view_radius);

        seekBarBlurRadius = activity.findViewById(R.id.seek_bar_blur_radius);
        seekBarBlurRadius.setMax(20);

        seekBarBrushSize = activity.findViewById(R.id.seek_bar_brush_sIze);
        seekBarBrushSize.setMax(50);

        configApplyButton(applyRetouch);

        seekBarBlurRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                blurRadius = progresValue + 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewBlurRadius.setText("Radius: " + blurRadius);
                System.out.println(blurRadius);
            }
        });

        seekBarBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                brushSize = progresValue + 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewBrushSize.setText("Brush: " + brushSize);
                System.out.println(brushSize);
            }
        });

        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        original = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        bufferedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mask = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        canvas = new Canvas(original);

        imageView.setImageBitmap(bitmap);
        imageView.setOnTouchListener(this);
    }

    private void configApplyButton(Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // PorterDuff mode
                PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;

                Paint paint = new Paint();
                paint.setXfermode(new PorterDuffXfermode(mode));

                PorterDuff.Mode mode2 = PorterDuff.Mode.DST_ATOP;

                Paint paint2 = new Paint();
                paint2.setXfermode(new PorterDuffXfermode(mode2));

                // blurred bitmap
                bufferedBitmap = ColorFIltersCollection.fastBlur(original, blurRadius, 1);

                bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                bufferCanvas = new Canvas(bitmap);

                bufferCanvas.drawBitmap(mask, 0, 0, null);
                bufferCanvas.drawBitmap(bufferedBitmap, 0, 0, paint);
                bufferCanvas.drawBitmap(original, 0, 0, paint2);

                // dump the buffer
//                activity.canvas.drawBitmap(original, 0, 0, null);
//                activity.canvas.drawBitmap(bitmap, 0, 0, null);

                imageView.setImageBitmap(bitmap);

                // TODO: need to return drawing option to user
            }
        });
    }

    private boolean canPutRect(int rad, int mx, int my) {
        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= bitmap.getWidth() ||
                        0 > my + j || my + j >= bitmap.getHeight()) {
                    continue;
                }
                if (abs(i) + abs(j) <= rad) {
                    if (bitmap.getPixel(mx + i, my + j) == Color.rgb(10, 255, 10) ||
                            bitmap.getPixel(mx + i, my + j) == Color.rgb(255, 10, 10)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void errorTouched() {
        // todo: hand this
        return;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int mx = (int) event.getX();
        int my = (int) event.getY();

        int rad = brushSize;
        if (!canPutRect(rad, mx, my)) {
            errorTouched();
            return false;
        }

        for (int i = -rad; i <= rad; i++) {
            for (int j = -rad; j <= rad; j++) {
                if (0 > mx + i || mx + i >= bitmap.getWidth()) {
                    continue;
                }
                if (0 > my + j || my + j >= bitmap.getHeight()) {
                    continue;
                }
                if (Math.sqrt(i*i + j*j) <= rad) {
                    bitmap.setPixel(mx + i, my + j, Color.RED);
                    mask.setPixel(mx + i, my + j, Color.BLUE);
                }
            }
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.invalidate();
            }
        });

        return true;
    }

}

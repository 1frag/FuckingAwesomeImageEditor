package com.example.image_editor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.ImageButton;

import android.widget.ImageView;
import android.widget.LinearLayout;

class Conductor {

    private MainActivity activity;
    private Bitmap beforeChanges;

    private ImageButton applyChanges;
    private ImageButton cancelChanges;

    Conductor(MainActivity activity) {
        this.activity = activity;
    }

    void touchToolbar() {
        Log.i("upd", "touchToolbar");
        beforeChanges = ((BitmapDrawable) activity.getImageView().getDrawable()).getBitmap();
    }

    public void setDefaultState(View view) {
        activity.inMethod = false;
        activity.imageChanged = false;
        LinearLayout placeHolder = activity.findViewById(R.id.method_layout);
        RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);

        placeHolder.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        activity.findViewById(R.id.apply_layout).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.button_undo).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.button_redo).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.button_save_image).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.button_camera).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.button_gallery).setVisibility(View.VISIBLE);
        activity.initClasses((1 << 9) - 1);
        activity.getImageView().setOnTouchListener(null);
    }

    void PrepareToRun(int resourse) {
        activity.inMethod = true;
        LinearLayout placeHolder = activity.findViewById(R.id.method_layout);
        RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);
        ImageView imageview = activity.getImageView();

//        LinearLayout ltiv = activity.findViewById(R.id.ltiv);
//        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(imageview.getLayoutParams());;
//        marginParams.setMargins(0, 10, 0, 0);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
//        ltiv.setLayoutParams(layoutParams);
//        FrameLayout fl = activity.findViewById(R.id.sample_content_fragment);
//        fl.setForegroundGravity(30);


        placeHolder.setVisibility(View.VISIBLE);

        final LayoutInflater factory = activity.getLayoutInflater();
        final View menu = factory.inflate(resourse, null);
        placeHolder.addView(menu, 0);

        cancelChanges = activity.findViewById(R.id.button_cancel_changes);
        applyChanges = activity.findViewById(R.id.button_apply_changes);

        configApplyButton(applyChanges);
        configCancelButton(cancelChanges);

        activity.findViewById(R.id.recyclerView).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.apply_layout).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.button_undo).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.button_redo).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.button_save_image).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.button_camera).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.button_gallery).setVisibility(View.INVISIBLE);
    }

    private void configCancelButton(ImageButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.imageChanged) {
                    openCancelDialog(v);
                } else setDefaultState(v);
            }
        });
    }

    private void configApplyButton(ImageButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.imageChanged) {
                    openApplyDialog(v);
                } else setDefaultState(v);
            }
        });
    }

    private void openCancelDialog(final View v) {
        // todo: UI че писать?)
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(activity);
        quitDialog.setTitle("Изменения НЕ будут применены. Продолжить?");

        quitDialog.setPositiveButton("Таки да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.getImageView().setImageBitmap(beforeChanges);
                activity.getImageView().invalidate();
                setDefaultState(v);
            }
        });

        quitDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        quitDialog.show();
    }

    private void openApplyDialog(final View v) {
        // todo: UI че писать?)
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(activity);
        quitDialog.setTitle("Изменения будут применены. Продолжить?");

        quitDialog.setPositiveButton("Таки да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.history.addBitmap(((BitmapDrawable) activity.getImageView().getDrawable()).getBitmap());
                setDefaultState(v);
            }
        });

        quitDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        quitDialog.show();
    }

    class AsyncTaskConductor extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            applyChanges.setEnabled(false);
            cancelChanges.setEnabled(false);
            activity.switchProgressBarVisibilityVisible();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = ((BitmapDrawable) activity.getImageView().getDrawable()).getBitmap();
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            activity.switchProgressBarVisibilityInvisible();
            activity.imageChanged = true;
            ImageView imageView = activity.getImageView();
            imageView.setImageBitmap(result);
            applyChanges.setEnabled(true);
            cancelChanges.setEnabled(true);
        }
    }

}

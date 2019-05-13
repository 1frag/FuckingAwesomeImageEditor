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

    private MainActivity mainActivity;
    private Bitmap mBeforeChanges;

    private ImageButton mApplyChangesButton;
    private ImageButton mCancelChangesButton;

    Conductor(MainActivity activity) {
        mainActivity = activity;
    }

    void touchToolbar() {
        Log.i("upd", "touchToolbar");
        mBeforeChanges = ((BitmapDrawable) mainActivity.getImageView().getDrawable()).getBitmap();
    }

    // TODO: maybe refactor this too?
    public void setDefaultState(View view) {
        mainActivity.inMethod = false;
        mainActivity.imageChanged = false;
        LinearLayout placeHolder = mainActivity.findViewById(R.id.method_layout);
        RecyclerView recyclerView = mainActivity.findViewById(R.id.recyclerView);

        placeHolder.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.apply_layout).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.button_undo).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.button_redo).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.button_save_image).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.button_camera).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.button_gallery).setVisibility(View.VISIBLE);
        mainActivity.initClasses((1 << 9) - 1);
        mainActivity.getImageView().setImageBitmap(mBeforeChanges);
        mainActivity.getImageView().setOnTouchListener(null);
    }

    void PrepareToRun(int resourse) {
        mainActivity.inMethod = true;
        LinearLayout placeHolder = mainActivity.findViewById(R.id.method_layout);

        placeHolder.setVisibility(View.VISIBLE);

        final LayoutInflater factory = mainActivity.getLayoutInflater();
        final View menu = factory.inflate(resourse, null);
        placeHolder.addView(menu, 0);

        mCancelChangesButton = mainActivity.findViewById(R.id.button_cancel_changes);
        mApplyChangesButton = mainActivity.findViewById(R.id.button_apply_changes);

        configApplyButton(mApplyChangesButton);
        configCancelButton(mCancelChangesButton);

        mainActivity.findViewById(R.id.recyclerView).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.apply_layout).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.button_undo).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.button_redo).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.button_save_image).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.button_camera).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.button_gallery).setVisibility(View.INVISIBLE);
    }

    private void configCancelButton(ImageButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainActivity.imageChanged) {
                    openCancelDialog(v);
                } else setDefaultState(v);
            }
        });
    }

    private void configApplyButton(ImageButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainActivity.imageChanged) {
                    openApplyDialog(v);
                } else setDefaultState(v);
            }
        });
    }

    private void openCancelDialog(final View v) {
        AlertDialog.Builder cancelDialog = new AlertDialog.Builder(mainActivity);
        cancelDialog.setTitle("Изменения будут утрачены. Продолжить?");

        cancelDialog.setPositiveButton("Таки да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainActivity.getImageView().setImageBitmap(mBeforeChanges);
                mainActivity.getImageView().invalidate();
                setDefaultState(v);
            }
        });

        cancelDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        cancelDialog.show();
    }

    // TODO: pretty useless dialog tho
    private void openApplyDialog(final View v) {
        AlertDialog.Builder applyDialog = new AlertDialog.Builder(mainActivity);
        applyDialog.setTitle("Изменения будут применены. Продолжить?");

        applyDialog.setPositiveButton("Таки да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainActivity.history.addBitmap(((BitmapDrawable) mainActivity.getImageView().getDrawable()).getBitmap());
                setDefaultState(v);
            }
        });

        applyDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        applyDialog.show();
    }

    private void lockInterface(){
        mApplyChangesButton.setEnabled(false);
        mCancelChangesButton.setEnabled(false);
        mainActivity.algoInWork = true;
        mainActivity.switchProgressBarVisibilityVisible();
    }

    private void unlockInterface(){
        mApplyChangesButton.setEnabled(true);
        mCancelChangesButton.setEnabled(true);
        mainActivity.algoInWork = false;
        mainActivity.switchProgressBarVisibilityInvisible();
    }

    class AsyncTaskConductor extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lockInterface();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = ((BitmapDrawable) mainActivity.getImageView().getDrawable()).getBitmap();
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            mainActivity.imageChanged = true;
            final ImageView imageView = mainActivity.getImageView();
            imageView.setImageBitmap(result);

            // invalidate changes once
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.invalidate();
                }
            });

            unlockInterface();
        }
    }

}

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
import android.widget.TextView;

class Conductor {
    public Bitmap bitmap; // mutable bitmap
    public Bitmap beforeChanges;

    private ImageButton mApplyChangesButton;
    private ImageButton mCancelChangesButton;

    private TextView mMethodName;

    public ImageView imageView;
    public MainActivity mainActivity;

    Conductor(MainActivity activity) {
        mainActivity = activity;
        imageView = mainActivity.getImageView();
        bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true); // to make it mutable
    }

    public Bitmap getmBeforeChanges(){
        return beforeChanges;
    }

    void touchToolbar() {
        Log.i("upd", "touchToolbar");
        beforeChanges = ((BitmapDrawable) mainActivity.getImageView().getDrawable()).getBitmap();
    }

    public void setDefaultState(View view) {
        LinearLayout placeHolder = mainActivity.findViewById(R.id.method_layout);
        RecyclerView recyclerView = mainActivity.findViewById(R.id.recyclerView);

        final LayoutInflater factory = mainActivity.getLayoutInflater();
        final View menu = factory.inflate(R.layout.main_head, null);
        mainActivity.getmHeader().removeAllViews();
        mainActivity.getmHeader().addView(menu, 0);

        placeHolder.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.apply_layout).setVisibility(View.INVISIBLE);

        // to discard some possible drawings on bitmap
        if (!mainActivity.imageChanged) mainActivity.getImageView().setImageBitmap(beforeChanges);

        mainActivity.inMethod = false;
        mainActivity.imageChanged = false;

        /* for Algem 2.0 delete moving points */
        removeFloatingPoints();

        // useless line tho
//        mainActivity.initClasses();

        mainActivity.getImageView().setOnTouchListener(null);
    }

    void prepareToRun(int resource) {
        mainActivity.inMethod = true;
        LinearLayout placeHolder = mainActivity.findViewById(R.id.method_layout);

        final LayoutInflater factory_menu = mainActivity.getLayoutInflater();
        final View menu = factory_menu.inflate(resource, null);
        placeHolder.addView(menu, 0);

        final LayoutInflater factory_head = mainActivity.getLayoutInflater();
        final View head = factory_head.inflate(R.layout.method_head, null);
        mainActivity.getmHeader().addView(head, 0);

        mCancelChangesButton = mainActivity.findViewById(R.id.button_cancel_changes);
        mApplyChangesButton = mainActivity.findViewById(R.id.button_apply_changes);

        mMethodName = mainActivity.findViewById(R.id.text_method_name);

        configApplyButton(mApplyChangesButton);
        configCancelButton(mCancelChangesButton);

        placeHolder.setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.recyclerView).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.apply_layout).setVisibility(View.VISIBLE);
    }

    // TODO: check for language
    public void setHeader(String header){
        mMethodName.setText(header);
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
                mainActivity.getImageView().setImageBitmap(beforeChanges);
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
                mainActivity.history.addBitmap(((BitmapDrawable) imageView.getDrawable()).getBitmap());
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

    public void lockInterface(){
        mApplyChangesButton.setEnabled(false);
        mCancelChangesButton.setEnabled(false);
        mainActivity.algoInWork = true;
        mainActivity.switchProgressBarVisibilityVisible();
    }

    public void unlockInterface(){
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

    private void removeFloatingPoints(){
        if(mainActivity.findViewById(R.id.circle1) != null)
            mainActivity.findViewById(R.id.circle1).setVisibility(View.GONE);
        if(mainActivity.findViewById(R.id.circle2) != null)
            mainActivity.findViewById(R.id.circle2).setVisibility(View.GONE);
        if(mainActivity.findViewById(R.id.circle3) != null)
            mainActivity.findViewById(R.id.circle3).setVisibility(View.GONE);
        if(mainActivity.findViewById(R.id.circle4) != null)
            mainActivity.findViewById(R.id.circle4).setVisibility(View.GONE);
        if(mainActivity.findViewById(R.id.circle5) != null)
            mainActivity.findViewById(R.id.circle5).setVisibility(View.GONE);
        if(mainActivity.findViewById(R.id.circle6) != null)
            mainActivity.findViewById(R.id.circle6).setVisibility(View.GONE);

    }

}

package com.example.image_editor;

import android.annotation.SuppressLint;
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

class Controller {

    private ImageButton mApplyChangesButton;
    private ImageButton mCancelChangesButton;

    private TextView mMethodName;

    private LinearLayout placeHolder;
    private RecyclerView recyclerView;

    public ImageView imageView;
    public MainActivity mainActivity;
    private ImageButton infoButton;

    Controller(MainActivity activity) {
        mainActivity = activity;
        imageView = mainActivity.getImageView();

        placeHolder = mainActivity.findViewById(R.id.method_layout);
        recyclerView = mainActivity.findViewById(R.id.recyclerView);
        infoButton = mainActivity.findViewById(R.id.button_help);

        mCancelChangesButton = mainActivity.findViewById(R.id.button_cancel_changes);
        mApplyChangesButton = mainActivity.findViewById(R.id.button_apply_changes);
    }

    void touchToolbar() {
        Log.i("upd", "touchToolbar");
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setDefaultState(View view) {
        final LayoutInflater factory = mainActivity.getLayoutInflater();
        final View menu = factory.inflate(R.layout.main_head, null);
        mainActivity.getmHeader().removeAllViews();
        mainActivity.getmHeader().addView(menu, 0);

        placeHolder.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.apply_layout).setVisibility(View.INVISIBLE);

        // to discard some possible drawings on bitmap
        if (mainActivity.imageChanged && !mainActivity.algorithmExecuted) {
            mainActivity.getImageView().setImageBitmap(
                    mainActivity.history.showHead().copy(Bitmap.Config.ARGB_8888, true));
            mainActivity.setBitmapFromImageView();
        }

        mainActivity.inMethod = false;
        mainActivity.imageChanged = false;
        mainActivity.algorithmExecuted = false;

        /* for Algem 2.0 delete moving points */
        mainActivity.drivingViews.hide();

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

        mMethodName = mainActivity.findViewById(R.id.text_method_name);

        configApplyButton(mApplyChangesButton);
        configCancelButton(mCancelChangesButton);
        imageView.setImageBitmap(mainActivity.getBitmapDrawing());

        placeHolder.setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.recyclerView).setVisibility(View.INVISIBLE);
        mainActivity.findViewById(R.id.apply_layout).setVisibility(View.VISIBLE);

        mainActivity.getImageView().setImageBitmap(mainActivity.getBitmap());
    }

    // TODO: check for language
    public void setHeader(String header){
        mMethodName.setText(header);
    }

    // TODO: override in each function, give layout as parameter and set onClickListener on public button
    private void configMethodInfoButton(LayoutInflater layout){
        return;
    }

    private void configCancelButton(ImageButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainActivity.algorithmExecuted) {
                    mainActivity.openQuitFromMethodDialog();
                } else setDefaultState(v);
            }
        });
    }

    private void configApplyButton(ImageButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mainActivity.algorithmExecuted && !mainActivity.imageChanged){
                    imageView.setImageBitmap(mainActivity.getBitmapBefore());
                    mainActivity.resetBitmap();
                    mainActivity.invalidateImageView();
//                    setDefaultState(v);
                    return;
                }
                if (mainActivity.algorithmExecuted) {
                    mainActivity.history.addBitmap(mainActivity.getBitmap());
                }
                setDefaultState(v);
            }
        });
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

    @SuppressLint("StaticFieldLeak")
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
            mainActivity.getImageView().setImageBitmap(result);
            mainActivity.setBitmapFromImageView();

            mainActivity.imageChanged = true;
            mainActivity.algorithmExecuted = true;

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

package com.example.image_editor.adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.image_editor.MainActivity;
import com.example.image_editor.R;
import com.example.image_editor.utils.ColorFIltersCollection;

import java.util.ArrayList;

public class FiltersAdapter extends RecyclerView.Adapter<FiltersAdapter.ViewHolder> {

    private MainActivity mainActivity;
    private ArrayList<String> mNamesFilters;
    private ArrayList<String> mNamesProg;

    private Bitmap mBitmap;
    private Bitmap mThumb;
    private Bitmap mBufferedBitmap;

    private boolean mClicked = false;

    public FiltersAdapter(MainActivity activity,
                   ArrayList<String> namesUser,
                   ArrayList<String> namesProg) throws NoSuchMethodException {
        mainActivity = activity;
        mNamesFilters = namesUser;
        mNamesProg = namesProg;
        mBitmap = ((BitmapDrawable) mainActivity.getImageView().getDrawable()).getBitmap();

        final int THUMBSIZE = 128;
        mThumb = ThumbnailUtils.extractThumbnail(mBitmap, THUMBSIZE, THUMBSIZE);
    }

    private void lockInterface(){
        mClicked = true;
        mainActivity.findViewById(R.id.button_apply_changes).setEnabled(false);
        mainActivity.findViewById(R.id.button_cancel_changes).setEnabled(false);
        mainActivity.algoInWork = true;
        mainActivity.switchProgressBarVisibilityVisible();
    }

    private void unlockInterface(){
        mClicked = false;
        mainActivity.findViewById(R.id.button_apply_changes).setEnabled(true);
        mainActivity.findViewById(R.id.button_cancel_changes).setEnabled(true);
        mainActivity.algoInWork = false;
        mainActivity.switchProgressBarVisibilityInvisible();
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskFilters extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lockInterface();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String which = params[0];
            String type = params[1];

            if (type.equals("thumb")) mBufferedBitmap = mThumb.copy(Bitmap.Config.ARGB_8888, true);
            if (type.equals("image")) mBufferedBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);

            switch (which) {
                case "Original":
                    mainActivity.imageChanged = false;
                    mainActivity.algorithmExecuted = false;
                    break;
                case "Movie":
                    mBufferedBitmap = ColorFIltersCollection.movieFilter(mBufferedBitmap);
                    break;
                case "Blur":
                    mBufferedBitmap = ColorFIltersCollection.fastBlur(mBufferedBitmap, 5, 1);
                    break;
                case "B&W":
                    mBufferedBitmap = ColorFIltersCollection.createGrayScale(mBufferedBitmap);
                    break;
                case "Blue laguna":
                    mBufferedBitmap = ColorFIltersCollection.lagunaFilter(mBufferedBitmap);
                    break;
                case "Contrast":
                    mBufferedBitmap = ColorFIltersCollection.adjustedContrast(mBufferedBitmap, 3);
                    break;
                case "Sephia":
                    mBufferedBitmap = ColorFIltersCollection.sephiaFilter(mBufferedBitmap);
                    break;
                case "Noise":
                    mBufferedBitmap = ColorFIltersCollection.fleaEffect(mBufferedBitmap);
                    break;
                case "Green grass":
                    mBufferedBitmap = ColorFIltersCollection.grassFilter(mBufferedBitmap);
                    break;
                case "Ruby":
                    mBufferedBitmap = ColorFIltersCollection.rubyFilter(mBufferedBitmap);
                    break;
                case "Vignette":
                    mBufferedBitmap = ColorFIltersCollection.radialMask(mBufferedBitmap);
                    break;
            }
            return mBufferedBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            mainActivity.imageChanged = true;
            mainActivity.algorithmExecuted = true;
            mainActivity.getImageView().setImageBitmap(result);
            mainActivity.setBitmapFromImageView();

            // invalidate changes once
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.getImageView().invalidate();
                }
            });

            unlockInterface();
        }
    }

    @Override
    public FiltersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        return new FiltersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FiltersAdapter.ViewHolder holder, final int position) {

        holder.name.setText(mNamesFilters.get(position));
        @SuppressLint("StaticFieldLeak")
        AsyncTaskFilters thumbAsync = new AsyncTaskFilters(){
            @Override
            protected void onPostExecute(Bitmap result) {
                holder.image.setImageBitmap(result);
                unlockInterface();
            }
        };
        thumbAsync.execute(mNamesProg.get(position), "thumb");

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClicked) return;
                AsyncTaskFilters filterAsync = new AsyncTaskFilters();
                filterAsync.execute(mNamesProg.get(position), "image");
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNamesFilters.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
            name = itemView.findViewById(R.id.name);
        }
    }
}

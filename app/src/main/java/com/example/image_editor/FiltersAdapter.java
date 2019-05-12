package com.example.image_editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;

// TODO; optimize async here
public class FiltersAdapter extends RecyclerView.Adapter<FiltersAdapter.ViewHolder> {

    private MainActivity mactivity;
    private ArrayList<String> mNamesUser = new ArrayList<>();
    private ArrayList<String> mNamesProg = new ArrayList<>();
    private ArrayList<Bitmap> mImageThumbs = new ArrayList<>();

    private Bitmap bitmap;
    private Bitmap thumb;

    private int THUMBSIZE = 128;

    FiltersAdapter(MainActivity activity,
                   ArrayList<String> namesUser,
                   ArrayList<String> namesProg,
                   ArrayList<Bitmap> imageThumbs) throws NoSuchMethodException {
        mactivity = activity;
        mNamesUser = namesUser;
        mNamesProg = namesProg;
        mImageThumbs = imageThumbs;
        bitmap = ((BitmapDrawable)mactivity.getImageView().getDrawable()).getBitmap();
        thumb = ThumbnailUtils.extractThumbnail(bitmap, THUMBSIZE, THUMBSIZE);
    }

    /* it is copy-paste-code */
    class AsyncTaskConductor extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mactivity.switchProgressBarVisibilityVisible();
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            String which = params[0];

            Bitmap bufferedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            switch (which) {
                case "Original":
                    bufferedBitmap = bitmap;
                    break;
                case "Movie":
                    bufferedBitmap = ColorFIltersCollection.movieFilter(bitmap);
                    break;
                case "Blur":
                    bufferedBitmap = ColorFIltersCollection.fastBlur(bitmap, 5, 1);
                    break;
                case "B&W":
                    bufferedBitmap = ColorFIltersCollection.createGrayScale(bitmap);
                    break;
                case "Blue laguna":
                    bufferedBitmap = ColorFIltersCollection.lagunaFilter(bitmap);
                    break;
                case "Contrast":
                    bufferedBitmap = ColorFIltersCollection.adjustedContrast(bitmap, 3);
                    break;
                case "Sephia":
                    bufferedBitmap = ColorFIltersCollection.sephiaFilter(bitmap);
                    break;
                case "Noise":
                    bufferedBitmap = ColorFIltersCollection.fleaEffect(bitmap);
                    break;
                case "Green grass":
                    bufferedBitmap = ColorFIltersCollection.grassFilter(bitmap);
                    break;
            }
            if (bufferedBitmap == null){
                Toast.makeText(mactivity.getApplicationContext(), "This wasn't supposed to happen.", Toast.LENGTH_LONG).show();
                return bitmap;
            }
            return bufferedBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            ImageView imageView = mactivity.getImageView();
            mactivity.switchProgressBarVisibilityInvisible();
            imageView.setImageBitmap(result);
        }
    }
    /* end CPC*/



    @Override
    public FiltersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        return new FiltersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FiltersAdapter.ViewHolder holder, final int position) {

        holder.name.setText(mNamesUser.get(position));

        AsyncTaskConductor thumbAsync = new AsyncTaskConductor(){
            @Override
            protected void onPreExecute() {
                return;
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                String which = params[0];

                Bitmap bufferedBitmap = thumb.copy(Bitmap.Config.ARGB_8888, true);
                switch (which) {
                    case "Original":
                        bufferedBitmap = thumb;
                        break;
                    case "Movie":
                        bufferedBitmap = ColorFIltersCollection.movieFilter(thumb);
                        break;
                    case "Blur":
                        bufferedBitmap = ColorFIltersCollection.fastBlur(thumb, 5, 1);
                        break;
                    case "B&W":
                        bufferedBitmap = ColorFIltersCollection.createGrayScale(thumb);
                        break;
                    case "Blue laguna":
                        bufferedBitmap = ColorFIltersCollection.lagunaFilter(thumb);
                        break;
                    case "Contrast":
                        bufferedBitmap = ColorFIltersCollection.adjustedContrast(thumb, 3);
                        break;
                    case "Sephia":
                        bufferedBitmap = ColorFIltersCollection.sephiaFilter(thumb);
                        break;
                    case "Noise":
                        bufferedBitmap = ColorFIltersCollection.fleaEffect(thumb);
                        break;
                    case "Green grass":
                        bufferedBitmap = ColorFIltersCollection.grassFilter(thumb);
                        break;
                }
                if (bufferedBitmap == null){
                    Toast.makeText(mactivity.getApplicationContext(), "This wasn't supposed to happen.", Toast.LENGTH_LONG).show();
                    return thumb;
                }
                return bufferedBitmap;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                thumb = result;
                holder.image.setImageBitmap(thumb);
            }
        };
        thumbAsync.execute(mNamesProg.get(position));


        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTaskConductor filterAsync = new AsyncTaskConductor();
                filterAsync.execute(mNamesProg.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImageThumbs.size();
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

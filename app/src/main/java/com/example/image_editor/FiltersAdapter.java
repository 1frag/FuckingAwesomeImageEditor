package com.example.image_editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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

public class FiltersAdapter extends RecyclerView.Adapter<FiltersAdapter.ViewHolder> {

    private MainActivity mactivity;
    private ArrayList<String> mNamesUser = new ArrayList<>();
    private ArrayList<String> mNamesProg = new ArrayList<>();
    private ArrayList<Integer> mImageUrls = new ArrayList<>();

    private Bitmap bitmap;

    FiltersAdapter(MainActivity activity,
                   ArrayList<String> namesUser,
                   ArrayList<String> namesProg,
                   ArrayList<Integer> imageUrls) throws NoSuchMethodException {
        mactivity = activity;
        mNamesUser = namesUser;
        mNamesProg = namesProg;
        mImageUrls = imageUrls;
        bitmap = ((BitmapDrawable)mactivity.getImageView().getDrawable()).getBitmap();
    }

    /* it is copy-paste-code */
    class AsyncTaskConductor extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mactivity.switchProgressBarVisibilityVisible();
            Toast.makeText(mactivity.getApplicationContext(), "Thread created", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            String which = params[0];

            Bitmap bufferedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            mactivity.history.addBitmap(bitmap);
            switch (which) {
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
            Toast.makeText(mactivity.getApplicationContext(), "NICE", Toast.LENGTH_SHORT).show();
        }
    }
    /* end CPC*/



    @Override
    public FiltersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        return new FiltersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FiltersAdapter.ViewHolder holder, final int position) {

        holder.image.setImageResource(mImageUrls.get(position));

        holder.name.setText(mNamesUser.get(position));

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
        return mImageUrls.size();
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

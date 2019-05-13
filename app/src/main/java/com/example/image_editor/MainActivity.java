package com.example.image_editor;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;
    private Bitmap mBitmap;

    private RecyclerView recyclerView;
    private LinearLayout placeHolder;

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<Integer> mImageUrls = new ArrayList<>();
    private ArrayList<Conductor> mClasses = new ArrayList<>();

    private ImageButton UndoButton;
    private ImageButton RedoButton;

    private ProgressBar progressBar;

    private static final String IMAGE_DIRECTORY = "/awesome";
    private int GALLERY = 1, CAMERA = 2;
    private int mInitialColor;

    public boolean inMethod = false; // set true if you in method
    public boolean imageChanged = false; // check image for changes
    public boolean algoInWork = false; // check task in background
    private boolean mPhotoChosen = false; // false if photo is default

    public History history;

    public LinearLayout getPlaceHolder() {
        return placeHolder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        this.mImageView = findViewById(R.id.iv);

        this.placeHolder = findViewById(R.id.method_layout);
        this.recyclerView = findViewById(R.id.recyclerView);

        mImageView.setMaxHeight((int) (height * 0.585));

        this.progressBar = (ProgressBar) findViewById(R.id.progressbar_main);
        switchProgressBarVisibilityInvisible();

        history = new History();
        history.clearAllAndSetOriginal(((BitmapDrawable) mImageView.getDrawable()).getBitmap());

        getLayoutInflater().inflate(
                R.layout.apply_menu,
                (LinearLayout)findViewById(R.id.apply_layout));

        UndoButton = (ImageButton) findViewById(R.id.button_undo);
        RedoButton = (ImageButton) findViewById(R.id.button_redo);

        configRedoButton();
        configUndoButton();

        getImages();
    }

    public void initClasses(int ID) {
        if (mClasses.size() == 0) {
            for (int i = 0; i < 9; i++)
                mClasses.add(new Conductor(this));
        }
        if ((ID & (1 << 0)) > 0) mClasses.set(0, new A_Star(this));
        if ((ID & (1 << 1)) > 0) mClasses.set(1, new Algem(this));
        if ((ID & (1 << 2)) > 0) mClasses.set(2, new Rotation(this));
        if ((ID & (1 << 3)) > 0) mClasses.set(3, new LinearAlgebra(this));
        if ((ID & (1 << 4)) > 0) mClasses.set(4, new Color_Filters(this));
        if ((ID & (1 << 5)) > 0) mClasses.set(5, new Retouch(this));
        if ((ID & (1 << 6)) > 0) mClasses.set(6, new Scaling(this));
        if ((ID & (1 << 7)) > 0) mClasses.set(7, new Segmentation(this));
        if ((ID & (1 << 8)) > 0) mClasses.set(8, new Usm(this));
        initRecyclerView();
    }

    private void initRecyclerView() {
        Log.d("upd", "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mNames, mImageUrls, mClasses);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (inMethod){
            if (algoInWork) return; // алгоритм ещё работает!
            else if (imageChanged) openQuitFromMethodDialog(); // уверен, что выйти из метода
            else (new Conductor(MainActivity.this)).setDefaultState(null); // выход из метода, если изменений не было
        }
        else openQuitDialog(); // уверен, что выйти из приложения
    }

    private void configRedoButton(){
        RedoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("REDO");
                mBitmap = history.takeFromBuffer();
                if (mBitmap == null){
                    Toast.makeText(getApplicationContext(), "Nothing to show", Toast.LENGTH_SHORT).show();
                }
                else mImageView.setImageBitmap(mBitmap);
            }
        });
    }

    private void configUndoButton(){
        UndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBitmap = history.popBitmap();
                if (mBitmap == null){
                    Toast.makeText(getApplicationContext(), "Whoops, something went wrong", Toast.LENGTH_SHORT).show();
                }
                else mImageView.setImageBitmap(mBitmap);
            }
        });
    }

    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
        quitDialog.setTitle("Выход: Вы уверены?");

        quitDialog.setPositiveButton("Таки да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
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

    private void openQuitFromMethodDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
        quitDialog.setTitle("Изменения будут применены. Продолжить?");

        quitDialog.setPositiveButton("Таки да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                (new Conductor(MainActivity.this)).setDefaultState(null);
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

    public void switchProgressBarVisibilityVisible(){
        this.progressBar.setVisibility(View.VISIBLE);
    }

    public void switchProgressBarVisibilityInvisible(){
        this.progressBar.setVisibility(View.GONE);
    }

    private void getImages() {
        Log.d("upd", "initImageBitmaps: preparing bitmaps.");
        initClasses((1 << 9) - 1);

        mImageUrls.add(R.drawable.icon_a_star); // 0
        mNames.add("A*");

        mImageUrls.add(R.drawable.icon_spline); // 1
        mNames.add("Spline");

        mImageUrls.add(R.drawable.icon_rotate); // 2
        mNames.add("Rotate");

        mImageUrls.add(R.drawable.icon_billinear_filter); // 3
        mNames.add("Bilinear filter");

        mImageUrls.add(R.drawable.icon_color_filters); // 4
        mNames.add("Filters");

        mImageUrls.add(R.drawable.icon_retouch); // 5
        mNames.add("Retouch");

        mImageUrls.add(R.drawable.icon_scale); // 6
        mNames.add("Scale");

        mImageUrls.add(R.drawable.icon_segmentation); // 7
        mNames.add("Segmentation");

        mImageUrls.add(R.drawable.icon_sharpness); // 8
        mNames.add("Sharpness");

        initRecyclerView();
    }

    public void choosePhotoFromGallery(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    public void takePhotoFromCamera(View view) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    /* legacy code starts here */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    this.mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);

                    double c = Math.min(
                            ((double) mImageView.getWidth()/ mBitmap.getWidth()),
                            ((double) mImageView.getHeight()/ mBitmap.getHeight()));
                    mBitmap = (new Scaling(this)).algorithm(mBitmap, (float) c);

                    if (this.mBitmap.getByteCount() > 10000000) {
                        Toast.makeText(getApplicationContext(), "Your photo is too large!", Toast.LENGTH_SHORT).show();
//                       return;
                    }
//                    this.path = saveImage(mBitmap); todo: test: is it correct? (not saved!)
                    Toast.makeText(MainActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    mImageView.setImageBitmap(mBitmap);
                    mPhotoChosen = true;
                    history.clearAllAndSetOriginal(mBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            this.mBitmap = (Bitmap) data.getExtras().get("data");

            double c = Math.min(
                    ((double) mImageView.getWidth()/ mBitmap.getWidth()),
                    ((double) mImageView.getHeight()/ mBitmap.getHeight()));
            mBitmap = (new Scaling(this)).algorithm(mBitmap, (float) c);

            mImageView.setImageBitmap(this.mBitmap);
//            this.path = saveImage(this.mBitmap); todo: 129 _0_0_
            Toast.makeText(MainActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
            mPhotoChosen = true;
            history.clearAllAndSetOriginal(mBitmap);
        }
    }

    public void saveImage(View view) {
        // todo: think about loading from private mBitmap or mImageView??

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        // todo: toast or logs
    }

    private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            //openSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }


    public ImageView getImageView() {
        return mImageView;
    }

    public void click_finish(View view) {
        Log.i("upd", ((Integer) view.getId()).toString());
        Log.i("upd", ((Integer) R.id.button_finish_a_star).toString());
    }


    public void btnSelectColor(View view) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, mInitialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mInitialColor = color;

            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // cancel was selected by the user
            }
        });
        dialog.show();
    }
    /* legacy code finish here */
}
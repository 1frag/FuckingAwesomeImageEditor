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
import android.widget.TextView;
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
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {

    private Button btn;
    private ImageView imageview;
    private TextView textview;
    private Bitmap bitmap;
    private String path;
    private static final String IMAGE_DIRECTORY = "/demonuts";
    private int GALLERY = 1, CAMERA = 2;
    private boolean photoChosen = false;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList myDataset = new ArrayList();
    private LinearLayout placeHolder;
    public boolean inMethod = false;
    public boolean imageChanged = false;
    private int initialColor;

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<Integer> mImageUrls = new ArrayList<>();
    private ArrayList<Conductor> mClasses = new ArrayList<>();

    private ProgressBar progressBar;

//    private DesignerSingleton managerDesign;
//    private Button load_from_cam;
//    private Button load_from_gallery;
//    private ImageView imageView;
//    private RecyclerView methods;
    private ImageButton undo;
    private ImageButton redo;

    public History history;
//    private Button download;
//    private TextView hint;
//
//    Defolt_station(DesignerSingleton managerDesign) {
//        this.managerDesign = managerDesign;
//        this.load_from_cam = managerDesign.imgCamera;
//        this.load_from_gallery = managerDesign.imgGallery;
//        this.imageView = managerDesign.iv;
//        this.methods = managerDesign.recyclerView;
//        this.undo = managerDesign.imgUndo;
//        this.redo = managerDesign.imgRedo;
//        this.download = managerDesign.imgDownload;
//        this.hint = managerDesign.logger;
//    }

    public LinearLayout getPlaceHolder() {
        return placeHolder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // todo: what is rool good tone this. or it is redundant?)
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        this.imageview = findViewById(R.id.iv);

        this.placeHolder = findViewById(R.id.method_layout);
        this.recyclerView = findViewById(R.id.recyclerView);

//        LinearLayout ltiv = findViewById(R.id.ltiv);

        imageview.setMaxHeight((int) (height * 0.585));

//        LinearLayout.LayoutParams params = new
//                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
        // Set the height by params
//        params.height=100;
        // set height of RecyclerView
//        recyclerView.setLayoutParams(params);

        this.progressBar = (ProgressBar) findViewById(R.id.progressbar_main);
        switchProgressBarVisibilityInvisible();

        history = new History();
        history.clearAllAndSetOriginal(((BitmapDrawable) imageview.getDrawable()).getBitmap());

        getLayoutInflater().inflate(
                R.layout.apply_menu,
                (LinearLayout)findViewById(R.id.apply_layout));


        undo = (ImageButton) findViewById(R.id.button_undo);
        redo = (ImageButton) findViewById(R.id.button_redo);

        configRedoButton();
        configUndoButton();

        getImages();
    }

    private void configRedoButton(){
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("REDO");
                bitmap = history.takeFromBuffer();
                if (bitmap == null){
                    Toast.makeText(getApplicationContext(), "Nothing to show", Toast.LENGTH_SHORT).show();
                }
                else imageview.setImageBitmap(bitmap);
            }
        });
    }

    // TODO: проверка, были ли изменения
    @Override
    public void onBackPressed() {
//         super.onBackPressed();
        if (inMethod){
            if (imageChanged) openQuitFromMethodDialog(); // уверен, что выйти из метода
            else (new Conductor(MainActivity.this)).setDefaultState(null); // выход из метода, если изменений не было
        }
        else openQuitDialog(); // уверен, что выйти из приложения
    }

    private void openQuitDialog() {
        // todo: UI че писать?)
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
        quitDialog.setTitle("Выход: Вы уверены?");

        quitDialog.setPositiveButton("Таки да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                finish();
            }
        });

        quitDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });

        quitDialog.show();
    }

    private void openQuitFromMethodDialog() {
        // todo: UI че писать?)
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

    private void configUndoButton(){
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = history.popBitmap();
                if (bitmap == null){
                    Toast.makeText(getApplicationContext(), "Whoops, something went wrong", Toast.LENGTH_SHORT).show();
                }
                else imageview.setImageBitmap(bitmap);
            }
        });
    }

    public void switchProgressBarVisibilityVisible(){
        this.progressBar.setVisibility(View.VISIBLE);
    }

    public void switchProgressBarVisibilityInvisible(){
        this.progressBar.setVisibility(View.GONE);
    }

    void initClasses(int ID) {
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

    private void initRecyclerView() {
        Log.d("upd", "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mNames, mImageUrls, mClasses);
        recyclerView.setAdapter(adapter);
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
                    this.bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);

                    double c = Math.min(
                            ((double)imageview.getWidth()/bitmap.getWidth()),
                            ((double)imageview.getHeight()/bitmap.getHeight()));
                    bitmap = (new Scaling(this)).algorithm(bitmap, (float) c);

                    if (this.bitmap.getByteCount() > 10000000) {
                        Toast.makeText(getApplicationContext(), "Your photo is too large!", Toast.LENGTH_SHORT).show();
//                       return;
                    }
//                    this.path = saveImage(bitmap); todo: test: is it correct? (not saved!)
                    Toast.makeText(MainActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    imageview.setImageBitmap(bitmap);
                    photoChosen = true;
                    history.clearAllAndSetOriginal(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            this.bitmap = (Bitmap) data.getExtras().get("data");

            double c = Math.min(
                    ((double)imageview.getWidth()/bitmap.getWidth()),
                    ((double)imageview.getHeight()/bitmap.getHeight()));
            bitmap = (new Scaling(this)).algorithm(bitmap, (float) c);

            imageview.setImageBitmap(this.bitmap);
//            this.path = saveImage(this.bitmap); todo: 129 _0_0_
            Toast.makeText(MainActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
            photoChosen = true;
            history.clearAllAndSetOriginal(bitmap);
        }
    }

    public void saveImage(View view) {
        // todo: think about loading from private bitmap or imageview??

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
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
        return imageview;
    }

    public void click_finish(View view) {
        Log.i("upd", ((Integer) view.getId()).toString());
        Log.i("upd", ((Integer) R.id.button_finish_a_star).toString());
    }


    public void btnSelectColor(View view) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                initialColor = color;

            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // cancel was selected by the user
            }
        });
        dialog.show();
    }
}
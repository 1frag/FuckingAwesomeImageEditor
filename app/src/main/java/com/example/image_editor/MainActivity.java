package com.example.image_editor;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Instrumentation;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.textclassifier.TextClassification;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStreamWriter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;

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

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<Conductor> mClasses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // todo: what is rool good tone this. or it is redundant?)
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        this.imageview = findViewById(R.id.iv);

        DesignerSingleton managerDesign = DesignerInit();
        getImages(managerDesign);
    }

    private DesignerSingleton DesignerInit() {
        return DesignerSingleton.getInstance(
                (Button) findViewById(R.id.btn1),
                (Button) findViewById(R.id.btn2),
                (Button) findViewById(R.id.btn3),
                (Button) findViewById(R.id.btn4),
                (ImageView) findViewById(R.id.iv),
                (ImageButton) findViewById(R.id.imgRedo),
                (ImageButton) findViewById(R.id.imgUndo),
                (TextView) findViewById(R.id.logger)
        );
    }

    private void getImages(DesignerSingleton managerDesign) {
        Log.d("fuck", "initImageBitmaps: preparing bitmaps.");

        mImageUrls.add("https://lh3.googleusercontent.com/XakVfCC74Ek2JAod2cqM8Z69nY0HGfS5GWPQlwOD-t9xaiW_0DgB58w6fYrL3VAdrGr2OD7gGvu3j_OuY6evNoMpu5kL8jNmLLoPxvxmIMEicZ5JH7QAlHcadS_kfXAKh72vOX80rWdg5EMSm2j9carmwgIs6kHEzOzZcCRBb8KwcjqJ0N8K_FLqh0e9HpmiANsNzirVJi1HqNb3rhnXdO-E7H5q-xoFmdLoEdhU81Kdb0v0C5rhzPlFw3CuAnSQF2y-E2XdhtIHg3rRMdBY4vV38D_zdSlYZz-YG82QccRcCdkcdaNPqKdhM8If_aNBybbbAbByK5tKuOQ9luzi052b-YadVNqVCtDkSU1__KhunXgzNV74Cp3_tY2c9uh4BJeGs7LH6ae2CBx_X9ENlKdYxQjp4TmGJooIUvcL6KnDDH8V03itU63eKoa0E7LYfLKj5oHYYtSLixLTE9o6FCfBzILM7V_SwizIS9GjyWorAI9OkStapJnYMtnZqJSoLFCOBsUaSok8lzaap_i6tkKgiRD_NT6_1jpzDcJIsZuWJxjpXR4RViQb3YOXjXfsJtWOISpDXp2guO_rZMmPBYE2sDDSVXfYbD1PR-4=w250-h238-p-k-nu");
        mNames.add("A*");
        mClasses.add(new A_Star(managerDesign));

        mImageUrls.add("https://lh3.googleusercontent.com/62loR7NsEz_knsBGi9HNDDXdWUPwWDHIiINdAVtocvMseItXxXpqT1xIHsiyytXWTX7e34WjBLYLPe6zu3VzosFFQAzdt7sO7Bd0ac0YXFtefodRNkMdknuaraW1dQIgCdJCZRCXQC3grWF3BT729DhtYPwzbVDMWoU1Zwsbovt0bTwftpLsjBe190XqsZS80izsK6Huz_oMBaDlj9_gZ1JwaaZGzw8yVSEyFihzG9b__fs1gXWrMaqYig8kQS7JNB7V7cyENN1oPs-oBa2mi2rX03EQSeVz7pEynjJxVLxlkcidMl6hyRePYUAv54izzTd9gIIFLz9uj6Lzu5Jb9IjyDx_QIt5meW_1sXNJgrTxmhzyYn54n5KSZLFe246Swy3plvTCZyFybzka-L4pzqxPSOd3kLem6DmqEz9liXTLq-_XjjW9CWvnO6cgtsX5ES-yMlsPW1vWCY7Dfymgv0VpKHuX-L5z_hJMW3CSrcQM0zhaWR6_AQlHkXquAgtz5GJ_p8po_lpoP2g00ZrHBx2v8FJg56kyBKwGZDcVU9oDYfeVZjyYoZHDxu4eUxfteCeRusVW1toqRpib5xGueW4JhQxQ-Mq7dRfoQtM=w250-h238-p-k-nu");
        mNames.add("Interpolation");
        mClasses.add(new algem(managerDesign));

        mImageUrls.add("https://lh3.googleusercontent.com/WTW5WswCvAGIrvHUkOurpnRDOdkS2nO4amFUxH7MFvQohjzDfFkrGeKBhR5bix6YUwFqPl5XWyHw__u01GHmc20YFT3eS2xrF1nQeC67HZqfZgJYDhk91HVb9AP5OqUsM9Pr6jYTE9aEcQCI4gPexxhuwh8tX0V5Zm_CvtqNW8LDqhWS3wyyNu3kFkRgKStfsfp0fkpNkoSWiRDED4TiRfCxOAfTr4RMWrRSgSks1vbk6S5RSEVujkVfnUXCdCmM0ri8kNzc9eMXlM-kI3upckqlp8ddxRWfB9iz4d-m4cUh0qUgAqFzI6d6U8QPT9jNDvLdpGuiZughaOrEY9W5gB8x8tBUDYszBkD2Z0H8eP5qWMjy77An92rM6xgzSipnPIVMyF-fT_zmsStApfCvQVYtihTnvpYO3RjubOMT7TUripM6rL0kS7MG3Wp5AaGZLhkzBGdSX0bTso8flhtpskJEwoA80oWm7AZjvZKB3E8VcOdvcOEwXkFKMsIFYg1iH_pb9ZTVeRlu7_vpfFilzIeiqTGbcYZ0a5682sIYNFgv10g4yggSwdAZqwSfilE0tysrCB60GGoXzywXhKYWAU-0Ywx_tsdY1JRYDWk=w250-h238-p-k-nu");
        mNames.add("Rotate");
        mClasses.add(new A_Star(managerDesign));

        mImageUrls.add("https://lh3.google.com/u/0/d/1-oY1hAQXpDlTNabvjRq3dMFz9KggF5o2=w250-h238-p-k-nu-iv1");
        mNames.add("Bilinear filter");
        mClasses.add(new A_Star(managerDesign));

        mImageUrls.add("https://lh3.google.com/u/0/d/1hHmJqYuwuXQRRYeCaPA2iKZugZ2DzQqG=w250-h238-p-k-nu-iv1");
        mNames.add("three linear");
        mClasses.add(new A_Star(managerDesign));

        mImageUrls.add("https://lh3.google.com/u/0/d/1Wrowvm_9pE0Pz-eRtr3-TfupmaxcdYS4=w250-h238-p-k-nu-iv1");
        mNames.add("Retouch");
        mClasses.add(new A_Star(managerDesign));

        mImageUrls.add("https://lh3.google.com/u/0/d/1uIy6ClqP5o4rA6c-oI9rcRIXqkpyhEbA=w250-h238-p-k-nu-iv1");
        mNames.add("Scale");
        mClasses.add(new A_Star(managerDesign));

        mImageUrls.add("https://lh3.google.com/u/0/d/1E09hl9Bxi_PG17MhxhEojqi8QCXZs3Nq=w250-h238-p-k-nu-iv1");
        mNames.add("Segmentation");
        mClasses.add(new A_Star(managerDesign));

        mImageUrls.add("https://lh3.google.com/u/0/d/15kNFyAOAMP6dbZC18q4eZ8L_EkmU1PoZ=w250-h238-p-k-nu-iv1");
        mNames.add("Sharpness");
        mClasses.add(new A_Star(managerDesign));

        mImageUrls.add("https://lh3.google.com/u/0/d/18j7zyWCV47KfHHCwMT6EJnUOCTDVz5Qe=w250-h238-p-k-nu-iv1");
        mNames.add("spline");
        mClasses.add(new A_Star(managerDesign));

        initRecyclerView();

    }

    private void initRecyclerView() {
        Log.d("fuck", "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mNames, mImageUrls, mClasses);
        recyclerView.setAdapter(adapter);
    }

    private void showPictureDialog() {
//        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
//        pictureDialog.setTitle("Select Action");
//        String[] pictureDialogItems = {
//                "Select photo from gallery",
//                "Capture photo from camera"};
//        pictureDialog.setItems(pictureDialogItems,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        switch (which) {
//                            case 0:
//                                choosePhotoFromGallery();
//                                break;
//                            case 1:
//                                takePhotoFromCamera();
//                                break;
//                        }
//                    }
//                });
//        pictureDialog.show();
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
                    // if photo is too big
                    if (this.bitmap.getByteCount() > 10000000) {
                        Toast.makeText(getApplicationContext(), "Your photo is too large!", Toast.LENGTH_SHORT).show();
                        return;
                    }
//                    this.path = saveImage(bitmap); todo: test: is it correct? (not saved!)
                    Toast.makeText(MainActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                    imageview.setImageBitmap(bitmap);
                    photoChosen = true;

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            this.bitmap = (Bitmap) data.getExtras().get("data");
            imageview.setImageBitmap(this.bitmap);
//            this.path = saveImage(this.bitmap); todo: 129 _0_0_
            Toast.makeText(MainActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
            photoChosen = true;
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

    private void configFiltersButton() {
//        Button filterButton = (Button) findViewById(R.id.filter_picker);
//        filterButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (photoChosen) {
//                    Intent intent = new Intent(MainActivity.this, Filters.class);
//                    intent.putExtra("Image", path);
//                    startActivity(intent);
//                }
//                else{
//                    Toast.makeText(getApplicationContext(), "You need to choose photo first!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

    }

    public void click_a_star(View view) {
        // todo: показать где-то три кнопки
        // todo: реагировать на них
        // todo: присабачить алгоритм
    }
}
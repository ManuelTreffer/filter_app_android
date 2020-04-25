package com.treffer.filterapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

   final int GALLERY_REQUEST_CODE = 100;
   private TextView choosePicture;
   private Button chooseFilter;
   private ImageView showSelectedImage;
   private Array matrixArray;
   private Spinner dropdown;
   private Button saveImage;
   private static final int MY_PERMISSION_REQUEST = 1;
   private String currentImage = "";

    private static final float[] NORMAL = {
            1,     0,     0,    0,   0, // red
            0,     1,     0,    0,   0, // green
            0,     0,     1,    0,   0, // blue
            0,     0,     0,    1,   0  // alpha
    };

   private static final float[] INVERS = {
            -1.0f,     0,     0,    0, 255, // red
            0, -1.0f,     0,    0, 255, // green
            0,     0, -1.0f,    0, 255, // blue
            0,     0,     0, 1.0f,   0  // alpha
    };

    private static final float[] SEPIA = {
            0.393f, 0.769f, 0.189f, 0,0, //
            0.349f,0.686f,0.168f,   0,0, //
            0.272f,0.534f,0.131f,0,0, //
            0, 0, 0, 1, 0, //
    };

    private static final float[] GREYSCALE = {
            0.3f,     0.59f,  0.11f,    0, 0, // red
            0.3f,     0.59f,  0.11f,    0, 0, // green
            0.3f,     0.59f,  0.11f,    0, 0, // blue
            0,     0,     0, 1.0f,   0  // alpha
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Color Filter Android Native");

        showSelectedImage = findViewById(R.id.showImage);
        dropdown = findViewById(R.id.spinner);
        saveImage = findViewById(R.id.saveImage);
        chooseFilter = findViewById(R.id.filterButton);
     /*   chooseFilter.setText("Filter anwenden");
        chooseFilter.setBackgroundColor(Color.rgb(255, 165, 0)); */
        choosePicture = findViewById(R.id.choosePicture);
        choosePicture.setText("Klicken Sie hier, um ein Bild auszuwählen");

        choosePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFromGallery();
            }
        });

        chooseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeColorFilter();
            }
        });

        final String[] filters = new String[]{"Normal", "Invers", "Sepia", "Greyscale"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, filters);
        dropdown.setAdapter(adapter);
        dropdown.setBackgroundColor(getResources().getColor(R.color.grey));

        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageView = findViewById(R.id.showImage);
                Bitmap bitmap = getScreenshot(imageView);
                currentImage = "img" + System.currentTimeMillis() + ".jpg";
                store(bitmap, currentImage);
            }
        });

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        } else {

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode){
            case MY_PERMISSION_REQUEST: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                    }
                }else {
                    Toast.makeText(this, "Kein Zugriff erlaubt", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private static Bitmap getScreenshot(View view){
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void store(Bitmap bm, String fileName){
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Filterimages";
        File dir = new File(dirPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File file = new File(dirPath, fileName);
        try{
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "Gespeichert", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);

        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data){
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    Uri selectedImage = data.getData();
                    showSelectedImage.setImageURI(selectedImage);
                    choosePicture.setTextColor(Color.rgb(255, 255, 255));
            }
    }

    public void changeColorFilter(){

        String selectedItem = dropdown.getSelectedItem().toString();

        if(selectedItem == "Invers"){
            showSelectedImage.getDrawable();
            showSelectedImage.setColorFilter(new ColorMatrixColorFilter(INVERS));
        }else if(selectedItem == "Sepia"){
            showSelectedImage.getDrawable();
            showSelectedImage.setColorFilter(new ColorMatrixColorFilter(SEPIA));
        }else if(selectedItem == "Normal") {
            showSelectedImage.getDrawable();
            showSelectedImage.setColorFilter(new ColorMatrixColorFilter(NORMAL));
        }else if(selectedItem == "Greyscale"){
            showSelectedImage.getDrawable();
            showSelectedImage.setColorFilter(new ColorMatrixColorFilter(GREYSCALE));
        }

    }

}

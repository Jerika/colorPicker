package com.my.project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements TouchImageView.OnColorPickedListener{

    final int REQUEST_CODE_PHOTO = 1;
    final int REQUEST_CODE_GALLERY = 2;
    final int REQUEST_CAMERA_PERMISSION = 3;
    final String TAG = "myLogs";

    DataBaseHelper helper;
    Bitmap imageToSave;

  //  @BindView(R.id.main_button)
    FloatingActionsMenu menu;
  //  @BindView(R.id.ivPhoto)
    TouchImageView ivPhoto;
 //   @BindView(R.id.imageView)
    public ImageView color;
 //   @BindView(R.id.firstColorName)
    public TextView colorFirstName;
 ////   @BindView(R.id.firstColor)
    public ImageView colorFirst;
 //   @BindView(R.id.secondColorName)
    public TextView colorSecondName;
 //   @BindView(R.id.secondColor)
    public ImageView colorSecond;
  //  @BindView(R.id.thirdColorName)
    public TextView colorThirdName;
 //   @BindView(R.id.thirdColor)
    public ImageView colorThird;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    //    ButterKnife.bind(this);
        helper = new DataBaseHelper(this);
        try {
            helper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        helper.openDataBase();
        ivPhoto = (TouchImageView) findViewById(R.id.ivPhoto);
        color = (ImageView) findViewById(R.id.imageView);

        colorFirst = (ImageView) findViewById(R.id.firstColor);
        colorFirstName = (TextView) findViewById(R.id.firstColorName);
        colorSecond = (ImageView) findViewById(R.id.secondColor);
        colorSecondName = (TextView) findViewById(R.id.secondColorName);
        colorThird = (ImageView) findViewById(R.id.thirdColor);
        colorThirdName = (TextView) findViewById(R.id.thirdColorName);

        ivPhoto.setImageResource(R.drawable.rainbow);
        ivPhoto.setMaxZoom(10);
        ivPhoto.setOnColorPickedListener(this);
        menu = (FloatingActionsMenu) findViewById(R.id.main_button);
        if (savedInstanceState != null) {
            imageToSave = savedInstanceState.getParcelable("bitmap");
            if (imageToSave == null) {
                ivPhoto.setImageResource(R.drawable.rainbow);
            } else {
                ivPhoto.setImageBitmap(imageToSave);
          }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isCameraPermissionsGranted() {
        return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (!isPermissionsGranted(grantResults)) {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                } else {
                    startCamera();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean isPermissionsGranted(int[] grantResults) {
        return grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    public void onClickPhoto(View view) {
        menu.collapse();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!isCameraPermissionsGranted()) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                startCamera();
            }
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        File f;
        if (isSDPresent) {
            f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
        }else{
            f = new File(getApplicationContext().getFilesDir(), "temp.jpg");
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        startActivityForResult(intent, 1);
    }

    public void onClickGallery(View view) {
        menu.collapse();
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        switch (requestCode) {
            case REQUEST_CODE_GALLERY:
                if (resultCode == RESULT_OK) {
                    ivPhoto.resetZoom();
                    Uri selectedImageUri = intent.getData();
                    String selectedImagePath = getPath(selectedImageUri);
                    imageToSave  = decodeSampledBitmapFromResource(selectedImagePath, 700, 700 );
                    ivPhoto.setImageBitmap(imageToSave);
                }
                break;
            case REQUEST_CODE_PHOTO:
                if (resultCode == RESULT_OK) {
                    ivPhoto.resetZoom();
                    Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
                    File f;
                    if (isSDPresent) {
                        f = new File(Environment.getExternalStorageDirectory().toString());
                    }else{
                        f = new File(getApplicationContext().getFilesDir().toString());
                    }
                    for (File temp : f.listFiles()) {
                        if (temp.getName().equals("temp.jpg")) {
                            f = temp;
                            break;
                        }
                    }
                    try {
                        imageToSave  = decodeSampledBitmapFromResource(f.getAbsolutePath(), 700, 700 );
                        ivPhoto.setImageBitmap(imageToSave);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                 } else if (resultCode == RESULT_CANCELED) {
                    Log.d(TAG, "Canceled");
                }
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(
        BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        super.onSaveInstanceState(toSave);
        toSave.putParcelable("bitmap", imageToSave);
    }

    @Override
    public void onColorPicked(int colorRGB) {
        if (colorRGB != 0 ){
            int redValue = Color.red(colorRGB);
            int greenValue = Color.green(colorRGB);
            int blueValue = Color.blue(colorRGB);
            ArrayList<DataBaseHelper.Colors> colorsFromDB = helper.getColor(redValue, greenValue, blueValue);

            colorFirst.setBackgroundColor(colorsFromDB.get(0).color);
            colorFirstName.setText(colorsFromDB.get(0).name);

            colorSecond.setBackgroundColor(colorsFromDB.get(1).color);
            colorSecondName.setText(colorsFromDB.get(1).name);

            colorThird.setBackgroundColor(colorsFromDB.get(2).color);
            colorThirdName.setText(colorsFromDB.get(2).name);

            color.setBackgroundColor(colorRGB);
        }
    }
}

package com.my.project;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements TouchImageView.OnColorPickedListener{

    final int REQUEST_CODE_PHOTO = 1;
    final int REQUEST_CODE_GALLERY = 2;
    final String TAG = "myLogs";
    TouchImageView ivPhoto;
    DataBaseHelper helper;
    Bitmap imageToSave;
    FloatingActionsMenu menu;
    public ImageView color;
    public TextView colorFirstName;
    public ImageView colorFirst;
    public TextView colorSecondName;
    public ImageView colorSecond;
    public TextView colorThirdName;
    public ImageView colorThird;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        ivPhoto.setImageResource(R.drawable.color_picker);
        ivPhoto.setMaxZoom(10);
        ivPhoto.setOnColorPickedListener(this);
        menu = (FloatingActionsMenu) findViewById(R.id.main_button);
        if (savedInstanceState != null) {
            imageToSave = savedInstanceState.getParcelable("bitmap");
            if (imageToSave == null) {
                ivPhoto.setImageResource(R.drawable.color_picker);
            } else {
                ivPhoto.setImageBitmap(imageToSave);
          }
        }
    }

    public void onClickPhoto(View view) {
        menu.collapse();
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

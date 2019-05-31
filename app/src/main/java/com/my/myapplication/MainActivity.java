package com.my.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.ImageQuality;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements TouchImageView.OnColorPickedListener {

    final int REQUEST_CODE_PHOTO = 1;
    final int REQUEST_CODE_GALLERY = 2;
    final int REQUEST_CAMERA_PERMISSION = 3;
    final String TAG = "myLogs";

    DataBaseHelper helper;
    Bitmap imageToSave;

    //  @BindView(R.id.main_button)
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
        setContentView(R.layout.activity_main2);
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
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickPhoto();
            }
        });
    }

    public void onClickPhoto() {
        Options options = Options.init()
                .setRequestCode(RequestCode.PICK_PHOTO_REQUEST_CODE.getId())
                .setCount(1)
                .setFrontfacing(true)
                .setImageQuality(ImageQuality.HIGH);
        Pix.start(this, options);
    }

    public void onClickGallery(View view) {
        Options options = Options.init()
                .setRequestCode(RequestCode.PICK_PHOTO_REQUEST_CODE.getId())
                .setCount(1)
                .setFrontfacing(true)
                .setImageQuality(ImageQuality.HIGH);
        Pix.start(this, options);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            ivPhoto.resetZoom();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(intent.getStringArrayListExtra(Pix.IMAGE_RESULTS).get(0), options);
            if (bitmap != null) {
                ivPhoto.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        super.onSaveInstanceState(toSave);
        toSave.putParcelable("bitmap", imageToSave);
    }

    @Override
    public void onColorPicked(int colorRGB) {
        if (colorRGB != 0) {
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

    public enum RequestCode {
        PICK_PHOTO_REQUEST_CODE,
        PICK_FILE_REQUEST_CODE;

        public static final int MINIMAL_PERMISSION_REQUEST_CODE = 1;

        public int getId() {
            return MINIMAL_PERMISSION_REQUEST_CODE + ordinal();
        }
    }
}

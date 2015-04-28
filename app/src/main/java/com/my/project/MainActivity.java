package com.my.project;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.io.File;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    File directory;
    final int REQUEST_CODE_PHOTO = 1;
    final int REQUEST_CODE_GALLERY = 2;
    final String TAG = "myLogs";
    TouchImageView ivPhoto;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    Matrix m;
    private float oldDist = 1f;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private int mode = NONE;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private float[] lastEvent = null;
    private float d = 0f;
    private float newRot = 0f;
    static DataBaseHelper helper;
    Bitmap imageToSave;
    FloatingActionsMenu menu;
    public static ImageView color;
    public static TextView colorName;
    private String selectedImagePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createDirectory();
        helper = new DataBaseHelper(this);
        try {
            helper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        helper.openDataBase();
        ivPhoto = (TouchImageView) findViewById(R.id.ivPhoto);
        color = (ImageView) findViewById(R.id.imageView);
        colorName = (TextView) findViewById(R.id.textView);
        ivPhoto.setImageResource(R.drawable.color_picker);
        ivPhoto.setMaxZoom(10);
        menu = (FloatingActionsMenu) findViewById(R.id.main_button);
        if (savedInstanceState != null) {
            imageToSave = savedInstanceState.getParcelable("bitmap");
            if (imageToSave == null) {
                ivPhoto.setImageResource(R.drawable.color_picker);
            } else {
                ivPhoto.setScaleType(ImageView.ScaleType.MATRIX);
                matrix.reset();
                ivPhoto.setImageBitmap(imageToSave);
                setTouch();
            }
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
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

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
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
                    Uri selectedImageUri = intent.getData();
                    selectedImagePath = getPath(selectedImageUri);
                    ivPhoto.setImageURI(selectedImageUri);
                    imageToSave = ((BitmapDrawable)ivPhoto.getDrawable()).getBitmap();
                }
                break;
            case REQUEST_CODE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //ivPhoto.setScaleType(ImageView.ScaleType.MATRIX);
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

                        imageToSave = BitmapFactory.decodeFile(f.getAbsolutePath());
                        imageToSave = Bitmap.createScaledBitmap(imageToSave, ivPhoto.getWidth(), ivPhoto.getHeight(), true);
                        //ivPhoto.setImageMatrix(matrix);
                        ivPhoto.setImageBitmap(imageToSave);


                       /* String path = android.os.Environment
                                .getExternalStorageDirectory()
                                + File.separator
                                + "Phoenix" + File.separator + "default";
                        f.delete();
                        OutputStream outFile = null;
                        File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                        try {
                            outFile = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                            outFile.flush();
                            outFile.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /*if (intent == null) {
                        ivPhoto.setImageURI(foto);
                        setTouch();
                        Log.d(TAG, "Intent is null");
                    } else {
                        Log.d(TAG, "Photo uri: " + intent.getData());
                        Bundle bndl = intent.getExtras();
                        if (bndl != null) {
                            Object obj = intent.getExtras().get("data");
                            if (obj instanceof Bitmap) {
                                Bitmap bitmap = (Bitmap) obj;
                                Log.d(TAG, "bitmap " + bitmap.getWidth() + " x " + bitmap.getHeight());
                                ivPhoto.setImageBitmap(bitmap);
                                setTouch();
                            }
                        }
                    }*/
                } else if (resultCode == RESULT_CANCELED) {
                    Log.d(TAG, "Canceled");
                }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void FitImageview(Bitmap image){
        if (image.getWidth() < image.getHeight()){

        }

       /* if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
       else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)*/
    }

    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;*/
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void createDirectory() {
        directory = new File(
                String.valueOf(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)));
        if (!directory.exists())
            directory.mkdirs();
    }

    public void setTouch() {
//        ivPhoto.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                ImageView view = (ImageView) v;
//                switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                    case MotionEvent.ACTION_DOWN:
//                       // matrix.reset();
//                        savedMatrix.set(matrix);
//                        start.set(event.getX(), event.getY());
//                        mode = DRAG;
//                        lastEvent = null;
//                        break;
//                    case MotionEvent.ACTION_POINTER_DOWN:
//                        oldDist = spacing(event);
//                        if (oldDist > 10f) {
//                            //matrix.reset();
//                            savedMatrix.set(matrix);
//                            midPoint(mid, event);
//                            mode = ZOOM;
//                        }
//                        /*lastEvent = new float[4];
//                        lastEvent[0] = event.getX(0);
//                        lastEvent[1] = event.getX(1);
//                        lastEvent[2] = event.getY(0);
//                        lastEvent[3] = event.getY(1);
//                        //d = rotation(event);*/
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        float eventX = event.getX();
//                        float eventY = event.getY();
//                        float[] eventXY = new float[]{eventX, eventY};
//                        Matrix invertMatrix = new Matrix();
//                        ((ImageView) v).getImageMatrix().invert(invertMatrix);
//                        invertMatrix.mapPoints(eventXY);
//                        int x = (int) eventXY[0];
//                        int y = (int) eventXY[1];
//                        Drawable imgDrawable = view.getDrawable();
//                        Bitmap bitmap = ((BitmapDrawable) imgDrawable).getBitmap();
//                        try {
//                            int touchedRGB = bitmap.getPixel(x, y);
//                            int redValue = Color.red(touchedRGB);
//                            int blueValue = Color.blue(touchedRGB);
//                            int greenValue = Color.green(touchedRGB);
//                            //Toast.makeText(getApplicationContext(), helper.getColor(redValue, greenValue, blueValue),Toast.LENGTH_SHORT).show();
//                            colorName.setText(helper.getColor(redValue, greenValue, blueValue));
//                            color.setBackgroundColor(touchedRGB);
//                        } catch (IllegalArgumentException ignored) {
//                        }
//                        break;
//                    case MotionEvent.ACTION_POINTER_UP:
//                        mode = NONE;
//                        lastEvent = null;
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        if (mode == DRAG) {
//                            matrix.set(savedMatrix);
//                            float dx = event.getX() - start.x;
//                            float dy = event.getY() - start.y;
//                            matrix.postTranslate(dx, dy);
//                        } else if (mode == ZOOM) {
//                            float newDist = spacing(event);
//                            if (newDist > 10f) {
//                                matrix.set(savedMatrix);
//                                float scale = (newDist / oldDist);
//                                matrix.postScale(scale, scale, mid.x, mid.y);
//                            }
//                        }
//                        break;
//                }
//                view.setImageMatrix(matrix);
//                return true;
//            }
//        });
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        super.onSaveInstanceState(toSave);
        toSave.putParcelable("bitmap", imageToSave);
    }

    public static void setColorName(int redValue, int greenValue,int blueValue, int touchedRGB){

        colorName.setText(helper.getColor(redValue, greenValue, blueValue));
        color.setBackgroundColor(touchedRGB);
    }
}

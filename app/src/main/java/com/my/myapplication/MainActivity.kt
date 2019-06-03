package com.my.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.fxn.utility.ImageQuality
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.content_main2.*


class MainActivity : AppCompatActivity(), TouchImageView.OnColorPickedListener {
    lateinit var helper: DataBaseHelper
    private var imageToSave: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        helper = DataBaseHelper(this)
        helper.createDataBase()
        helper.openDataBase()

        ivPhoto.setImageResource(R.drawable.rainbow)
        ivPhoto.maxZoom = 10f
        ivPhoto.onColorPickedListener = this
        fab.setOnClickListener { onClickPhoto() }
    }

    private fun onClickPhoto() {
        val options = Options.init()
                .setRequestCode(RequestCode.PICK_PHOTO_REQUEST_CODE.id)
                .setCount(1)
                .setFrontfacing(true)
                .setImageQuality(ImageQuality.HIGH)
        Pix.start(this, options)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  intent: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            ivPhoto.resetZoom()
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bitmap = BitmapFactory.decodeFile(intent!!.getStringArrayListExtra(Pix.IMAGE_RESULTS)[0], options)
            if (bitmap != null) {
                ivPhoto.setImageBitmap(bitmap)
            }
        }
    }

    public override fun onSaveInstanceState(toSave: Bundle) {
        super.onSaveInstanceState(toSave)
        toSave.putParcelable("bitmap", imageToSave)
    }

    override fun onColorPicked(colorRGB: Int) {
        if (colorRGB != 0) {
            val redValue = Color.red(colorRGB)
            val greenValue = Color.green(colorRGB)
            val blueValue = Color.blue(colorRGB)
            val colorsFromDB = helper.getColor(redValue, greenValue, blueValue)

            firstColor.setBackgroundColor(colorsFromDB[0].color)
            firstColorName.text = colorsFromDB[0].name

            secondColor.setBackgroundColor(colorsFromDB[1].color)
            secondColorName.text = colorsFromDB[1].name

            thirdColor.setBackgroundColor(colorsFromDB[2].color)
            thirdColorName.text = colorsFromDB[2].name

            imageView.setBackgroundColor(colorRGB)
        }
    }

    enum class RequestCode {
        PICK_PHOTO_REQUEST_CODE;

        val id: Int
            get() = MINIMAL_PERMISSION_REQUEST_CODE + ordinal

        companion object {
            const val MINIMAL_PERMISSION_REQUEST_CODE = 1
        }
    }
}

package com.my.myapplication

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class DataBaseHelper
/**
 * Constructor
 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
 *
 * @param context - app context
 */
internal constructor(private val myContext: Context) : SQLiteOpenHelper(myContext, DB_NAME, null, 1) {
    private var myDataBase: SQLiteDatabase? = null

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    internal fun createDataBase() {
        val dbExist = checkDataBase()
        if (!dbExist) {
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.readableDatabase
            try {
                copyDataBase()
            } catch (e: IOException) {
                throw Error("Error copying database")
            }

        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private fun checkDataBase(): Boolean {
        var checkDB: SQLiteDatabase? = null
        try {
            val myPath = DB_PATH + DB_NAME
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY)
        } catch (e: SQLiteException) {
            //database does't exist yet.
        }

        checkDB?.close()
        return checkDB != null
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    @Throws(IOException::class)
    private fun copyDataBase() {
        //Open your local db as the input stream
        val myInput = myContext.assets.open(DB_NAME)
        // Path to the just created empty db
        val outFileName = DB_PATH + DB_NAME
        //Open the empty db as the output stream
        val myOutput = FileOutputStream(outFileName)
        //transfer bytes from the inputfile to the outputfile
        val buffer = ByteArray(1024)
        var length: Int
        while (myInput.read(buffer) > 0) {
            length = myInput.read(buffer)
            myOutput.write(buffer, 0, length)
        }
        //Close the streams
        myOutput.flush()
        myOutput.close()
        myInput.close()

    }

    @Throws(SQLException::class)
    internal fun openDataBase() {
        val myPath = DB_PATH + DB_NAME
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY)
    }

    @Synchronized
    override fun close() {
        if (myDataBase != null)
            myDataBase!!.close()
        super.close()
    }

    override fun onCreate(db: SQLiteDatabase) {}

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    internal fun getColor(red: Int, green: Int, blue: Int): ArrayList<Colors> {
        val colorsFromDB = ArrayList<Colors>()
        val cursor = myDataBase?.rawQuery("SELECT c.*, ( (c.R-" + red + ")*(c.R-" + red + ") " +
                " +  (c.G-" + green + ")*(c.G-" + green + ")  +  (c.B-" + blue + ")*(c.B-" + blue + ") )" +
                " AS `distance` FROM colors as c ORDER BY `distance` ASC LIMIT 3", null)
        if (cursor != null) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val tempColor = Colors()
                tempColor.name = cursor.getString(cursor.getColumnIndex("title"))
                tempColor.color = Color.rgb(cursor.getInt(cursor.getColumnIndex("R")),
                        cursor.getInt(cursor.getColumnIndex("G")), cursor.getInt(cursor.getColumnIndex("B")))
                colorsFromDB.add(tempColor)
                cursor.moveToNext()
            }
        }
        return colorsFromDB
    }

    internal inner class Colors {
        var name: String? = null
        var color: Int = 0
    }

    companion object {
        //The Android's default system path of your application database.
        private val DB_PATH = "/data/data/com.my.myapplication/databases/"
        private val DB_NAME = "colors.db"
    }
}
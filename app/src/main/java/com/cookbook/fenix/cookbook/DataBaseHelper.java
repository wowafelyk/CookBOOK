package com.cookbook.fenix.cookbook;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

/**
 * File used for handle SQLite database
 * <p/>
 * Created by fenix on 01.07.2015.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "recipesDB.db";
    private static final String TABLE_NAME = "recipes";
    private static final String KEY_ID = "id";                  //primary key
    private static final String KEY_IMG_URL = "img url";
    private static final String KEY_TITLE = "object title";
    private static final String KEY_SOCIAL_RANK = "social rank";
    private static final String KEY_PUBLISHER = "publisher";
    private static final String KEY_INGREDIENTS = "ingredients";
    private static final String KEY_RECIPEID = "recipeID";
    private Context context;

    public static final String TEST = "test";


    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                    + KEY_ID + " INTEGER PRIMARY KEY," + KEY_IMG_URL + " TEXT,"
                    + KEY_PUBLISHER + " TEXT," + KEY_SOCIAL_RANK + " TEXT" + KEY_TITLE + " TEXT" + KEY_RECIPEID + " TEXT" + ")";
            db.execSQL(CREATE_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void drop() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE " + TABLE_NAME);
        onCreate(db);
        db.close();
    }

    public void addRecipe(Recipe data) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //values.put(KEY_INGREDIENTS, data.getIngredients().toString());
        values.put(KEY_PUBLISHER, data.getPublisher());
        values.put(KEY_SOCIAL_RANK, data.getSocialRank());
        values.put(KEY_TITLE, data.getTitle());
        values.put(KEY_IMG_URL, data.getImgURL());
        db.insert(TABLE_NAME, null, values);
        Log.d(TEST, db.getMaximumSize() + " = DatabaseHandler");
        db.close();
    }

    public Recipe getRecipe(int id) {
        Recipe data = new Recipe();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{KEY_IMG_URL, KEY_RECIPEID,
                        KEY_PUBLISHER, KEY_SOCIAL_RANK, KEY_TITLE},
                KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            data = new Recipe(cursor.getString(4), cursor.getString(1), cursor.getString(3),
                    cursor.getString(1), cursor.getString(0));
        }
        return data;
    }

    /**
     * used for gettingt all data from database
     */
    public LinkedList<Recipe> getRecipes() {
        LinkedList<Recipe> linkedList = new LinkedList<Recipe>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{KEY_IMG_URL, KEY_RECIPEID,
                        KEY_PUBLISHER, KEY_SOCIAL_RANK, KEY_TITLE},
                null, null, null, null, KEY_ID + " DESC");
        if (cursor.moveToFirst()) {
            do {
                linkedList.add(new Recipe(cursor.getString(4), cursor.getString(1), cursor.getString(3),
                        cursor.getString(1), cursor.getString(0)));
            } while (cursor.moveToNext());
        }
        db.close();
        return linkedList;
    }


    /**
     * metod for copy database to SDCard of phone or tab
     */
    public void exportDB() {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = "/data/" + "com.example.fenix.timecounter" + "/databases/" + DATABASE_NAME;
        String backupDBPath = DATABASE_NAME;
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(context, "DB Exported!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

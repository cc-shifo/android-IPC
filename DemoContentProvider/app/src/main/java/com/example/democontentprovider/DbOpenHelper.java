package com.example.democontentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 17-11-13.
 */

public class DbOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "DbOpenHelper";
    private static final String DB_NAME = "book_provider.db";
    private static final String TABLE_BOOK = "book";
    private static final String TABLE_USER = "user";
    private static final int DB_VERSION = 1;

    private String CREATE_TABLE_BOOK = "CREATE TABLE IF NOT EXISTS " + TABLE_BOOK
            + "(_id INTEGER PRIMARY KEY, " + "name TEXT)";
    private String CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS " + TABLE_USER
            + "(_id INTEGER PRIMARY KEY, " + "name TEXT, " + "sex INT)";

    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BOOK);
        db.execSQL(CREATE_TABLE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

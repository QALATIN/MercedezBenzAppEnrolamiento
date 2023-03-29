package com.latinid.mercedes.db.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.latinid.mercedes.db.SQLConstants;


public class DBHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, SQLConstants.DB, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQLConstants.SQL_CREATE_TABLE_USER);
        sqLiteDatabase.execSQL(SQLConstants.SQL_CREATE_TABLE_ENROLLMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

package com.latinid.mercedes.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.latinid.mercedes.db.SQLConstants;
import com.latinid.mercedes.db.helpers.DBHelper;
import com.latinid.mercedes.db.model.ActiveEnrollment;
import com.latinid.mercedes.db.model.User;

import java.util.ArrayList;
import java.util.List;

public class DataBase {

    private Context context;
    private SQLiteDatabase sqLiteDatabase;
    private SQLiteOpenHelper sqLiteOpenHelper;

    public DataBase(Context context) {
        this.context = context;
        sqLiteOpenHelper = new DBHelper(context);
        sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();
    }

    public void open() {
        sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();
    }

    public void close() {
        sqLiteDatabase.close();
    }



    public void insertUser(User user) {
        ContentValues values = user.toValues();
        sqLiteDatabase.insert(SQLConstants.tableUser, null, values);
    }

    public void deleteUser() {
        sqLiteDatabase.execSQL("delete from " + SQLConstants.tableUser);
    }

    public int updateUser(User user) {
        ContentValues values = user.toValues();
        return sqLiteDatabase.update(SQLConstants.tableUser, values, null, null);
    }

    @SuppressLint("Range")
    public List<User> getUsers() {
        List<User> userList = new ArrayList<>();
        User user;
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.query(SQLConstants.tableUser,
                    SQLConstants.ALL_COLUMNS_USER,
                    "user_id>0 ",
                    null,
                    null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    user = new User();
                    user.setUser_id(cursor.getString(cursor.getColumnIndex(SQLConstants.USER_ID)));
                    user.setName((cursor.getString(cursor.getColumnIndex(SQLConstants.USER_NAME))));
                    user.setPass((cursor.getString(cursor.getColumnIndex(SQLConstants.USER_PASS))));
                    user.setRegistration_date((cursor.getString(cursor.getColumnIndex(SQLConstants.USER_REGISTRATION_DATE))));
                    user.setActive((cursor.getString(cursor.getColumnIndex(SQLConstants.USER_ACTIVE))));
                    user.setFinger_id((cursor.getString(cursor.getColumnIndex(SQLConstants.USER_FINGER_ID))));
                    userList.add(user);
                }while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return userList;
    }


    public void insertEnroll(ActiveEnrollment activeEnrollment) {
        ContentValues values = activeEnrollment.toValues();
        sqLiteDatabase.insert(SQLConstants.tableEnrrollment, null, values);
    }

    public void deleteEnroll() {
        sqLiteDatabase.execSQL("delete from " + SQLConstants.tableEnrrollment);
    }

    public int updateEnroll(ActiveEnrollment activeEnrollment, String enroll_id) {
        ContentValues values = activeEnrollment.toValues();
        return sqLiteDatabase.update(SQLConstants.tableEnrrollment, values, "enroll_id="+enroll_id, null);
    }

    @SuppressLint("Range")
    public List<ActiveEnrollment> getEnrolls() {
        List<ActiveEnrollment> userList = new ArrayList<>();
        ActiveEnrollment activeEnrollment;
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.query(SQLConstants.tableEnrrollment,
                    SQLConstants.ALL_COLUMNS_ENROLL,
                    "enroll_id>0 ",
                    null,
                    null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    activeEnrollment = new ActiveEnrollment();
                    activeEnrollment.setEnroll_id(cursor.getString(cursor.getColumnIndex(SQLConstants.ENROLL_ID)));
                    activeEnrollment.setState_id((cursor.getString(cursor.getColumnIndex(SQLConstants.STATE_ID))));
                    activeEnrollment.setDate((cursor.getString(cursor.getColumnIndex(SQLConstants.DATE))));
                    activeEnrollment.setSignature_id((cursor.getString(cursor.getColumnIndex(SQLConstants.SIGNATURE_ID))));
                    activeEnrollment.setIdent_id((cursor.getString(cursor.getColumnIndex(SQLConstants.IDENT_ID))));
                    activeEnrollment.setFinger_id((cursor.getString(cursor.getColumnIndex(SQLConstants.FINGER_ID))));
                    activeEnrollment.setDocs_id((cursor.getString(cursor.getColumnIndex(SQLConstants.DOCS_ID))));
                    activeEnrollment.setJson_signature((cursor.getString(cursor.getColumnIndex(SQLConstants.JSON_SIGNATURE))));
                    activeEnrollment.setJson_ident((cursor.getString(cursor.getColumnIndex(SQLConstants.JSON_IDENT))));
                    activeEnrollment.setJson_pers((cursor.getString(cursor.getColumnIndex(SQLConstants.JSON_PERS))));
                    activeEnrollment.setJson_finger((cursor.getString(cursor.getColumnIndex(SQLConstants.JSON_FINGER))));
                    activeEnrollment.setJson_docs((cursor.getString(cursor.getColumnIndex(SQLConstants.JSON_DOCS))));
                    activeEnrollment.setFolio((cursor.getString(cursor.getColumnIndex(SQLConstants.FOLIO))));
                    activeEnrollment.setSolicitante_id((cursor.getString(cursor.getColumnIndex(SQLConstants.SOLICITANTE_ID))));
                    activeEnrollment.setTipo_enroll((cursor.getString(cursor.getColumnIndex(SQLConstants.TIPO_ENROLL))));
                    activeEnrollment.setEnroll_solicitante((cursor.getString(cursor.getColumnIndex(SQLConstants.ENROLL_SOLICITANTE))));
                    userList.add(activeEnrollment);
                }while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return userList;
    }
}

package com.latinid.mercedes.db.model;

import android.content.ContentValues;

import com.latinid.mercedes.db.SQLConstants;

public class User {
    private String user_id;
    private String name;
    private String pass;
    private String registration_date;
    private String active;
    private String finger_id;

    public User() {
    }

    public User(String user_id, String name, String pass, String registration_date, String active, String finger_id) {
        this.user_id = user_id;
        this.name = name;
        this.pass = pass;
        this.registration_date = registration_date;
        this.active = active;
        this.finger_id = finger_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getRegistration_date() {
        return registration_date;
    }

    public void setRegistration_date(String registration_date) {
        this.registration_date = registration_date;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getFinger_id() {
        return finger_id;
    }

    public void setFinger_id(String finger_id) {
        this.finger_id = finger_id;
    }

    public ContentValues toValues(){
        ContentValues contentValues = new ContentValues(6);
        contentValues.put(SQLConstants.USER_ID, user_id);
        contentValues.put(SQLConstants.USER_NAME, name);
        contentValues.put(SQLConstants.USER_PASS, pass);
        contentValues.put(SQLConstants.USER_REGISTRATION_DATE, registration_date);
        contentValues.put(SQLConstants.USER_ACTIVE, active);
        contentValues.put(SQLConstants.USER_FINGER_ID, finger_id);
        return contentValues;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", name='" + name + '\'' +
                ", pass='" + pass + '\'' +
                ", registration_date='" + registration_date + '\'' +
                ", active='" + active + '\'' +
                ", finger_id='" + finger_id + '\'' +
                '}';
    }
}

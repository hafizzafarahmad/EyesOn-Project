package com.princedev.eyesonapp.Models;

/**
 * Created by Fizz on 30/08/2018.
 */

public class Friend {

    public String date, uid;

    public Friend() {
    }

    public Friend(String date, String uid) {
        this.date = date;
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

package com.birthdaywish.surajvikash.chatapp.DataModels;

/**
 * Created by surajvikash on 30/05/18.
 */

public class Friends {

    public Friends(){

    }

    public Friends(String date) {
        this.date = date;
    }

    String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

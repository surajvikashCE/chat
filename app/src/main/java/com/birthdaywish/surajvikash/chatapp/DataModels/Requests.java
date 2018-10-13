package com.birthdaywish.surajvikash.chatapp.DataModels;

/**
 * Created by surajvikash on 04/06/18.
 */

public class Requests {

    String requestType;

    public Requests(){

    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public Requests(String requestType) {

        this.requestType = requestType;
    }
}

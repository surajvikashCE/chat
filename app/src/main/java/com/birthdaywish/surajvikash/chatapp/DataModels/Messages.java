package com.birthdaywish.surajvikash.chatapp.DataModels;

/**
 * Created by surajvikash on 31/05/18.
 */

public class Messages {
    String message, from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Messages(){

    }

    public Messages(String from) {
        this.from = from;
    }

    public Messages(String message, String from, String type, long time, boolean seen) {
        this.message = message;
        this.from = from;
        this.type = type;
        this.time = time;
        this.seen = seen;
    }

    public Messages(String message, String type, long time, boolean seen) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
    }

    String type;
    long time;
    boolean seen;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}

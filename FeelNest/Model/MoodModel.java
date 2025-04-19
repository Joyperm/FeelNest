package com.example.FeelNest.Model;

public class MoodModel {
    String date;
    String mood;
    String note;
    long createAt;
    String Turl;

    public MoodModel() {
    }

    public MoodModel(String turl,long createAt, String date, String mood, String note ) {
        this.date = date;
        this.mood = mood;
        this.note = note;
        this.createAt = createAt;
        Turl = turl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    public String getTurl() {
        return Turl;
    }

    public void setTurl(String turl) {
        Turl = turl;
    }
}

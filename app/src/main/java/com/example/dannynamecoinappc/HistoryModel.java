package com.example.namecoin;

public class HistoryModel {
    private String title;
    private String date;

    public HistoryModel(String title, String date) {
        this.title = title;
        this.date = date;

    }

    public HistoryModel() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

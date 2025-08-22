package com.example.final3o.Student.Attendance;

public class StudModel {
    private String paperName;
    private String date;
    private String status;

    // Constructor
    public StudModel(String paperName, String date, String status) {
        this.paperName = paperName;
        this.date = date;
        this.status = status;
    }

    // Getters
    public String getPaperName() {
        return paperName;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }
}

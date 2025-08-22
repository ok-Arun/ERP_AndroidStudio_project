package com.example.final3o.Student.Marks;

public class MarksModel {

    private String paper;
    private int marks;
    private String time;

    public MarksModel(String paper, int marks, String time) {
        this.paper = paper;
        this.marks = marks;
        this.time = time;
    }

    // Getters and setters (if needed)
    public String getPaper() {
        return paper;
    }

    public void setPaper(String paper) {
        this.paper = paper;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

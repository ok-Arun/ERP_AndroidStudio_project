package com.example.final3o.Admin.Department;
public class CourseModel {
    private String courseId;
    private String courseName;
    private String depid;
    private int semester;

    // No-argument constructor (required by Firebase)
    public CourseModel() {
        // Empty constructor needed for Firestore deserialization
    }

    // Constructor with arguments
    public CourseModel(String courseId, String courseName, String depid, int semester) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.depid = depid;
        this.semester = semester;
    }

    // Getters and setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDepid() {
        return depid;
    }

    public void setDepid(String depid) {
        this.depid = depid;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }
}

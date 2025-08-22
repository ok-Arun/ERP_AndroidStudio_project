package com.example.final3o.Teacher.Students;

public class ChangeRequest {
    private String fullName;
    private String address;
    private String courseName;
    private String dob;
    private String gender;
    private String parentName;
    private String phoneNumber;
    private String semester;
    private String status;
    private String studentId;

    // Constructor
    public ChangeRequest(String fullName, String address, String courseName, String dob, String gender,
                         String parentName, String phoneNumber, String semester, String status, String studentId) {
        this.fullName = fullName;
        this.address = address;
        this.courseName = courseName;
        this.dob = dob;
        this.gender = gender;
        this.parentName = parentName;
        this.phoneNumber = phoneNumber;
        this.semester = semester;
        this.status = status;
        this.studentId = studentId;
    }

    // Getters and setters for each field
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}

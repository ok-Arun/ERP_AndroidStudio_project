package com.example.final3o.Teacher.Attendance;

public class Student {
    private String uid;
    private String name;
    private String studentId;
    private String attendanceStatus;
    private String departmentName;
    private String studentName;

    // 🟢 NEW FIELD for checkbox control
    private boolean present; // Default false (absent)

    // Default constructor (required for Firebase)
    public Student() {
    }

    // Constructor with all arguments
    public Student(String uid, String name, String studentId, String attendanceStatus, String departmentName, String studentName) {
        this.uid = uid;
        this.name = name;
        this.studentId = studentId;
        this.attendanceStatus = attendanceStatus;
        this.departmentName = departmentName;
        this.studentName = studentName;
        this.present = false; // default
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    // 🔴 NEW Getter/Setter for checkbox
    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}

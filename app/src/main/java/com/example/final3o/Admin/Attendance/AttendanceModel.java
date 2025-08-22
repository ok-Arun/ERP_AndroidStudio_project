package com.example.final3o.Admin.Attendance;
public class AttendanceModel {
    private String name;
    private String studentId;
    private String attendanceStatus;

    public AttendanceModel() {
        // Default constructor required for Firestore
    }

    public AttendanceModel(String name, String studentId, String attendanceStatus) {
        this.name = name;
        this.studentId = studentId;
        this.attendanceStatus = attendanceStatus;
    }

    public String getName() {
        return name;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }
}

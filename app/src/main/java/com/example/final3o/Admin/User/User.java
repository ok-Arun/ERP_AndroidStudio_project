package com.example.final3o.Admin.User;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private String dob;
    private String parentsName;
    private String registrationYear;
    private String gender;
    private String course;
    private String studentId;
    private String teacherId;
    private String adminId;
    private String role;
    private String cid;
    private String Name;
    private String userStatus;  // NEW field to track user status (active/passout/retired)

    // No-argument constructor required by Firestore
    public User() {
    }

    // Constructor including the new 'status' field
    public User(String uid, String fullName, String Name, String email, String dob, String parentsName, String registrationYear,
                String gender, String course, String studentId, String teacherId, String adminId,
                String role, String cid, String userStatus) {
        this.uid = uid;
        this.fullName = fullName;
        this.Name = Name;
        this.email = email;
        this.dob = dob;
        this.parentsName = parentsName;
        this.registrationYear = registrationYear;
        this.gender = gender;
        this.course = course;
        this.studentId = studentId;
        this.teacherId = teacherId;
        this.adminId = adminId;
        this.role = role;
        this.cid = cid;
        this.userStatus = userStatus;
    }

    // Getters and setters
    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getParentsName() {
        return parentsName;
    }

    public void setParentsName(String parentsName) {
        this.parentsName = parentsName;
    }

    public String getRegistrationYear() {
        return registrationYear;
    }

    public void setRegistrationYear(String registrationYear) {
        this.registrationYear = registrationYear;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }




}

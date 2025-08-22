package com.example.final3o.Teacher;

public class Teacher {

    private String name;
    private String email;
    private String subject;

    public Teacher() {
        // Default constructor for Firestore
    }

    public Teacher(String name, String email, String subject) {
        this.name = name;
        this.email = email;
        this.subject = subject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}

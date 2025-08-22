package com.example.final3o.Admin.Department;

public class DepartmentModel {
    private String departmentName;  // Store the name of the department
    private String depid;           // Store the department ID (short code)
    private String assignedTeacher; // Store the assigned teacher's name

    // Constructor to initialize departmentName, depid, and assignedTeacher
    public DepartmentModel(String departmentName, String depid, String assignedTeacher) {
        this.departmentName = departmentName;
        this.depid = depid;
        this.assignedTeacher = assignedTeacher;
    }

    // Getter for department name
    public String getDepartmentName() {
        return departmentName;
    }

    // Setter for department name
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    // Getter for depid
    public String getDepid() {
        return depid;
    }

    // Setter for depid
    public void setDepid(String depid) {
        this.depid = depid;
    }

    // Getter for assignedTeacher
    public String getAssignedTeacher() {
        return assignedTeacher;
    }

    // Setter for assignedTeacher
    public void setAssignedTeacher(String assignedTeacher) {
        this.assignedTeacher = assignedTeacher;
    }

    // Optional: toString method for easier debugging
    @Override
    public String toString() {
        return "DepartmentModel{" +
                "departmentName='" + departmentName + '\'' +
                ", depid='" + depid + '\'' +
                ", assignedTeacher='" + assignedTeacher + '\'' +
                '}';
    }
}

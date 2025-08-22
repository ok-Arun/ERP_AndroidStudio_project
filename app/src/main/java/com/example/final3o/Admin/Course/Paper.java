package com.example.final3o.Admin.Course;
public class Paper {
    private String paperId;
    private String paperName;
    private String courseId, paperCode;
    private String courseName;
    private String deptId;
    private String departmentName;
    private String semester;
    private String paperType;
    private String teacherId;
    private String teacherName;

    public Paper() {
        // Required for Firebase
    }

    public Paper(String paperId, String paperName, String paperCode, String paperType,
                 String courseId, String courseName, String deptId, String departmentName, String semester) {
        this.paperId = paperId;
        this.paperName = paperName;
        this.paperCode = paperCode;
        this.paperType = paperType;
        this.courseId = courseId;
        this.courseName = courseName;
        this.deptId = deptId;
        this.departmentName = departmentName;
        this.semester = semester;
    }

    public Paper(String paperId,String paperCode, String paperName, String courseId, String courseName,
                 String departmentId, String departmentName, String semester, String paperType,
                 String teacherId, String teacherName) {
        this.paperId = paperId;
        this.paperCode = paperCode;
        this.paperName = paperName;
        this.courseId = courseId;
        this.courseName = courseName;
        this.deptId = departmentId;
        this.departmentName = departmentName;
        this.semester = semester;
        this.paperType = paperType;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
    }

    // All getters and setters
    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public String getPaperCode() {
        return paperCode;
    }

    public void setPaperCode(String paperCode) {
        this.paperCode = paperCode;
    }

    public String getPaperType() {
        return paperType;
    }

    public void setPaperType(String paperType) {
        this.paperType = paperType;
    }

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

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    @Override
    public String toString() {
        return "Paper{" +
                "paperId='" + paperId + '\'' +
                ", paperName='" + paperName + '\'' +
                ", paperCode='" + paperCode + '\'' +
                ", courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", deptId='" + deptId + '\'' +
                ", departmentName='" + departmentName + '\'' +
                ", semester='" + semester + '\'' +
                ", paperType='" + paperType + '\'' +
                '}';
    }
}

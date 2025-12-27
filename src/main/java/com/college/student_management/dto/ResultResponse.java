package com.college.student_management.dto;

import java.util.List;

public class ResultResponse {
    private String rollNo;
    private String name;
    private int semester;
    private double gpa;
    private double cgpa;
    private List<SubjectResult> subjects;

    public static class SubjectResult {
        private String subjectName;
        private int marks;
        private String grade;
        private int credits;
        private int gradePoint;

        public SubjectResult(String subjectName, int marks, String grade, int credits, int gradePoint) {
            this.subjectName = subjectName;
            this.marks = marks;
            this.grade = grade;
            this.credits = credits;
            this.gradePoint = gradePoint;
        }

        // Getters and Setters
        public String getSubjectName() {
            return subjectName;
        }

        public int getMarks() {
            return marks;
        }

        public String getGrade() {
            return grade;
        }

        public int getCredits() {
            return credits;
        }

        public int getGradePoint() {
            return gradePoint;
        }
    }

    public ResultResponse() {
    }

    // Getters and Setters
    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public List<SubjectResult> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectResult> subjects) {
        this.subjects = subjects;
    }
}

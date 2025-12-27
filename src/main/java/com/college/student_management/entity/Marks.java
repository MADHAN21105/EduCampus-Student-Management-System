package com.college.student_management.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "marks")
public class Marks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_roll_no", referencedColumnName = "rollNo")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student student;

    private int semester;
    private String subject;
    private int marks;
    private String grade;
    private int gradePoint;

    public Marks() {
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getSubject() {
        return subject;
    }

    public int getMarks() {
        return marks;
    }

    public String getGrade() {
        return grade;
    }

    public int getGradePoint() {
        return gradePoint;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setGradePoint(int gradePoint) {
        this.gradePoint = gradePoint;
    }

    public void setMarks(int marks) {
        this.marks = marks;
        this.grade = calculateGrade(marks);
        this.gradePoint = calculateGradePoint(marks);
    }

    private String calculateGrade(int m) {
        if (m >= 90)
            return "A+";
        if (m >= 80)
            return "A";
        if (m >= 70)
            return "B";
        if (m >= 60)
            return "C";
        if (m >= 50)
            return "D";
        if (m >= 40)
            return "E";
        return "F";
    }

    private int calculateGradePoint(int m) {
        if (m >= 90)
            return 10;
        if (m >= 80)
            return 9;
        if (m >= 70)
            return 8;
        if (m >= 60)
            return 7;
        if (m >= 50)
            return 6;
        if (m >= 40)
            return 5;
        return 0;
    }
}

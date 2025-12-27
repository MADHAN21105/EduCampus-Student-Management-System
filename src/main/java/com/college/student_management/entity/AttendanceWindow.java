package com.college.student_management.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance_windows")
public class AttendanceWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private Integer semester;

    @Column(nullable = false)
    private LocalDate reopenDate;

    @Column(nullable = false)
    private LocalDate lastWorkingDay;

    @Column(nullable = false)
    private Integer periodsPerDay;

    public AttendanceWindow() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public LocalDate getReopenDate() {
        return reopenDate;
    }

    public void setReopenDate(LocalDate reopenDate) {
        this.reopenDate = reopenDate;
    }

    public LocalDate getLastWorkingDay() {
        return lastWorkingDay;
    }

    public void setLastWorkingDay(LocalDate lastWorkingDay) {
        this.lastWorkingDay = lastWorkingDay;
    }

    public Integer getPeriodsPerDay() {
        return periodsPerDay;
    }

    public void setPeriodsPerDay(Integer periodsPerDay) {
        this.periodsPerDay = periodsPerDay;
    }
}

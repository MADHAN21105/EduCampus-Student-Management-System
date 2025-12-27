package com.college.student_management.dto;

import java.util.Map;

public class AttendanceSummaryDTO {
    private Integer semester;
    private long presentHours;
    private long absentHours;
    private long onDutyHours;
    private long medicalLeaveHours;
    private long workedHours;
    private String attendancePercentage;
    private String enrollmentDate; // Open Date
    private String endDate; // Close Date
    private boolean eligible;
    private Integer periodsPerDay;
    private java.util.List<Map<String, Object>> dailyAttendance; // Detailed day-wise table data

    // Getters and Setters
    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public long getPresentHours() {
        return presentHours;
    }

    public void setPresentHours(long presentHours) {
        this.presentHours = presentHours;
    }

    public long getAbsentHours() {
        return absentHours;
    }

    public void setAbsentHours(long absentHours) {
        this.absentHours = absentHours;
    }

    public long getOnDutyHours() {
        return onDutyHours;
    }

    public void setOnDutyHours(long onDutyHours) {
        this.onDutyHours = onDutyHours;
    }

    public long getMedicalLeaveHours() {
        return medicalLeaveHours;
    }

    public void setMedicalLeaveHours(long medicalLeaveHours) {
        this.medicalLeaveHours = medicalLeaveHours;
    }

    public long getWorkedHours() {
        return workedHours;
    }

    public void setWorkedHours(long workedHours) {
        this.workedHours = workedHours;
    }

    public String getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(String attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public java.util.List<Map<String, Object>> getDailyAttendance() {
        return dailyAttendance;
    }

    public void setDailyAttendance(java.util.List<Map<String, Object>> dailyAttendance) {
        this.dailyAttendance = dailyAttendance;
    }

    public Integer getPeriodsPerDay() {
        return periodsPerDay;
    }

    public void setPeriodsPerDay(Integer periodsPerDay) {
        this.periodsPerDay = periodsPerDay;
    }
}

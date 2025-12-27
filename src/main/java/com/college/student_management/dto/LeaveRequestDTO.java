package com.college.student_management.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class LeaveRequestDTO {

    private Long id;
    private String studentName;
    private String studentRollNo;
    private String requestType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
    private String status;
    private LocalDateTime appliedOn;
    private String teacherRemarks;
    private LocalDateTime reviewedOn;
    private String teacherName;
    private long numberOfDays;

    // Constructors
    public LeaveRequestDTO() {
    }

    public LeaveRequestDTO(Long id, String studentName, String studentRollNo, String requestType,
            LocalDate fromDate, LocalDate toDate, String reason, String status,
            LocalDateTime appliedOn, String teacherRemarks, LocalDateTime reviewedOn, String teacherName) {
        this.id = id;
        this.studentName = studentName;
        this.studentRollNo = studentRollNo;
        this.requestType = requestType;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.reason = reason;
        this.status = status;
        this.appliedOn = appliedOn;
        this.teacherRemarks = teacherRemarks;
        this.reviewedOn = reviewedOn;
        this.teacherName = teacherName;
        this.numberOfDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
        if (toDate != null) {
            this.numberOfDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        }
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
        if (fromDate != null) {
            this.numberOfDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        }
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAppliedOn() {
        return appliedOn;
    }

    public void setAppliedOn(LocalDateTime appliedOn) {
        this.appliedOn = appliedOn;
    }

    public String getTeacherRemarks() {
        return teacherRemarks;
    }

    public void setTeacherRemarks(String teacherRemarks) {
        this.teacherRemarks = teacherRemarks;
    }

    public LocalDateTime getReviewedOn() {
        return reviewedOn;
    }

    public void setReviewedOn(LocalDateTime reviewedOn) {
        this.reviewedOn = reviewedOn;
    }

    public long getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(long numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}

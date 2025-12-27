package com.college.student_management.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @Column(nullable = false, unique = true)
    private String rollNo;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String studentClass;
    private String year;
    private String department;
    private Integer semester;

    // New Profile Fields
    private String gender;
    private String dob;
    private String fatherName;
    private String motherName;
    private String bloodGroup;
    @Column(length = 500)
    private String address;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // 🔹 Default constructor (required by JPA)
    public Student() {
    }

    // 🔹 Parameterized constructor
    public Student(String name, String rollNo, String email, String phone,
            String studentClass, String year, String department, Integer semester) {
        this.name = name;
        this.rollNo = rollNo;
        this.email = email;
        this.phone = phone;
        this.studentClass = studentClass;
        this.year = year;
        this.department = department;
        this.semester = semester;
    }

    // 🔹 Getters and Setters
    public String getRollNo() {
        return rollNo;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getStudentClass() {
        return studentClass;
    }

    public String getYear() {
        return year;
    }

    public String getDepartment() {
        return department;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

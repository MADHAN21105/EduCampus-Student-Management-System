package com.college.student_management.repository;

import com.college.student_management.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudentRollNo(String rollNo);

    List<Attendance> findByStudentRollNoAndSemester(String rollNo, Integer semester);

    List<Attendance> findByDate(LocalDate date);

    java.util.Optional<Attendance> findByStudentRollNoAndDateAndPeriod(String rollNo, LocalDate date, Integer period);
}

package com.college.student_management.repository;

import com.college.student_management.entity.AttendanceWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AttendanceWindowRepository extends JpaRepository<AttendanceWindow, Long> {
    Optional<AttendanceWindow> findByDepartmentAndSemester(String department, Integer semester);
}

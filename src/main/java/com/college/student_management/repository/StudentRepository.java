package com.college.student_management.repository;

import com.college.student_management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    java.util.List<Student> findByDepartment(String department);

    java.util.List<Student> findByDepartmentAndSemester(String department, Integer semester);

    java.util.Optional<Student> findByUserUsername(String username);

    java.util.Optional<Student> findByRollNo(String rollNo);
}

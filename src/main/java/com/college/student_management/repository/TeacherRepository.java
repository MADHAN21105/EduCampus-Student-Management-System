package com.college.student_management.repository;

import com.college.student_management.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, String> {
    java.util.Optional<Teacher> findByUserUsername(String username);

    java.util.List<Teacher> findByDepartment(String department);
}

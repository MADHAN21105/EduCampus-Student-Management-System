package com.college.student_management.repository;

import com.college.student_management.entity.Marks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarksRepository extends JpaRepository<Marks, Long> {
    List<Marks> findByStudentRollNo(String rollNo);

    List<Marks> findByStudentRollNoAndSemester(String rollNo, int semester);

    java.util.Optional<Marks> findByStudentRollNoAndSubjectAndSemester(String rollNo, String subject, int semester);
}

package com.college.student_management.repository;

import com.college.student_management.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findBySemester(int semester);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Subject s WHERE s.teacher.facultyId = :facultyId")
    long countByTeacherFacultyId(@org.springframework.data.repository.query.Param("facultyId") String facultyId);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Subject s WHERE s.teacher.facultyId = :facultyId")
    List<Subject> findByTeacherFacultyId(
            @org.springframework.data.repository.query.Param("facultyId") String facultyId);

    java.util.Optional<Subject> findByName(String name);
}

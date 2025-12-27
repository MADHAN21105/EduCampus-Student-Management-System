package com.college.student_management.controller;

import com.college.student_management.entity.Subject;
import com.college.student_management.repository.SubjectRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectRepository repo;

    public SubjectController(SubjectRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Subject> getAllSubjects() {
        return repo.findAll();
    }

    @GetMapping("/semester/{semester}")
    public List<Subject> getBySemester(@PathVariable int semester) {
        return repo.findBySemester(semester);
    }

    @GetMapping("/teacher/{facultyId}")
    public List<Subject> getByTeacher(@PathVariable String facultyId) {
        return repo.findByTeacherFacultyId(facultyId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Subject addSubject(@RequestBody Subject subject) {
        return repo.save(subject);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Subject updateSubject(@PathVariable Long id, @RequestBody Subject subjectDetails) {
        Subject subject = repo.findById(id).orElseThrow();
        subject.setName(subjectDetails.getName());
        subject.setCode(subjectDetails.getCode());
        subject.setSemester(subjectDetails.getSemester());
        subject.setTeacher(subjectDetails.getTeacher());
        return repo.save(subject);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSubject(@PathVariable Long id) {
        repo.deleteById(id);
    }
}

package com.college.student_management.controller;

import com.college.student_management.entity.Marks;
import com.college.student_management.repository.MarksRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marks")
public class MarksController {

    private final MarksRepository repo;

    public MarksController(MarksRepository repo) {
        this.repo = repo;
    }

    // ✅ Teacher enters marks (Upsert logic)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Marks addMarks(@RequestBody Marks marks) {
        String rollNo = marks.getStudent().getRollNo();
        String subject = marks.getSubject();
        int semester = marks.getSemester();

        return repo.findByStudentRollNoAndSubjectAndSemester(rollNo, subject, semester)
                .map(existing -> {
                    existing.setMarks(marks.getMarks());
                    return repo.save(existing);
                })
                .orElseGet(() -> repo.save(marks));
    }

    // ✅ View marks by student
    @GetMapping("/student/{rollNo}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public List<Marks> getMarks(@PathVariable String rollNo) {
        return repo.findByStudentRollNo(rollNo);
    }

    // ✅ View marks by student and semester
    @GetMapping("/student/{rollNo}/semester/{semester}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public List<Marks> getMarksBySemester(@PathVariable String rollNo, @PathVariable int semester) {
        return repo.findByStudentRollNoAndSemester(rollNo, semester);
    }
}

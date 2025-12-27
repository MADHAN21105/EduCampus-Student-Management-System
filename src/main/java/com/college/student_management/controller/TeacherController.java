package com.college.student_management.controller;

import com.college.student_management.entity.*;
import com.college.student_management.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherRepository repo;
    private final UserRepository userRepo;
    private final SubjectRepository subjectRepo;

    public TeacherController(TeacherRepository repo, UserRepository userRepo, SubjectRepository subjectRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.subjectRepo = subjectRepo;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Teacher> getAllTeachers() {
        return repo.findAll();
    }

    @GetMapping("/department/{dept}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_TEACHER') or hasAuthority('ROLE_STUDENT')")
    public List<Teacher> getTeachersByDepartment(@PathVariable String dept) {
        return repo.findByDepartment(dept);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public Teacher getMyDetails() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return repo.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found for user: " + username));
    }

    @GetMapping("/{facultyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public Teacher getTeacher(@PathVariable String facultyId) {
        return repo.findById(facultyId).orElseThrow(() -> new RuntimeException("Teacher not found: " + facultyId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Teacher addTeacher(@RequestBody Teacher teacher) {
        String fId = teacher.getFacultyId();
        if (fId == null) {
            throw new IllegalArgumentException("Faculty ID cannot be null");
        }
        final String facultyId = fId;
        // ✅ Preserve user link if updating an existing teacher record
        repo.findById(facultyId).ifPresent(existing -> {
            if (teacher.getUser() == null) {
                teacher.setUser(existing.getUser());
            }
        });
        return repo.save(teacher);
    }

    @PutMapping("/{facultyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Teacher updateTeacher(@PathVariable String facultyId, @RequestBody Teacher teacherDetails) {
        Teacher teacher = repo.findById(facultyId).orElseThrow();
        teacher.setName(teacherDetails.getName());
        teacher.setEmail(teacherDetails.getEmail());
        teacher.setPhone(teacherDetails.getPhone());
        teacher.setSpecialization(teacherDetails.getSpecialization());
        teacher.setDepartment(teacherDetails.getDepartment());
        teacher.setGender(teacherDetails.getGender());
        teacher.setDob(teacherDetails.getDob());
        teacher.setFatherName(teacherDetails.getFatherName());
        teacher.setMotherName(teacherDetails.getMotherName());
        teacher.setBloodGroup(teacherDetails.getBloodGroup());
        teacher.setAddress(teacherDetails.getAddress());
        return repo.save(teacher);
    }

    // ✅ Teachers can update their own profile (restricted fields)
    @PutMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public Teacher updateOwnProfile(@RequestBody Teacher teacherDetails) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Teacher teacher = repo.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found for user: " + username));

        teacher.setEmail(teacherDetails.getEmail());
        teacher.setPhone(teacherDetails.getPhone());
        teacher.setSpecialization(teacherDetails.getSpecialization());
        teacher.setGender(teacherDetails.getGender());
        teacher.setDob(teacherDetails.getDob());
        teacher.setFatherName(teacherDetails.getFatherName());
        teacher.setMotherName(teacherDetails.getMotherName());
        teacher.setBloodGroup(teacherDetails.getBloodGroup());
        teacher.setAddress(teacherDetails.getAddress());

        return repo.save(teacher);
    }

    @DeleteMapping("/{facultyId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteTeacher(@PathVariable String facultyId) {
        repo.findById(facultyId).ifPresent(teacher -> {
            // 1. Delete associated data (subjects)
            subjectRepo.deleteAll(subjectRepo.findByTeacherFacultyId(facultyId));

            // 2. Delete associated login
            User u = teacher.getUser();
            if (u != null) {
                userRepo.delete(u);
            }

            // 3. Delete teacher profile
            repo.delete(teacher);
        });
    }

    // 📊 Teacher dashboard summary
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public String dashboard() {
        return "Teacher Dashboard Overview";
    }
}

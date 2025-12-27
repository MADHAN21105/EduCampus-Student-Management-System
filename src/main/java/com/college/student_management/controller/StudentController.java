package com.college.student_management.controller;

import com.college.student_management.entity.*;
import com.college.student_management.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository repo;
    private final UserRepository userRepo;
    private final AttendanceRepository attendanceRepo;
    private final MarksRepository marksRepo;
    private final LeaveRequestRepository leaveRepo;

    public StudentController(StudentRepository repo, UserRepository userRepo,
            AttendanceRepository attendanceRepo, MarksRepository marksRepo,
            LeaveRequestRepository leaveRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.attendanceRepo = attendanceRepo;
        this.marksRepo = marksRepo;
        this.leaveRepo = leaveRepo;
    }

    // ✅ ADMIN + TEACHER can view students
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<Student> getAllStudents() {
        return repo.findAll();
    }

    // ✅ Get individual student details
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public Student getStudentById(@PathVariable String id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Student not found: " + id));
    }

    // ✅ ONLY ADMIN can add students
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Student addStudent(@RequestBody Student student) {
        String rNo = student.getRollNo();
        if (rNo == null) {
            throw new IllegalArgumentException("Roll number cannot be null");
        }
        final String rollNo = rNo;
        // ✅ Preserve user link if updating an existing student record
        repo.findById(rollNo).ifPresent(existing -> {
            if (student.getUser() == null) {
                student.setUser(existing.getUser());
            }
        });
        return repo.save(student);
    }

    // ✅ ONLY ADMIN can delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteStudent(@PathVariable String id) {
        repo.findById(id).ifPresent(student -> {
            // 1. Delete associated data first (to avoid FK errors)
            attendanceRepo.deleteAll(attendanceRepo.findByStudentRollNo(id));
            marksRepo.deleteAll(marksRepo.findByStudentRollNo(id));
            leaveRepo.deleteAll(leaveRepo.findByStudentRollNo(id));

            // 2. Delete associated login
            User u = student.getUser(); // Changed from 'teacher.getUser()' to 'student.getUser()'
            if (u != null) {
                userRepo.delete(u);
            }

            // 3. Delete student profile
            repo.delete(student);
        });
    }

    // ✅ Update student details
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Student updateStudent(@PathVariable String id, @RequestBody Student studentDetails) {
        Student student = repo.findById(id).orElseThrow();
        student.setName(studentDetails.getName());
        student.setEmail(studentDetails.getEmail());
        student.setRollNo(studentDetails.getRollNo());
        student.setStudentClass(studentDetails.getStudentClass());
        student.setYear(studentDetails.getYear());
        student.setPhone(studentDetails.getPhone());
        student.setDepartment(studentDetails.getDepartment());
        student.setSemester(studentDetails.getSemester());

        // Detailed Profile Fields
        student.setGender(studentDetails.getGender());
        student.setDob(studentDetails.getDob());
        student.setFatherName(studentDetails.getFatherName());
        student.setMotherName(studentDetails.getMotherName());
        student.setBloodGroup(studentDetails.getBloodGroup());
        student.setAddress(studentDetails.getAddress());

        return repo.save(student);
    }

    // ✅ Students can update their own profile (restricted fields)
    @PutMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public Student updateOwnProfile(@RequestBody Student studentDetails) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = repo.findById(username).orElseThrow();

        student.setEmail(studentDetails.getEmail());
        student.setPhone(studentDetails.getPhone());
        student.setGender(studentDetails.getGender());
        student.setDob(studentDetails.getDob());
        student.setFatherName(studentDetails.getFatherName());
        student.setMotherName(studentDetails.getMotherName());
        student.setBloodGroup(studentDetails.getBloodGroup());
        student.setAddress(studentDetails.getAddress());

        return repo.save(student);
    }
}

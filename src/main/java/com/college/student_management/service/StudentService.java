package com.college.student_management.service;

import com.college.student_management.entity.Student;
import com.college.student_management.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    // Constructor injection (BEST PRACTICE)
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Add / Save student
    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    // Get all students
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // Get student by ID
    public Student getStudentById(String id) {
        return studentRepository.findById(id).orElse(null);
    }

    // Delete student
    public void deleteStudent(String id) {
        studentRepository.deleteById(id);
    }
}

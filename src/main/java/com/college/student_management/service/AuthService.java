package com.college.student_management.service;

import com.college.student_management.entity.User;
import com.college.student_management.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final com.college.student_management.repository.TeacherRepository teacherRepository;
    private final com.college.student_management.repository.StudentRepository studentRepository;

    // 🔥 FORCE BCrypt (bypass Spring injection)
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository,
            com.college.student_management.repository.TeacherRepository teacherRepository,
            com.college.student_management.repository.StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    public User authenticate(String username, String password, String expectedRole) {

        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        boolean match = encoder.matches(password.trim(), user.getPassword());

        if (!match) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // Enforce role check if role is provided
        if (expectedRole != null && !user.getRole().name().equals(expectedRole)) {
            String roleName = expectedRole.equals("TEACHER") ? "Staff"
                    : expectedRole.substring(0, 1) + expectedRole.substring(1).toLowerCase();
            throw new IllegalArgumentException("Access denied. This portal is only for " + roleName + " accounts.");
        }

        return user;
    }

    @Transactional
    public User register(String username, String password, com.college.student_management.entity.Role role) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Username and password cannot be null");
        }
        String trimmedUsername = username.trim();
        if (userRepository.findByUsername(trimmedUsername).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // ✅ Check if Student/Teacher profile already exists to avoid unique constraint
        // issues
        if (role == com.college.student_management.entity.Role.TEACHER) {
            if (trimmedUsername == null) {
                throw new IllegalArgumentException("Faculty ID cannot be null");
            }
            if (teacherRepository.findById(trimmedUsername).isPresent()) {
                throw new IllegalArgumentException("Faculty ID already exists: " + trimmedUsername);
            }
        } else if (role == com.college.student_management.entity.Role.STUDENT) {
            if (trimmedUsername == null) {
                throw new IllegalArgumentException("Roll No cannot be null");
            }
            if (studentRepository.findById(trimmedUsername).isPresent()) {
                throw new IllegalArgumentException("Roll No already exists: " + trimmedUsername);
            }
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(encoder.encode(password.trim()));
        user.setRole(role);
        user.setTemporaryPassword(true); // Set as temporary by default

        User savedUser = userRepository.save(user);

        // ✅ Automatically create Student/Teacher profile
        if (role == com.college.student_management.entity.Role.TEACHER) {
            com.college.student_management.entity.Teacher teacher = new com.college.student_management.entity.Teacher();
            teacher.setFacultyId(username.trim()); // Use username as ID
            teacher.setName(username.trim()); // Default name
            teacher.setEmail(username.trim() + "@school.com"); // Default email
            teacher.setUser(savedUser);
            teacherRepository.save(teacher);
        } else if (role == com.college.student_management.entity.Role.STUDENT) {
            com.college.student_management.entity.Student student = new com.college.student_management.entity.Student();
            student.setRollNo(username.trim()); // Use username as ID
            student.setName(username.trim()); // Default name
            student.setEmail(username.trim() + "@school.com"); // Default email
            student.setUser(savedUser);
            studentRepository.save(student);
        }

        return savedUser;
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!encoder.matches(oldPassword.trim(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(encoder.encode(newPassword.trim()));
        user.setTemporaryPassword(false); // Clear temporary flag
        userRepository.save(user);
    }
}

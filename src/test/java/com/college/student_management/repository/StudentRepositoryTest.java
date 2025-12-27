package com.college.student_management.repository;

import com.college.student_management.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("StudentRepository Tests")
class StudentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudentRepository studentRepository;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setRollNo("CS2021001");
        testStudent.setName("John Doe");
        testStudent.setEmail("john@example.com");
        testStudent.setDepartment("Computer Science");
        testStudent.setSemester(5);
        testStudent.setPhone("1234567890");
        testStudent.setGender("Male");
        testStudent.setBloodGroup("O+");
    }

    @Test
    @DisplayName("Should save and find student by roll number")
    void testFindByRollNo() {
        // Arrange
        entityManager.persist(testStudent);
        entityManager.flush();

        // Act
        Optional<Student> found = studentRepository.findByRollNo("CS2021001");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("Should return empty when student not found by roll number")
    void testFindByRollNo_NotFound() {
        // Act
        Optional<Student> found = studentRepository.findByRollNo("INVALID");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find students by department")
    void testFindByDepartment() {
        // Arrange
        Student student2 = new Student();
        student2.setRollNo("CS2021002");
        student2.setName("Jane Smith");
        student2.setEmail("jane@example.com");
        student2.setDepartment("Computer Science");
        student2.setSemester(5);

        entityManager.persist(testStudent);
        entityManager.persist(student2);
        entityManager.flush();

        // Act
        List<Student> csStudents = studentRepository.findByDepartment("Computer Science");

        // Assert
        assertThat(csStudents).hasSize(2);
        assertThat(csStudents).extracting(Student::getDepartment)
                .containsOnly("Computer Science");
    }

    @Test
    @DisplayName("Should find students by semester")
    void testFindBySemester() {
        // Arrange
        entityManager.persist(testStudent);
        entityManager.flush();

        // Act
        List<Student> semester5Students = studentRepository.findBySemester(5);

        // Assert
        assertThat(semester5Students).hasSize(1);
        assertThat(semester5Students.get(0).getSemester()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should count students correctly")
    void testCount() {
        // Arrange
        entityManager.persist(testStudent);
        entityManager.flush();

        // Act
        long count = studentRepository.count();

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should delete student by ID")
    void testDeleteById() {
        // Arrange
        Student saved = entityManager.persist(testStudent);
        entityManager.flush();
        Long studentId = saved.getId();

        // Act
        studentRepository.deleteById(studentId);
        entityManager.flush();

        // Assert
        Optional<Student> deleted = studentRepository.findById(studentId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should update student information")
    void testUpdateStudent() {
        // Arrange
        Student saved = entityManager.persist(testStudent);
        entityManager.flush();

        // Act
        saved.setEmail("newemail@example.com");
        saved.setPhone("9876543210");
        Student updated = studentRepository.save(saved);
        entityManager.flush();

        // Assert
        assertThat(updated.getEmail()).isEqualTo("newemail@example.com");
        assertThat(updated.getPhone()).isEqualTo("9876543210");
    }
}

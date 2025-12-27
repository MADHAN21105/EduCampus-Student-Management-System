package com.college.student_management.service;

import com.college.student_management.entity.Student;
import com.college.student_management.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Tests")
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setRollNo("CS2021001");
        testStudent.setName("John Doe");
        testStudent.setEmail("john@example.com");
        testStudent.setDepartment("Computer Science");
        testStudent.setSemester(5);
        testStudent.setPhone("1234567890");
    }

    @Test
    @DisplayName("Should return all students")
    void testGetAllStudents() {
        // Arrange
        List<Student> students = Arrays.asList(testStudent, new Student());
        when(studentRepository.findAll()).thenReturn(students);

        // Act
        List<Student> result = studentService.getAllStudents();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).contains(testStudent);
        verify(studentRepository).findAll();
    }

    @Test
    @DisplayName("Should find student by roll number")
    void testFindByRollNo_Success() {
        // Arrange
        when(studentRepository.findByRollNo(anyString())).thenReturn(Optional.of(testStudent));

        // Act
        Optional<Student> result = studentService.findByRollNo("CS2021001");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getRollNo()).isEqualTo("CS2021001");
        assertThat(result.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return empty when student not found by roll number")
    void testFindByRollNo_NotFound() {
        // Arrange
        when(studentRepository.findByRollNo(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<Student> result = studentService.findByRollNo("INVALID");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should save new student")
    void testSaveStudent() {
        // Arrange
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        // Act
        Student result = studentService.saveStudent(testStudent);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRollNo()).isEqualTo("CS2021001");
        verify(studentRepository).save(testStudent);
    }

    @Test
    @DisplayName("Should delete student by ID")
    void testDeleteStudent() {
        // Arrange
        Long studentId = 1L;
        doNothing().when(studentRepository).deleteById(studentId);

        // Act
        studentService.deleteStudent(studentId);

        // Assert
        verify(studentRepository).deleteById(studentId);
    }

    @Test
    @DisplayName("Should count total students")
    void testCountStudents() {
        // Arrange
        when(studentRepository.count()).thenReturn(150L);

        // Act
        long count = studentService.countStudents();

        // Assert
        assertThat(count).isEqualTo(150L);
        verify(studentRepository).count();
    }

    @Test
    @DisplayName("Should find students by department")
    void testFindByDepartment() {
        // Arrange
        List<Student> csStudents = Arrays.asList(testStudent);
        when(studentRepository.findByDepartment("Computer Science")).thenReturn(csStudents);

        // Act
        List<Student> result = studentService.findByDepartment("Computer Science");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepartment()).isEqualTo("Computer Science");
    }

    @Test
    @DisplayName("Should find students by semester")
    void testFindBySemester() {
        // Arrange
        List<Student> semester5Students = Arrays.asList(testStudent);
        when(studentRepository.findBySemester(5)).thenReturn(semester5Students);

        // Act
        List<Student> result = studentService.findBySemester(5);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemester()).isEqualTo(5);
    }
}

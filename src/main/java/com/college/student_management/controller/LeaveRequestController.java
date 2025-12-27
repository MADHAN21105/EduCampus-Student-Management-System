package com.college.student_management.controller;

import com.college.student_management.dto.LeaveRequestDTO;
import com.college.student_management.entity.Attendance;
import com.college.student_management.entity.LeaveRequest;
import com.college.student_management.entity.Student;
import com.college.student_management.entity.Teacher;
import com.college.student_management.repository.AttendanceRepository;
import com.college.student_management.repository.LeaveRequestRepository;
import com.college.student_management.repository.StudentRepository;
import com.college.student_management.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    // Student applies for leave
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LeaveRequest> applyForLeave(@RequestBody LeaveRequest leaveRequest) {
        if (leaveRequest.getStudent() == null || leaveRequest.getStudent().getRollNo() == null) {
            throw new RuntimeException("Student roll number is required");
        }

        // Fetch student entity
        String rollNo = leaveRequest.getStudent().getRollNo();
        Student student = studentRepository.findByRollNo(rollNo)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // ✅ Link to specific teacher if provided
        if (leaveRequest.getTeacher() != null && leaveRequest.getTeacher().getFacultyId() != null) {
            String facultyId = leaveRequest.getTeacher().getFacultyId();
            Teacher teacher = teacherRepository.findById(facultyId)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            leaveRequest.setTeacher(teacher);
        }

        leaveRequest.setStudent(student);
        leaveRequest.setStatus("PENDING");
        leaveRequest.setAppliedOn(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return ResponseEntity.ok(saved);
    }

    // Get all leave requests for a student
    @GetMapping("/student/{rollNo}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<List<LeaveRequestDTO>> getStudentLeaveRequests(@PathVariable String rollNo) {
        List<LeaveRequest> requests = leaveRequestRepository.findByStudentRollNo(rollNo);
        List<LeaveRequestDTO> dtos = requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Get all pending leave requests (for teachers)
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<LeaveRequestDTO>> getPendingLeaveRequests() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();

        List<LeaveRequest> requests;
        // ✅ If it's a teacher, only show requests directed to them
        if (org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"))) {
            Teacher teacher = teacherRepository.findByUserUsername(username)
                    .orElseThrow(() -> new RuntimeException("Teacher profile not found"));
            requests = leaveRequestRepository
                    .findByTeacherFacultyIdAndStatusOrderByAppliedOnDesc(teacher.getFacultyId(), "PENDING");
        } else {
            // Admin sees all pending
            requests = leaveRequestRepository.findByStatusOrderByAppliedOnDesc("PENDING");
        }

        List<LeaveRequestDTO> dtos = requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Get all leave requests (for admin/teacher)
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<LeaveRequestDTO>> getAllLeaveRequests() {
        List<LeaveRequest> requests = leaveRequestRepository.findAllByOrderByAppliedOnDesc();
        List<LeaveRequestDTO> dtos = requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Approve leave request
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<LeaveRequest> approveLeaveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        if (id == null) {
            throw new RuntimeException("ID cannot be null");
        }

        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        request.setStatus("APPROVED");
        request.setReviewedOn(LocalDateTime.now());

        if (body != null && body.containsKey("remarks")) {
            request.setTeacherRemarks(body.get("remarks"));
        }

        LeaveRequest updated = leaveRequestRepository.save(request);

        // Update attendance automatically
        updateAttendanceForLeaveRequest(request);

        return ResponseEntity.ok(updated);
    }

    // Reject leave request
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<LeaveRequest> rejectLeaveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        if (id == null) {
            throw new RuntimeException("ID cannot be null");
        }

        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        request.setStatus("REJECTED");
        request.setReviewedOn(LocalDateTime.now());

        if (body != null && body.containsKey("remarks")) {
            request.setTeacherRemarks(body.get("remarks"));
        }

        LeaveRequest updated = leaveRequestRepository.save(request);

        // Mark as absent for rejected requests
        markAsAbsentForDateRange(request);

        return ResponseEntity.ok(updated);
    }

    // Helper method to update attendance based on approved leave
    @org.springframework.transaction.annotation.Transactional
    private void updateAttendanceForLeaveRequest(LeaveRequest request) {
        String rollNo = request.getStudent().getRollNo();
        LocalDate currentDate = request.getFromDate();
        LocalDate endDate = request.getToDate();

        System.out.println("Updating attendance for leave request - Student: " + rollNo +
                ", From: " + currentDate + ", To: " + endDate +
                ", Type: " + request.getRequestType());

        // Determine status based on request type
        // Note: Map LEAVE to OD if approved so it doesn't count against percentage
        String attendanceStatus;
        boolean isPresent;

        switch (request.getRequestType().toUpperCase()) {
            case "OD":
                attendanceStatus = "OD";
                isPresent = true;
                break;
            case "ML":
                attendanceStatus = "ML";
                isPresent = true;
                break;
            case "LEAVE":
                attendanceStatus = "OD"; // Map approved leave to OD to count as present/excused
                isPresent = true;
                break;
            default:
                attendanceStatus = "A";
                isPresent = false;
        }

        Integer semester = request.getStudent().getSemester();
        // Fallback to year if semester is null
        if (semester == null && request.getStudent().getYear() != null) {
            try {
                semester = Integer.parseInt(request.getStudent().getYear());
            } catch (Exception e) {
                semester = 1;
            }
        }

        int recordsUpdated = 0;
        // Update attendance for each day in the range
        while (!currentDate.isAfter(endDate)) {
            // Skip Sundays for attendance updates
            if (currentDate.getDayOfWeek().getValue() == 7) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            // Update all 8 periods for each day
            for (int period = 1; period <= 8; period++) {
                Attendance attendance = attendanceRepository
                        .findByStudentRollNoAndDateAndPeriod(rollNo, currentDate, period)
                        .orElse(new Attendance());

                attendance.setStudent(request.getStudent());
                attendance.setDate(currentDate);
                attendance.setPeriod(period);
                attendance.setStatus(attendanceStatus);
                attendance.setPresent(isPresent);
                attendance.setSemester(semester != null ? semester : 1);

                attendanceRepository.save(attendance);
                recordsUpdated++;
            }

            currentDate = currentDate.plusDays(1);
        }

        System.out.println("Attendance update completed. Total records updated: " + recordsUpdated);
    }

    // Helper method to mark as absent for rejected requests
    @org.springframework.transaction.annotation.Transactional
    private void markAsAbsentForDateRange(LeaveRequest request) {
        String rollNo = request.getStudent().getRollNo();
        LocalDate currentDate = request.getFromDate();
        LocalDate endDate = request.getToDate();

        Integer semester = request.getStudent().getSemester();
        if (semester == null && request.getStudent().getYear() != null) {
            try {
                semester = Integer.parseInt(request.getStudent().getYear());
            } catch (Exception e) {
                semester = 1;
            }
        }

        while (!currentDate.isAfter(endDate)) {
            // Skip Sundays
            if (currentDate.getDayOfWeek().getValue() == 7) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            for (int period = 1; period <= 8; period++) {
                Attendance attendance = attendanceRepository
                        .findByStudentRollNoAndDateAndPeriod(rollNo, currentDate, period)
                        .orElse(new Attendance());

                attendance.setStudent(request.getStudent());
                attendance.setDate(currentDate);
                attendance.setPeriod(period);
                attendance.setStatus("A");
                attendance.setPresent(false);
                attendance.setSemester(semester != null ? semester : 1);

                attendanceRepository.save(attendance);
            }

            currentDate = currentDate.plusDays(1);
        }
    }

    // Convert entity to DTO
    private LeaveRequestDTO convertToDTO(LeaveRequest request) {
        return new LeaveRequestDTO(
                request.getId(),
                request.getStudent().getName(),
                request.getStudent().getRollNo(),
                request.getRequestType(),
                request.getFromDate(),
                request.getToDate(),
                request.getReason(),
                request.getStatus(),
                request.getAppliedOn(),
                request.getTeacherRemarks(),
                request.getReviewedOn(),
                request.getTeacher() != null ? request.getTeacher().getName() : "General/Department Head");
    }
}

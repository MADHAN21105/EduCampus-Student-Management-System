package com.college.student_management.repository;

import com.college.student_management.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByStudentRollNo(String rollNo);

    List<LeaveRequest> findByStatus(String status);

    List<LeaveRequest> findByStudentRollNoAndStatus(String rollNo, String status);

    List<LeaveRequest> findByStatusOrderByAppliedOnDesc(String status);

    List<LeaveRequest> findByTeacherFacultyIdAndStatusOrderByAppliedOnDesc(String facultyId, String status);

    List<LeaveRequest> findAllByOrderByAppliedOnDesc();
}

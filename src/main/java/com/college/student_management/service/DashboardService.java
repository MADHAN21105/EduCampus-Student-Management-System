package com.college.student_management.service;

import com.college.student_management.repository.StudentRepository;
import com.college.student_management.repository.TeacherRepository;
import com.college.student_management.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final StudentRepository studentRepo;
    private final TeacherRepository teacherRepo;
    private final SubjectRepository subjectRepo;
    private final com.college.student_management.repository.AttendanceRepository attendanceRepo;
    private final com.college.student_management.repository.MarksRepository marksRepo;

    public DashboardService(StudentRepository studentRepo, TeacherRepository teacherRepo,
            SubjectRepository subjectRepo,
            com.college.student_management.repository.AttendanceRepository attendanceRepo,
            com.college.student_management.repository.MarksRepository marksRepo) {
        this.studentRepo = studentRepo;
        this.teacherRepo = teacherRepo;
        this.subjectRepo = subjectRepo;
        this.attendanceRepo = attendanceRepo;
        this.marksRepo = marksRepo;
    }

    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", studentRepo.count());
        stats.put("totalTeachers", teacherRepo.count());
        stats.put("totalSubjects", subjectRepo.count());
        stats.put("totalClasses", 10); // Placeholder for classes
        stats.put("attendanceOverview", "92%"); // Placeholder
        return stats;
    }

    public Map<String, Object> getTeacherStats(String teacherId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("assignedSubjects", subjectRepo.countByTeacherFacultyId(teacherId));
        stats.put("assignedClasses", 3); // Placeholder
        stats.put("todayAttendance", "95%"); // Placeholder
        stats.put("totalStudents", studentRepo.count());
        return stats;
    }

    public Map<String, Object> getStudentStats(String studentId) {
        Map<String, Object> stats = new HashMap<>();

        java.util.List<com.college.student_management.entity.Attendance> attendanceList = attendanceRepo
                .findByStudentRollNo(studentId);

        long presentCount = attendanceList.stream()
                .filter(a -> "P".equals(a.getStatus()) || "OD".equals(a.getStatus()) || "ML".equals(a.getStatus()))
                .count();

        long workedHours = attendanceList.stream()
                .filter(a -> !"NT".equals(a.getStatus()) && !"-".equals(a.getStatus()))
                .count();

        double attendancePercentage = workedHours == 0 ? 0 : (double) presentCount / workedHours * 100;

        stats.put("attendancePercentage", String.format("%.1f", attendancePercentage));

        // Calculate dynamic academic results
        java.util.List<com.college.student_management.entity.Marks> marks = marksRepo.findByStudentRollNo(studentId);
        if (marks.isEmpty()) {
            stats.put("examResults", "N/A");
            stats.put("grade", "N/A");
            stats.put("cgpa", "0.0");
            stats.put("cgpaPercentage", 0);
        } else {
            double avg = marks.stream().mapToInt(m -> m.getMarks()).average().orElse(0);
            stats.put("examResults", avg >= 40 ? "Pass" : "Fail");

            // CGPA Calculation: Average Marks / 10
            double cgpa = avg / 10.0;
            stats.put("cgpa", String.format("%.1f", cgpa));
            stats.put("cgpaPercentage", (int) avg);

            if (avg >= 90)
                stats.put("grade", "A+");
            else if (avg >= 80)
                stats.put("grade", "A");
            else if (avg >= 70)
                stats.put("grade", "B");
            else if (avg >= 60)
                stats.put("grade", "C");
            else if (avg >= 50)
                stats.put("grade", "D");
            else
                stats.put("grade", "F");
        }

        return stats;
    }

    public Map<String, Object> getAiInsights(String studentId) {
        Map<String, Object> insights = new HashMap<>();
        insights.put("prediction", "Likely to score 85%+ based on attendance.");
        insights.put("suggestion", "Focus more on Mathematics to improve overall grade.");
        return insights;
    }
}

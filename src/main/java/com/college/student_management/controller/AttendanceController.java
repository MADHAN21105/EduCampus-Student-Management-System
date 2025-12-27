package com.college.student_management.controller;

import com.college.student_management.dto.AttendanceSummaryDTO;
import com.college.student_management.entity.*;
import com.college.student_management.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceRepository repo;
    private final AttendanceWindowRepository windowRepo;
    private final StudentRepository studentRepo;

    public AttendanceController(AttendanceRepository repo, AttendanceWindowRepository windowRepo,
            StudentRepository studentRepo) {
        this.repo = repo;
        this.windowRepo = windowRepo;
        this.studentRepo = studentRepo;
    }

    // ✅ Teacher marks attendance (Upsert logic)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Attendance markAttendance(@RequestBody Attendance attendance) {
        LocalDate date = attendance.getDate() != null ? attendance.getDate() : LocalDate.now();
        String rollNo = attendance.getStudent().getRollNo();
        Integer period = attendance.getPeriod();

        return repo.findByStudentRollNoAndDateAndPeriod(rollNo, date, period)
                .map(existing -> {
                    existing.setStatus(attendance.getStatus());
                    existing.setSemester(attendance.getSemester());
                    // Sync boolean present for DB compatibility
                    existing.setPresent(!"A".equals(attendance.getStatus()));
                    return repo.save(existing);
                })
                .orElseGet(() -> {
                    if (attendance.getDate() == null)
                        attendance.setDate(date);
                    // Sync boolean present for DB compatibility
                    attendance.setPresent(!"A".equals(attendance.getStatus()));
                    return repo.save(attendance);
                });
    }

    // ✅ View attendance by student
    @GetMapping("/student/{rollNo}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public List<Attendance> getByStudent(@PathVariable String rollNo) {
        return repo.findByStudentRollNo(rollNo);
    }

    // ✅ View detailed summary for student and semester
    @GetMapping("/student/{rollNo}/summary/semester/{semester}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public AttendanceSummaryDTO getSummary(@PathVariable String rollNo, @PathVariable Integer semester) {
        Student student = studentRepo.findById(rollNo).orElseThrow();
        List<Attendance> list = repo.findByStudentRollNoAndSemester(rollNo, semester);
        AttendanceSummaryDTO dto = new AttendanceSummaryDTO();
        dto.setSemester(semester);

        long pCount = list.stream().filter(att -> "P".equals(att.getStatus())).count();
        long odCount = list.stream().filter(att -> "OD".equals(att.getStatus())).count();
        long mlCount = list.stream().filter(att -> "ML".equals(att.getStatus())).count();
        long aCount = list.stream().filter(att -> "A".equals(att.getStatus())).count();
        long worked = pCount + odCount + mlCount + aCount;

        dto.setPresentHours(pCount);
        dto.setOnDutyHours(odCount);
        dto.setMedicalLeaveHours(mlCount);
        dto.setAbsentHours(aCount);
        dto.setWorkedHours(worked);

        double pct = worked == 0 ? 0 : (double) (pCount + odCount + mlCount) / worked * 100;
        dto.setAttendancePercentage(String.format("%.2f", pct));
        dto.setEligible(pct >= 75.0);

        // Fetch Window for Grid Generation
        Optional<AttendanceWindow> windowOpt = windowRepo.findByDepartmentAndSemester(student.getDepartment(),
                semester);
        Map<LocalDate, Map<String, Object>> dailyMap = new TreeMap<>();

        if (windowOpt.isPresent()) {
            AttendanceWindow window = windowOpt.get();
            dto.setEnrollmentDate(window.getReopenDate().toString());
            dto.setEndDate(window.getLastWorkingDay().toString());
            dto.setPeriodsPerDay(window.getPeriodsPerDay());

            // 1. Pre-populate all dates in range
            LocalDate current = window.getReopenDate();
            while (!current.isAfter(window.getLastWorkingDay())) {
                Map<String, Object> dayRow = new HashMap<>();
                dayRow.put("date", current.toString());
                dayRow.put("day", current.getDayOfWeek().toString());

                boolean isSun = current.getDayOfWeek().getValue() == 7;
                dayRow.put("dayType", isSun ? "Holiday" : "Working");
                dayRow.put("description", isSun ? "Sunday" : "–");

                String defaultStatus = "–";
                for (int i = 1; i <= window.getPeriodsPerDay(); i++) {
                    dayRow.put("P" + i, defaultStatus);
                }
                dailyMap.put(current, dayRow);
                current = current.plusDays(1);
            }
        } else {
            // Fallback for missing window
            dto.setPeriodsPerDay(8);
        }

        // 2. Overwrite with actual marked attendance
        for (Attendance att : list) {
            dailyMap.putIfAbsent(att.getDate(), new HashMap<>());
            Map<String, Object> dayRow = dailyMap.get(att.getDate());
            if (!dayRow.containsKey("date")) {
                dayRow.put("date", att.getDate().toString());
                dayRow.put("day", att.getDate().getDayOfWeek().toString());
                dayRow.put("dayType", "Working");
                dayRow.put("description", "–");
            }
            dayRow.put("P" + att.getPeriod(), att.getStatus());
        }

        dto.setDailyAttendance(new ArrayList<>(dailyMap.values()));
        return dto;
    }

    // ✅ Generate Bulk Attendance Grid (NT = Not Taken)
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public String generateAttendance(@RequestBody AttendanceWindow window) {
        // 1. Save or Update the Window
        windowRepo.findByDepartmentAndSemester(window.getDepartment(), window.getSemester())
                .ifPresent(existing -> window.setId(existing.getId()));
        windowRepo.save(window);

        // 2. Fetch Students
        List<Student> students = studentRepo.findByDepartmentAndSemester(window.getDepartment(), window.getSemester());

        // 3. Generate Records
        List<Attendance> batch = new ArrayList<>();
        LocalDate current = window.getReopenDate();
        while (!current.isAfter(window.getLastWorkingDay())) {
            String status = "–";
            boolean present = current.getDayOfWeek().getValue() != 7;

            for (Student s : students) {
                for (int p = 1; p <= window.getPeriodsPerDay(); p++) {
                    // Check if already exists to avoid duplicates
                    if (!repo.findByStudentRollNoAndDateAndPeriod(s.getRollNo(), current, p).isPresent()) {
                        Attendance att = new Attendance();
                        att.setStudent(s);
                        att.setDate(current);
                        att.setPeriod(p);
                        att.setStatus(status);
                        att.setSemester(window.getSemester());
                        att.setPresent(present);
                        batch.add(att);
                    }
                }
            }
            current = current.plusDays(1);
        }

        repo.saveAll(batch);
        return "Generated " + batch.size() + " attendance records.";
    }

    // ✅ Get all attendance records for a specific date
    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<Attendance> getByDate(@PathVariable LocalDate date) {
        return repo.findByDate(date);
    }

    // ✅ List all semester windows (Academic Calendar)
    @GetMapping("/windows")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<AttendanceWindow> getAllWindows() {
        return windowRepo.findAll();
    }
}

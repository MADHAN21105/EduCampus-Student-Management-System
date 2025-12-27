package com.college.student_management.service;

import com.college.student_management.dto.ResultResponse;
import com.college.student_management.entity.Marks;
import com.college.student_management.entity.Subject;
import com.college.student_management.repository.MarksRepository;
import com.college.student_management.repository.SubjectRepository;
import com.college.student_management.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResultService {

    private final MarksRepository marksRepo;
    private final SubjectRepository subjectRepo;
    private final StudentRepository studentRepo;

    public ResultService(MarksRepository marksRepo, SubjectRepository subjectRepo, StudentRepository studentRepo) {
        this.marksRepo = marksRepo;
        this.subjectRepo = subjectRepo;
        this.studentRepo = studentRepo;
    }

    public ResultResponse getSemesterResult(String rollNo, int semester) {
        List<Marks> semesterMarks = marksRepo.findByStudentRollNoAndSemester(rollNo, semester);
        ResultResponse response = new ResultResponse();
        response.setRollNo(rollNo);
        response.setSemester(semester);

        if (semesterMarks.isEmpty())
            return response;

        response.setName(semesterMarks.get(0).getStudent().getName());

        List<ResultResponse.SubjectResult> subjectResults = new ArrayList<>();
        double totalWeightedGP = 0;
        int totalCredits = 0;

        for (Marks mark : semesterMarks) {
            Subject subject = subjectRepo.findByName(mark.getSubject()).orElse(null);
            int credits = (subject != null) ? subject.getCredits() : 0;

            subjectResults.add(new ResultResponse.SubjectResult(
                    mark.getSubject(),
                    mark.getMarks(),
                    mark.getGrade(),
                    credits,
                    mark.getGradePoint()));

            totalWeightedGP += mark.getGradePoint() * credits;
            totalCredits += credits;
        }

        response.setSubjects(subjectResults);
        response.setGpa(totalCredits > 0 ? totalWeightedGP / totalCredits : 0);
        response.setCgpa(calculateCGPA(rollNo));

        return response;
    }

    public double calculateCGPA(String rollNo) {
        List<Marks> allMarks = marksRepo.findByStudentRollNo(rollNo);
        if (allMarks.isEmpty())
            return 0;

        Map<Integer, List<Marks>> marksBySemester = allMarks.stream()
                .collect(Collectors.groupingBy(Marks::getSemester));

        double sumGPA = 0;
        int activeSemesters = 0;

        for (Map.Entry<Integer, List<Marks>> entry : marksBySemester.entrySet()) {
            double totalWeightedGP = 0;
            int totalCredits = 0;
            for (Marks mark : entry.getValue()) {
                Subject subject = subjectRepo.findByName(mark.getSubject()).orElse(null);
                int credits = (subject != null) ? subject.getCredits() : 0;
                totalWeightedGP += mark.getGradePoint() * credits;
                totalCredits += credits;
            }
            if (totalCredits > 0) {
                sumGPA += totalWeightedGP / totalCredits;
                activeSemesters++;
            }
        }

        return activeSemesters > 0 ? sumGPA / activeSemesters : 0;
    }

    public Map<String, Object> getOverallPerformance(String rollNo) {
        List<Marks> allMarks = marksRepo.findByStudentRollNo(rollNo);
        Map<String, Object> performance = new HashMap<>();

        if (allMarks.isEmpty()) {
            performance.put("cgpa", 0.0);
            performance.put("semesterWiseGPA", new HashMap<>());
            return performance;
        }

        Map<Integer, List<Marks>> marksBySemester = allMarks.stream()
                .collect(Collectors.groupingBy(Marks::getSemester));

        Map<Integer, Double> semesterGPA = new HashMap<>();
        double sumGPA = 0;
        int activeSemesters = 0;

        for (Map.Entry<Integer, List<Marks>> entry : marksBySemester.entrySet()) {
            double totalWeightedGP = 0;
            int totalCredits = 0;
            for (Marks mark : entry.getValue()) {
                Subject subject = subjectRepo.findByName(mark.getSubject()).orElse(null);
                int credits = (subject != null) ? subject.getCredits() : 0;
                totalWeightedGP += mark.getGradePoint() * credits;
                totalCredits += credits;
            }
            if (totalCredits > 0) {
                double gpa = totalWeightedGP / totalCredits;
                semesterGPA.put(entry.getKey(), gpa);
                sumGPA += gpa;
                activeSemesters++;
            }
        }

        performance.put("cgpa", activeSemesters > 0 ? sumGPA / activeSemesters : 0);
        performance.put("semesterWiseGPA", semesterGPA);
        return performance;
    }

    public Map<String, Double> getDepartmentAverageGPAs() {
        List<String> departments = studentRepo.findAll().stream()
                .map(s -> s.getDepartment())
                .filter(d -> d != null)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Double> deptAverages = new HashMap<>();

        for (String dept : departments) {
            List<com.college.student_management.entity.Student> students = studentRepo.findByDepartment(dept);
            double sumCGPA = 0;
            int count = 0;
            for (com.college.student_management.entity.Student s : students) {
                double cgpa = calculateCGPA(s.getRollNo());
                if (cgpa > 0) {
                    sumCGPA += cgpa;
                    count++;
                }
            }
            if (count > 0) {
                deptAverages.put(dept, sumCGPA / count);
            } else {
                deptAverages.put(dept, 0.0);
            }
        }
        return deptAverages;
    }
}

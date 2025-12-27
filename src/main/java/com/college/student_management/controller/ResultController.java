package com.college.student_management.controller;

import com.college.student_management.dto.ResultResponse;
import com.college.student_management.service.ResultService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/student/{rollNo}/semester/{semester}")
    public ResultResponse getSemesterResult(@PathVariable String rollNo, @PathVariable int semester) {
        return resultService.getSemesterResult(rollNo, semester);
    }

    @GetMapping("/student/{rollNo}/overall")
    public Map<String, Object> getOverallPerformance(@PathVariable String rollNo) {
        return resultService.getOverallPerformance(rollNo);
    }

    @GetMapping("/department-averages")
    public Map<String, Double> getDepartmentAverageGPAs() {
        return resultService.getDepartmentAverageGPAs();
    }
}

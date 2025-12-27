package com.college.student_management.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentProfileController {

    @GetMapping("/profile")
    public String profile(Authentication auth) {
        return "Welcome student: " + auth.getName();
    }
}

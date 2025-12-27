package com.college.student_management.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    private final PasswordEncoder encoder;

    public DebugController(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @GetMapping("/debug/encode")
    public String encodePassword() {
        return encoder.encode("admin123");
    }
}

package com.college.student_management.controller;

import com.college.student_management.dto.LoginRequest;
import com.college.student_management.dto.LoginResponse;
import com.college.student_management.dto.RegisterRequest;
import com.college.student_management.entity.User;
import com.college.student_management.security.JwtUtil;
import com.college.student_management.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            User user = authService.authenticate(
                    request.getUsername(),
                    request.getPassword(),
                    request.getRole());

            String token = jwtUtil.generateToken(
                    user.getUsername(),
                    user.getRole().name());

            // Create response with temporary password flag
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", user.getRole().name());
            response.put("temporaryPassword", user.isTemporaryPassword());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException ex) {
            // 🔥 THIS FIXES 500 → 401
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    ex.getMessage());
        }
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        try {
            return authService.register(
                    request.getUsername(),
                    request.getPassword(),
                    request.getRole());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            authService.changePassword(username, oldPassword, newPassword);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage());
        }
    }
}

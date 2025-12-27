package com.college.student_management.integration;

import com.college.student_management.dto.LoginRequest;
import com.college.student_management.dto.RegisterRequest;
import com.college.student_management.entity.Role;
import com.college.student_management.entity.User;
import com.college.student_management.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Authentication Integration Tests")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should complete full registration and login flow")
    void testCompleteAuthFlow() throws Exception {
        // Step 1: Register new user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("integrationtest");
        registerRequest.setPassword("password123");
        registerRequest.setRole("STUDENT");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Step 2: Login with registered credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("integrationtest");
        loginRequest.setPassword("password123");
        loginRequest.setRole("STUDENT");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("Should prevent duplicate username registration")
    void testDuplicateRegistration() throws Exception {
        // Step 1: Register first user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("duplicate");
        registerRequest.setPassword("password123");
        registerRequest.setRole("STUDENT");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Step 2: Try to register with same username
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle temporary password flow")
    void testTemporaryPasswordFlow() throws Exception {
        // Arrange: Create user with temporary password
        User tempUser = new User();
        tempUser.setUsername("tempuser");
        tempUser.setPassword(passwordEncoder.encode("temp123"));
        tempUser.setRole(Role.STUDENT);
        tempUser.setTemporaryPassword(true);
        userRepository.save(tempUser);

        // Act: Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("tempuser");
        loginRequest.setPassword("temp123");
        loginRequest.setRole("STUDENT");

        // Assert: Should indicate temporary password
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temporaryPassword").value(true));
    }

    @Test
    @DisplayName("Should reject login with wrong password")
    void testLoginWithWrongPassword() throws Exception {
        // Arrange: Create user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("correctpassword"));
        user.setRole(Role.STUDENT);
        userRepository.save(user);

        // Act: Try to login with wrong password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");
        loginRequest.setRole("STUDENT");

        // Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject login with non-existent user")
    void testLoginWithNonExistentUser() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("password123");
        loginRequest.setRole("STUDENT");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}

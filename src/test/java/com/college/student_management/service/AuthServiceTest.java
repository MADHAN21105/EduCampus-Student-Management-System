package com.college.student_management.service;

import com.college.student_management.dto.LoginRequest;
import com.college.student_management.dto.LoginResponse;
import com.college.student_management.dto.RegisterRequest;
import com.college.student_management.entity.Role;
import com.college.student_management.entity.User;
import com.college.student_management.repository.UserRepository;
import com.college.student_management.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.STUDENT);
        testUser.setTemporaryPassword(false);

        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setRole("STUDENT");

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        loginRequest.setRole("STUDENT");
    }

    @Test
    @DisplayName("Should successfully register new user")
    void testRegisterUser_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = authService.register(registerRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegisterUser_UsernameExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_Success() {
        // Arrange
        when(userRepository.findByUsernameAndRole(anyString(), any(Role.class)))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("test-jwt-token");

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("test-jwt-token");
        assertThat(response.getRole()).isEqualTo("STUDENT");
        assertThat(response.isTemporaryPassword()).isFalse();
        verify(jwtUtil).generateToken("testuser", "STUDENT");
    }

    @Test
    @DisplayName("Should fail login with invalid password")
    void testLogin_InvalidPassword() {
        // Arrange
        when(userRepository.findByUsernameAndRole(anyString(), any(Role.class)))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");

        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Should fail login when user not found")
    void testLogin_UserNotFound() {
        // Arrange
        when(userRepository.findByUsernameAndRole(anyString(), any(Role.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("Should return temporary password flag when user has temporary password")
    void testLogin_TemporaryPassword() {
        // Arrange
        testUser.setTemporaryPassword(true);
        when(userRepository.findByUsernameAndRole(anyString(), any(Role.class)))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("test-jwt-token");

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response.isTemporaryPassword()).isTrue();
    }

    @Test
    @DisplayName("Should successfully change password")
    void testChangePassword_Success() {
        // Arrange
        String username = "testuser";
        String oldPassword = "oldPass123";
        String newPassword = "newPass456";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.changePassword(username, oldPassword, newPassword);

        // Assert
        verify(userRepository)
                .save(argThat(user -> !user.isTemporaryPassword() && user.getPassword().equals("newEncodedPassword")));
    }

    @Test
    @DisplayName("Should fail password change with incorrect old password")
    void testChangePassword_WrongOldPassword() {
        // Arrange
        String username = "testuser";
        String oldPassword = "wrongPassword";
        String newPassword = "newPass456";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.changePassword(username, oldPassword, newPassword))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid old password");

        verify(userRepository, never()).save(any(User.class));
    }
}

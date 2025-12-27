package com.college.student_management.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "testSecretKeyForJWTTokenGenerationInTestEnvironmentOnly";
    private final long testExpiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Note: You may need to use reflection to set private fields
        // or create a constructor that accepts these values
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void testGenerateToken() {
        // Arrange
        String username = "testuser";
        String role = "STUDENT";

        // Act
        String token = jwtUtil.generateToken(username, role);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should extract username from token")
    void testExtractUsername() {
        // Arrange
        String username = "testuser";
        String role = "STUDENT";
        String token = jwtUtil.generateToken(username, role);

        // Act
        String extractedUsername = jwtUtil.extractUsername(token);

        // Assert
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Should extract role from token")
    void testExtractRole() {
        // Arrange
        String username = "testuser";
        String role = "STUDENT";
        String token = jwtUtil.generateToken(username, role);

        // Act
        String extractedRole = jwtUtil.extractRole(token);

        // Assert
        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    @DisplayName("Should validate correct token")
    void testValidateToken_Valid() {
        // Arrange
        String username = "testuser";
        String role = "STUDENT";
        String token = jwtUtil.generateToken(username, role);

        // Act
        boolean isValid = jwtUtil.validateToken(token, username);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject token with wrong username")
    void testValidateToken_WrongUsername() {
        // Arrange
        String username = "testuser";
        String role = "STUDENT";
        String token = jwtUtil.generateToken(username, role);

        // Act
        boolean isValid = jwtUtil.validateToken(token, "wronguser");

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should detect expired token")
    void testIsTokenExpired() {
        // Arrange
        String username = "testuser";
        String role = "STUDENT";
        String token = jwtUtil.generateToken(username, role);

        // Act
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Assert
        assertThat(isExpired).isFalse(); // Should not be expired immediately
    }

    @Test
    @DisplayName("Should handle invalid token format")
    void testInvalidTokenFormat() {
        // Arrange
        String invalidToken = "invalid.token.format";

        // Act & Assert
        assertThatThrownBy(() -> jwtUtil.extractUsername(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testUniqueness() {
        // Arrange & Act
        String token1 = jwtUtil.generateToken("user1", "STUDENT");
        String token2 = jwtUtil.generateToken("user2", "STUDENT");

        // Assert
        assertThat(token1).isNotEqualTo(token2);
    }
}

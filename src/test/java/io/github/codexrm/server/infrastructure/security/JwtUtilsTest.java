package io.github.codexrm.server.infrastructure.security;

import io.github.codexrm.server.infrastructure.security.jwt.JwtUtils;
import io.github.codexrm.server.infrastructure.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();

        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "testSecretKeytestSecretKeytestSecretKey");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 5000); // 5 segundos

        userDetails = new UserDetailsImpl(
                1,
                "testuser",
                "Name",
                "Last",
                "test@test.com",
                true,
                "password",
                List.of(() -> "ROLE_USER")
        );
    }

    // Generate Token
    @Test
    void shouldGenerateJwtToken() {
        String token = jwtUtils.generateJwtToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    // Valid Token
    @Test
    void shouldValidateValidJwtToken() {
        String token = jwtUtils.generateJwtToken(userDetails);

        boolean isValid = jwtUtils.validateJwtToken(token);

        assertTrue(isValid);
    }

    // Expired Token
    @Test
    void shouldFailValidationForExpiredToken() throws InterruptedException {

        String token = jwtUtils.generateJwtToken(userDetails);

        Thread.sleep(5500);

        boolean isValid = jwtUtils.validateJwtToken(token);

        assertFalse(isValid);
    }

    // Invalid Token
    @Test
    void shouldFailValidationForInvalidToken() {
        String invalidToken = "this.is.not.a.valid.token";

        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        assertFalse(isValid);
    }

    // Get Username
    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtUtils.generateJwtToken(userDetails);

        String username = jwtUtils.getUserNameFromJwtToken(token);

        assertEquals("testuser", username);
    }

    // Tampered Token
    @Test
    void shouldFailValidationForTamperedToken() {

        String token = jwtUtils.generateJwtToken(userDetails);

        String tamperedToken = token + "tampered";

        boolean isValid = jwtUtils.validateJwtToken(tamperedToken);

        assertFalse(isValid);
    }
}
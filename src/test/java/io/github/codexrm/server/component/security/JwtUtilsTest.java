package io.github.codexrm.server.component.security;

import io.github.codexrm.server.security.jwt.JwtUtils;
import io.github.codexrm.server.security.services.UserDetailsImpl;
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
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1000); // 1 segundo

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

    // GENERATE TOKEN
    @Test
    void shouldGenerateJwtToken() {
        String token = jwtUtils.generateJwtToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    // VALID TOKEN
    @Test
    void shouldValidateValidJwtToken() {
        String token = jwtUtils.generateJwtToken(userDetails);

        boolean isValid = jwtUtils.validateJwtToken(token);

        assertTrue(isValid);
    }

    // EXPIRED TOKEN
    @Test
    void shouldFailValidationForExpiredToken() throws InterruptedException {

        String token = jwtUtils.generateJwtToken(userDetails);

        Thread.sleep(1500);

        boolean isValid = jwtUtils.validateJwtToken(token);

        assertFalse(isValid);
    }

    // INVALID TOKEN
    @Test
    void shouldFailValidationForInvalidToken() {
        String invalidToken = "this.is.not.a.valid.token";

        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        assertFalse(isValid);
    }

    // GET USERNAME
    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtUtils.generateJwtToken(userDetails);

        String username = jwtUtils.getUserNameFromJwtToken(token);

        assertEquals("testuser", username);
    }
}
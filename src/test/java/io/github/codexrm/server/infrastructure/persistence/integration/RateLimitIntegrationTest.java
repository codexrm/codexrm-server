package io.github.codexrm.server.infrastructure.persistence.integration;

import io.github.codexrm.server.api.dto.request.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

// Overrides the lax integration-test rate limit (1000/min, set so
// AuthIntegrationTest's many signin calls don't trip each other up)
// with a low, deterministic limit just for this test class, so the
// 429 behavior itself can be verified without waiting a full minute
// or making a thousand requests.
@TestPropertySource(properties = {
        "codexrm.ratelimit.auth.capacity=3",
        "codexrm.ratelimit.auth.refill-tokens=3",
        "codexrm.ratelimit.auth.refill-minutes=1"
})
public class RateLimitIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void setUp() {
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();
        restTemplate.getRestTemplate().setRequestFactory(factory);
    }

    @Test
    void shouldReturn429AfterExceedingSigninRateLimitWithCorrelationId() {

        LoginRequest badLogin = new LoginRequest();
        badLogin.setUsername("nonexistentUser");
        badLogin.setPassword("wrongPassword");

        // First 3 attempts consume the configured capacity (401 for bad
        // credentials is expected and fine — what matters is that they're
        // NOT rate-limited yet).
        for (int i = 0; i < 3; i++) {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url("/api/auth/signin"), badLogin, String.class);

            assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        }

        // The 4th attempt must be rejected before even checking credentials.
        ResponseEntity<String> fourthAttempt =
                restTemplate.postForEntity(url("/api/auth/signin"), badLogin, String.class);

        assertThat(fourthAttempt.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(fourthAttempt.getHeaders().get("X-Correlation-Id")).isNotNull();
    }
}
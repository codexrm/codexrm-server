package io.github.codexrm.server.infrastructure.persistence.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codexrm.server.api.dto.request.LoginRequest;
import io.github.codexrm.server.api.dto.request.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class ReferenceAuthorizationIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void setUp() {
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();
        restTemplate.getRestTemplate().setRequestFactory(factory);
    }


    // Helpers
    private String signupAndLogin(String username, String email) throws Exception {

        SignupRequest signup = new SignupRequest();
        signup.setUsername(username);
        signup.setPassword("Test@123");
        signup.setEmail(email);
        signup.setName("User");
        signup.setLastName("Test");
        signup.setEnabled(true);

        ResponseEntity<String> signupResponse =
                restTemplate.postForEntity(url("/api/auth/signup"), signup, String.class);

        assertThat(signupResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        LoginRequest login = new LoginRequest();
        login.setUsername(username);
        login.setPassword("Test@123");

        ResponseEntity<String> loginResponse =
                restTemplate.postForEntity(url("/api/auth/signin"), login, String.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode json = objectMapper.readTree(loginResponse.getBody());
        return json.get("token").asText();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String createReference(HttpHeaders headers, String title) {

        String body = """
                {
                  "title": "%s",
                  "year": "2018",
                  "month": "mar",
                  "note": "Second edition",
                  "referenceType": "WebPageReferenceDTO",
                  "author": "Perez,Maria",
                  "url": "https://example.com/article"
                }
                """.formatted(title);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/references"),
                        HttpMethod.POST,
                        entity,
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        return response.getBody(); // assuming it returns JSON with id or similar
    }

    // Test
    @Test
    void userShouldOnlySeeOwnReferences() throws Exception {

        String tokenA = signupAndLogin("userA", "a@test.com");
        String tokenB = signupAndLogin("userB", "b@test.com");

        HttpHeaders headersA = authHeaders(tokenA);
        HttpHeaders headersB = authHeaders(tokenB);

        createReference(headersA, "Ref A1");
        createReference(headersA, "Ref A2");
        createReference(headersB, "Ref B1");

        ResponseEntity<String> responseA =
                restTemplate.exchange(url("/api/references"), HttpMethod.GET,
                        new HttpEntity<>(headersA), String.class);

        ResponseEntity<String> responseB =
                restTemplate.exchange(url("/api/references"), HttpMethod.GET,
                        new HttpEntity<>(headersB), String.class);

        assertThat(responseA.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseB.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(responseA.getBody()).contains("Ref A1");
        assertThat(responseA.getBody()).contains("Ref A2");

        assertThat(responseB.getBody()).contains("Ref B1");
        assertThat(responseB.getBody()).doesNotContain("Ref A1");
    }

    @Test
    void userShouldNotAccessOtherUsersReferenceById() throws Exception {

        String tokenA = signupAndLogin("userC", "c@test.com");
        String tokenB = signupAndLogin("userD", "d@test.com");

        HttpHeaders headersA = authHeaders(tokenA);
        HttpHeaders headersB = authHeaders(tokenB);

        String refJson = createReference(headersA, "Secret Ref");

        JsonNode node = objectMapper.readTree(refJson);
        String id = node.get("id").asText();

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/references/" + id),
                        HttpMethod.GET,
                        new HttpEntity<>(headersB),
                        String.class
                );

        assertThat(response.getStatusCode())
                .isIn(HttpStatus.FORBIDDEN, HttpStatus.NOT_FOUND);
    }

    @Test
    void userShouldNotDeleteOtherUsersReference() throws Exception {

        String tokenA = signupAndLogin("userE", "e@test.com");
        String tokenB = signupAndLogin("userF", "f@test.com");

        HttpHeaders headersA = authHeaders(tokenA);
        HttpHeaders headersB = authHeaders(tokenB);

        String refJson = createReference(headersA, "Delete Test Ref");

        JsonNode node = objectMapper.readTree(refJson);
        String id = node.get("id").asText();

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/references/" + id),
                        HttpMethod.DELETE,
                        new HttpEntity<>(headersB),
                        String.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void unauthorizedUserShouldBeBlocked() {

        ResponseEntity<String> response =
                restTemplate.getForEntity(url("/api/references"), String.class);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void ownerShouldDeleteOwnReference() throws Exception {

        String token = signupAndLogin("owner", "owner@test.com");

        HttpHeaders headers = authHeaders(token);

        String refJson = createReference(headers, "My Ref");

        JsonNode node = objectMapper.readTree(refJson);
        String id = node.get("id").asText();

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/references/" + id),
                        HttpMethod.DELETE,
                        new HttpEntity<>(headers),
                        String.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }
}
package io.github.codexrm.server.infrastructure.persistence.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codexrm.server.api.dto.request.LoginRequest;
import io.github.codexrm.server.api.dto.request.SignupRequest;
import io.github.codexrm.server.infrastructure.persistence.repository.ReferenceRepository;
import io.github.codexrm.server.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReferenceFlowIntegrationTest extends BaseIntegrationTest {

    private final String username = "test" + System.currentTimeMillis();

    private final String email = "test" + System.currentTimeMillis() + "@test.com";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReferenceRepository referenceRepository;

    @Test
    void shouldCompleteFullReferenceFlow() throws Exception {

        signupUser();
        String token = loginAndGetToken();
        HttpHeaders headers = authHeaders(token);
        createReference(headers);

        ResponseEntity<String> listResponse = restTemplate.exchange(url("/api/references"), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(listResponse.getBody()).contains("Deep Learning");
    }

    private void signupUser() {

        SignupRequest signup = new SignupRequest();
        signup.setUsername(username);
        signup.setPassword("Test@123");
        signup.setEmail(email);
        signup.setName("User");
        signup.setLastName("Test");
        signup.setEnabled(true);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url("/api/auth/signup"),
                        signup,
                        String.class
                );

        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private String loginAndGetToken() throws Exception {

        LoginRequest login = new LoginRequest();
        login.setUsername(username);
        login.setPassword("Test@123");

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url("/api/auth/signin"),
                        login,
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        return extractToken(response);
    }

    private void createReference(HttpHeaders headers) {

        String requestBody = """
                {
                  "title": "Deep Learning",
                  "year": "2018",
                  "month": "mar",
                  "note": "Second edition",
                  "referenceType": "WebPageReferenceDTO",
                  "author": "Perez,Maria",
                  "url": "https://example.com/article"
                }
                """;

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/references"),
                        HttpMethod.POST,
                        entity,
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private HttpHeaders authHeaders(String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String extractToken(ResponseEntity<String> response) throws Exception {

        JsonNode json = objectMapper.readTree(response.getBody());
        return json.get("token").asText();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
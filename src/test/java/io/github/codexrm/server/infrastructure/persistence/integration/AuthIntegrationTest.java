package io.github.codexrm.server.infrastructure.persistence.integration;

import io.github.codexrm.server.api.dto.request.LoginRequest;
import io.github.codexrm.server.api.dto.request.SignupRequest;
import io.github.codexrm.server.api.dto.request.TokenRefreshRequest;
import io.github.codexrm.server.api.dto.response.JwtResponse;
import io.github.codexrm.server.api.dto.response.TokenRefreshResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthIntegrationTest extends BaseIntegrationTest{

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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
    void shouldRejectDuplicateSignup() {

        SignupRequest signup = new SignupRequest();
        signup.setUsername("duplicateUser");
        signup.setPassword("Test@123");
        signup.setEmail("duplicate@test.com");
        signup.setName("User");
        signup.setLastName("Test");
        signup.setEnabled(true);

        ResponseEntity<String> firstResponse =
                restTemplate.postForEntity(
                        url("/api/auth/signup"),
                        signup,
                        String.class
                );

        assertThat(firstResponse.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> secondResponse =
                restTemplate.postForEntity(
                        url("/api/auth/signup"),
                        signup,
                        String.class
                );

        assertThat(secondResponse.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldRejectInvalidLogin() {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("daniel");
        loginRequest.setPassword("Daniel123");

        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/auth/signin"),
                        HttpMethod.POST,
                        request,
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldLoginSuccessfully() {

        SignupRequest signup = new SignupRequest();
        signup.setUsername("loginuser");
        signup.setPassword("Test@123");
        signup.setEmail("login@test.com");
        signup.setName("User");
        signup.setLastName("Test");
        signup.setEnabled(true);

        restTemplate.postForEntity(
                url("/api/auth/signup"),
                signup,
                String.class
        );

        LoginRequest login = new LoginRequest();
        login.setUsername("loginuser");
        login.setPassword("Test@123");

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url("/api/auth/signin"),
                        login,
                        String.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .contains("token");
    }

    @Test
    void shouldRejectAccessToProtectedEndpointWithoutToken() {

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        url("/api/users/me"),
                        String.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldAllowAccessToProtectedEndpointWithJwt() {

        // signup
        SignupRequest signup = new SignupRequest();
        signup.setUsername("jwtuser2");
        signup.setPassword("Test@123");
        signup.setEmail("jwt2@test.com");
        signup.setName("User");
        signup.setLastName("Test");
        signup.setEnabled(true);

        restTemplate.postForEntity(url("/api/auth/signup"), signup, String.class);

        // login
        LoginRequest login = new LoginRequest();
        login.setUsername("jwtuser2");
        login.setPassword("Test@123");

        ResponseEntity<JwtResponse> loginResponse =
                restTemplate.postForEntity(
                        url("/api/auth/signin"),
                        login,
                        JwtResponse.class
                );

        String token = loginResponse.getBody().getToken();

        // request con JWT
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/users/me"),
                        HttpMethod.GET,
                        entity,
                        String.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    // Doble login: el segundo signin del mismo usuario no debe romper con 500
    @Test
    void shouldAllowDoubleLoginWithoutServerError() {

        SignupRequest signup = new SignupRequest();
        signup.setUsername("doubleLoginUser");
        signup.setPassword("Test@123");
        signup.setEmail("doublelogin@test.com");
        signup.setName("User");
        signup.setLastName("Test");
        signup.setEnabled(true);

        restTemplate.postForEntity(url("/api/auth/signup"), signup, String.class);

        LoginRequest login = new LoginRequest();
        login.setUsername("doubleLoginUser");
        login.setPassword("Test@123");

        ResponseEntity<JwtResponse> firstLogin =
                restTemplate.postForEntity(url("/api/auth/signin"), login, JwtResponse.class);

        assertThat(firstLogin.getStatusCode()).isEqualTo(HttpStatus.OK);
        String firstRefreshToken = firstLogin.getBody().getRefreshToken();

        ResponseEntity<JwtResponse> secondLogin =
                restTemplate.postForEntity(url("/api/auth/signin"), login, JwtResponse.class);

        assertThat(secondLogin.getStatusCode()).isEqualTo(HttpStatus.OK);
        String secondRefreshToken = secondLogin.getBody().getRefreshToken();

        // A new refresh token is issued; the old one is replaced, not duplicated.
        assertThat(secondRefreshToken).isNotEqualTo(firstRefreshToken);
    }

    // Rotación: usar el refresh token devuelve uno nuevo, distinto al usado
    @Test
    void shouldRotateRefreshTokenOnUse() throws Exception {

        SignupRequest signup = new SignupRequest();
        signup.setUsername("rotationUser");
        signup.setPassword("Test@123");
        signup.setEmail("rotation@test.com");
        signup.setName("User");
        signup.setLastName("Test");
        signup.setEnabled(true);

        restTemplate.postForEntity(url("/api/auth/signup"), signup, String.class);

        LoginRequest login = new LoginRequest();
        login.setUsername("rotationUser");
        login.setPassword("Test@123");

        ResponseEntity<JwtResponse> loginResponse =
                restTemplate.postForEntity(url("/api/auth/signin"), login, JwtResponse.class);

        String originalRefreshToken = loginResponse.getBody().getRefreshToken();

        TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
        refreshRequest.setRefreshToken(originalRefreshToken);

        ResponseEntity<String> refreshResponse =
                restTemplate.postForEntity(
                        url("/api/auth/refresh-token"),
                        refreshRequest,
                        String.class
                );

        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode body = objectMapper.readTree(refreshResponse.getBody());
        String rotatedRefreshToken = body.get("refreshToken").asText();

        assertThat(rotatedRefreshToken).isNotEqualTo(originalRefreshToken);
    }

    // El refresh token viejo, ya rotado, no debe volver a funcionar
    @Test
    void shouldRejectOldRefreshTokenAfterRotation() {

        SignupRequest signup = new SignupRequest();
        signup.setUsername("oldTokenUser");
        signup.setPassword("Test@123");
        signup.setEmail("oldtoken@test.com");
        signup.setName("User");
        signup.setLastName("Test");
        signup.setEnabled(true);

        restTemplate.postForEntity(url("/api/auth/signup"), signup, String.class);

        LoginRequest login = new LoginRequest();
        login.setUsername("oldTokenUser");
        login.setPassword("Test@123");

        ResponseEntity<JwtResponse> loginResponse =
                restTemplate.postForEntity(url("/api/auth/signin"), login, JwtResponse.class);

        String originalRefreshToken = loginResponse.getBody().getRefreshToken();

        TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
        refreshRequest.setRefreshToken(originalRefreshToken);

        // First use: rotates the token, succeeds
        restTemplate.postForEntity(url("/api/auth/refresh-token"), refreshRequest, String.class);

        // Second use of the SAME (now-rotated-away) token must fail
        ResponseEntity<String> reuseAttempt =
                restTemplate.postForEntity(url("/api/auth/refresh-token"), refreshRequest, String.class);

        assertThat(reuseAttempt.getStatusCode()).isNotEqualTo(HttpStatus.OK);
    }

}

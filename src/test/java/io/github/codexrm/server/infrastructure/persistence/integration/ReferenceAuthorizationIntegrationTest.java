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
import io.github.codexrm.server.domain.enums.ERole;
import io.github.codexrm.server.domain.model.Role;
import io.github.codexrm.server.domain.model.User;
import io.github.codexrm.server.infrastructure.persistence.repository.RoleRepository;
import io.github.codexrm.server.infrastructure.persistence.repository.UserRepository;
import io.github.codexrm.server.infrastructure.security.jwt.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class ReferenceAuthorizationIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

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

    private String signupWithRole(String username, String email, ERole role) {

        Role dbRole = roleRepository.findByName(role)
                .orElseThrow(() -> new IllegalStateException("Role not seeded: " + role));

        User user = new User(username, "User", "Test", email, true,
                passwordEncoder.encode("Test@123"));
        user.setRoles(Set.of(dbRole));

        userRepository.save(user);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        return jwtUtils.generateTokenFromUsername(username);
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

        return response.getBody();
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

    @Test
    void userShouldNotUpdateOtherUsersReference() throws Exception {

        String tokenA = signupAndLogin("userG", "g@test.com");
        String tokenB = signupAndLogin("userH", "h@test.com");

        HttpHeaders headersA = authHeaders(tokenA);
        HttpHeaders headersB = authHeaders(tokenB);

        String refJson = createReference(headersA, "Original Title");
        JsonNode node = objectMapper.readTree(refJson);
        String id = node.get("id").asText();

        String updateBody = """
                {
                  "title": "Hacked Title",
                  "year": "2020",
                  "month": "jan",
                  "note": "tampered",
                  "referenceType": "WebPageReferenceDTO",
                  "author": "Hacker,Evil",
                  "url": "https://evil.com"
                }
                """;

        HttpEntity<String> entity = new HttpEntity<>(updateBody, headersB);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/references/" + id),
                        HttpMethod.PUT,
                        entity,
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void userShouldNotAccessAnotherUsersProfile() throws Exception {

        String tokenA = signupAndLogin("userI", "i@test.com");
        String tokenB = signupAndLogin("userJ", "j@test.com");

        // Get userI's ID
        ResponseEntity<String> meResponse =
                restTemplate.exchange(
                        url("/api/users/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(authHeaders(tokenA)),
                        String.class
                );
        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode meNode = objectMapper.readTree(meResponse.getBody());
        String userAId = meNode.get("id").asText();

        // userB tries to access userA's profile
        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/users/" + userAId),
                        HttpMethod.GET,
                        new HttpEntity<>(authHeaders(tokenB)),
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void unauthorizedUserShouldBeBlockedOnUsersEndpoint() {

        ResponseEntity<String> response =
                restTemplate.getForEntity(url("/api/users/1"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void ownerShouldUpdateOwnReference() throws Exception {

        String token = signupAndLogin("ownerUpdate", "ownerupdate@test.com");
        HttpHeaders headers = authHeaders(token);

        String refJson = createReference(headers, "Original");
        JsonNode node = objectMapper.readTree(refJson);
        String id = node.get("id").asText();

        String updateBody = """
                {
                  "title": "Updated Title",
                  "year": "2021",
                  "month": "feb",
                  "note": "updated note",
                  "referenceType": "WebPageReferenceDTO",
                  "author": "Author,Valid",
                  "url": "https://valid.com"
                }
                """;

        HttpEntity<String> entity = new HttpEntity<>(updateBody, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/references/" + id),
                        HttpMethod.PUT,
                        entity,
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Updated Title");
    }


    // ROL VÁLIDO PERO PERMISO INSUFICIENTE → 403
    @Test
    void regularUserShouldBeForbiddenFromCreatingUsers() throws Exception {

        String token = signupAndLogin("noPermission", "nopermission@test.com");
        HttpHeaders headers = authHeaders(token);

        String body = """
            {
              "username": "someoneelse",
              "email": "someoneelse@test.com",
              "password": "Test@123",
              "name": "Some",
              "lastName": "One",
              "enabled": true,
              "roles": ["ROLE_USER"]
            }
            """;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/users"),
                        HttpMethod.POST,
                        entity,
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // MANAGER puede ver referencias de todos los usuarios
    @Test
    void managerShouldSeeAllUsersReferences() throws Exception {

        String ownerToken = signupAndLogin("ownerForManager", "ownerformanager@test.com");
        createReference(authHeaders(ownerToken), "Visible to manager");

        String managerToken = signupWithRole("managerUser", "manager@test.com", ERole.ROLE_MANAGER);
        HttpHeaders managerHeaders = authHeaders(managerToken);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/references/all-users?page=0&size=10"),
                        HttpMethod.GET,
                        new HttpEntity<>(managerHeaders),
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // USER regular NO puede acceder a all-users (solo MANAGER)
    @Test
    void regularUserShouldBeForbiddenFromAllUsersReferences() throws Exception {

        String token = signupAndLogin("notManager", "notmanager@test.com");
        HttpHeaders headers = authHeaders(token);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/references/all-users?page=0&size=10"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // AUDITOR puede ver el perfil de cualquier usuario
    @Test
    void auditorShouldSeeAnyUserProfile() throws Exception {

        String targetToken = signupAndLogin("auditTarget", "audittarget@test.com");

        JsonNode meNode = objectMapper.readTree(
                restTemplate.exchange(
                        url("/api/users/me"),
                        HttpMethod.GET,
                        new HttpEntity<>(authHeaders(targetToken)),
                        String.class
                ).getBody()
        );
        String targetId = meNode.get("id").asText();

        String auditorToken = signupWithRole("auditorUser", "auditor@test.com", ERole.ROLE_AUDITOR);
        HttpHeaders auditorHeaders = authHeaders(auditorToken);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/users/" + targetId),
                        HttpMethod.GET,
                        new HttpEntity<>(auditorHeaders),
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // EXPORT filtra silenciosamente IDs de referencias ajenas
    @Test
    void exportShouldSilentlyFilterOutOthersReferences() throws Exception {

        String ownerToken = signupAndLogin("exportOwner", "exportowner@test.com");
        HttpHeaders ownerHeaders = authHeaders(ownerToken);
        String ownRefJson = createReference(ownerHeaders, "My own reference");
        String ownId = objectMapper.readTree(ownRefJson).get("id").asText();

        String otherToken = signupAndLogin("exportOther", "exportother@test.com");
        String otherRefJson = createReference(authHeaders(otherToken), "Someone else's reference");
        String otherId = objectMapper.readTree(otherRefJson).get("id").asText();

        String body = "[" + ownId + ", " + otherId + "]";
        HttpEntity<String> entity = new HttpEntity<>(body, ownerHeaders);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url("/api/references/export?fileName=export.ris&format=RIS"),
                        HttpMethod.POST,
                        entity,
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
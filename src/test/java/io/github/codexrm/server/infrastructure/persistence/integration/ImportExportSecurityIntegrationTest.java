package io.github.codexrm.server.infrastructure.persistence.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codexrm.server.api.dto.request.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class ImportExportSecurityIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String VALID_RIS =
            "TY  - JOUR\r\n" +
                    "N1  - aa\r\n" +
                    "AU  - Doe,John\r\n" +
                    "TI  - A Valid Reference\r\n" +
                    "T2  - Some Journal\r\n" +
                    "PY  - 2024\r\n" +
                    "VL  - 1\r\n" +
                    "C7  - 3\r\n" +
                    "SN  - 1234-5678\r\n" +
                    "SP  - 10\r\n" +
                    "ER  - \r\n";

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void setUp() {
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();
        restTemplate.getRestTemplate().setRequestFactory(factory);
    }

    private String signupAndLogin(String username, String email) throws Exception {

        SignupRequest signup = new SignupRequest();
        signup.setUsername(username);
        signup.setPassword("Test@123");
        signup.setEmail(email);
        signup.setName("User");
        signup.setLastName("Test");
        signup.setEnabled(true);

        restTemplate.postForEntity(url("/api/auth/signup"), signup, String.class);

        String loginBody = "{\"username\":\"" + username + "\",\"password\":\"Test@123\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                url("/api/auth/signin"),
                new HttpEntity<>(loginBody, headers),
                String.class
        );

        return objectMapper.readTree(loginResponse.getBody()).get("token").asText();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private HttpEntity<MultiValueMap<String, Object>> multipartRequest(
            String token, String filename, String content, String format) {

        HttpHeaders headers = authHeaders(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource fileResource = new ByteArrayResource(
                content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("uploadFile", fileResource);

        return new HttpEntity<>(body, headers);
    }

    // A valid RIS file is accepted
    @Test
    void shouldAcceptValidRisFile() throws Exception {
        String token = signupAndLogin("importValid", "importvalid@test.com");

        HttpEntity<MultiValueMap<String, Object>> request =
                multipartRequest(token, "references.ris", VALID_RIS, "RIS");

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/references/import?format=RIS"), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // Empty file is rejected
    @Test
    void shouldRejectEmptyFile() throws Exception {
        String token = signupAndLogin("importEmpty", "importempty@test.com");

        HttpEntity<MultiValueMap<String, Object>> request =
                multipartRequest(token, "empty.ris", "", "RIS");

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/references/import?format=RIS"), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // Malicious filename (path traversal) never escapes the upload directory
    @Test
    void shouldNeverWriteOutsideUploadDirectoryOnMaliciousFilename() throws Exception {
        String token = signupAndLogin("importMalicious", "importmalicious@test.com");

        HttpEntity<MultiValueMap<String, Object>> request =
                multipartRequest(token, "../../../../evil.ris", VALID_RIS, "RIS");

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/references/import?format=RIS"), request, String.class);

        File suspiciousFile = new File("evil.ris").getAbsoluteFile();
        assertThat(suspiciousFile).doesNotExist();
    }

    // Temp files are cleaned up even when the import fails partway through
    @Test
    void shouldCleanUpTempFileWhenImportFails() throws Exception {
        String token = signupAndLogin("importCleanup", "importcleanup@test.com");

        String malformedRis = "this is not a valid RIS file at all";

        HttpEntity<MultiValueMap<String, Object>> request =
                multipartRequest(token, "malformed.ris", malformedRis, "RIS");

        restTemplate.postForEntity(url("/api/references/import?format=RIS"), request, String.class);

        String secondToken = signupAndLogin("importCleanup2", "importcleanup2@test.com");
        HttpEntity<MultiValueMap<String, Object>> secondRequest =
                multipartRequest(secondToken, "valid.ris", VALID_RIS, "RIS");

        ResponseEntity<String> secondResponse = restTemplate.postForEntity(
                url("/api/references/import?format=RIS"), secondRequest, String.class);

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
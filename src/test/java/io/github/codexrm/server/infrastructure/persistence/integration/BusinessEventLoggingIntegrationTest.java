package io.github.codexrm.server.infrastructure.persistence.integration;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.codexrm.server.api.dto.request.LoginRequest;
import io.github.codexrm.server.api.dto.request.SignupRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class BusinessEventLoggingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private ListAppender<ILoggingEvent> logAppender;
    private Logger authControllerLogger;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void setUp() {
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();
        restTemplate.getRestTemplate().setRequestFactory(factory);

        authControllerLogger = (Logger) LoggerFactory.getLogger(
                "io.github.codexrm.server.api.controller.AuthController");

        logAppender = new ListAppender<>();
        logAppender.start();
        authControllerLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        authControllerLogger.detachAppender(logAppender);
    }

    @Test
    void shouldLogLoginSuccessEvent() {

        SignupRequest signup = new SignupRequest();
        signup.setUsername("eventLogUser");
        signup.setPassword("Test@123");
        signup.setEmail("eventlog@test.com");
        signup.setName("Event");
        signup.setLastName("Log");
        signup.setEnabled(true);

        restTemplate.postForEntity(url("/api/auth/signup"), signup, String.class);

        LoginRequest login = new LoginRequest();
        login.setUsername("eventLogUser");
        login.setPassword("Test@123");

        restTemplate.postForEntity(url("/api/auth/signin"), login, String.class);

        boolean foundEvent = logAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage()
                        .contains("event=auth.login.success username=eventLogUser"));

        assertThat(foundEvent).isTrue();
    }

    @Test
    void shouldLogLoginFailedEventWithoutPassword() {

        LoginRequest badLogin = new LoginRequest();
        badLogin.setUsername("neverExistedUser");
        badLogin.setPassword("SuperSecretPassword123");

        restTemplate.postForEntity(url("/api/auth/signin"), badLogin, String.class);

        boolean foundEvent = logAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage()
                        .contains("event=auth.login.failed username=neverExistedUser"));

        assertThat(foundEvent).isTrue();

        // The password itself must never appear in any log line from this request.
        boolean passwordLeaked = logAppender.list.stream()
                .anyMatch(event -> event.getFormattedMessage().contains("SuperSecretPassword123"));

        assertThat(passwordLeaked).isFalse();
    }
}
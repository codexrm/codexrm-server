package io.github.codexrm.server.infrastructure.exception;

import io.github.codexrm.server.api.dto.response.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/references/1");
    }

    @Test
    void resourceNotFoundMapsTo404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleResourceNotFound(new ResourceNotFoundException("Reference", "id", 1), request);

        assertStatus(response, HttpStatus.NOT_FOUND);
    }

    @Test
    void entityNotFoundMapsTo404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleEntityNotFound(new EntityNotFoundException("not found"), request);

        assertStatus(response, HttpStatus.NOT_FOUND);
    }

    @Test
    void duplicateResourceMapsTo409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicateResource(new DuplicateResourceException("User", "username", "bob"), request);

        assertStatus(response, HttpStatus.CONFLICT);
    }

    @Test
    void invalidOperationOwnershipViolationMapsTo403() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidOperation(
                        new InvalidOperationException("You do not have permission to access this resource"),
                        request);

        assertStatus(response, HttpStatus.FORBIDDEN);
    }

    @Test
    void illegalArgumentMapsTo400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgument(new IllegalArgumentException("bad input"), request);

        assertStatus(response, HttpStatus.BAD_REQUEST);
    }

    @Test
    void badRequestExceptionMapsTo400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBadRequest(new BadRequestException("Missing required fields for Reference"), request);

        assertStatus(response, HttpStatus.BAD_REQUEST);
    }

    @Test
    void tokenRefreshExceptionMapsTo403() {
        ResponseEntity<ErrorResponse> response =
                handler.handleTokenRefreshException(
                        new TokenRefreshException("token", "expired"), request);

        assertStatus(response, HttpStatus.FORBIDDEN);
    }

    @Test
    void accessDeniedMapsTo403() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(new AccessDeniedException("denied"), request);

        assertStatus(response, HttpStatus.FORBIDDEN);
    }

    @Test
    void badCredentialsMapsTo401() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBadCredentials(new BadCredentialsException("bad credentials"), request);

        assertStatus(response, HttpStatus.UNAUTHORIZED);
    }

    @Test
    void malformedJsonMapsTo400() {
        org.springframework.http.converter.HttpMessageNotReadableException ex =
                mock(org.springframework.http.converter.HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponse> response = handler.handleMalformedJson(ex, request);

        assertStatus(response, HttpStatus.BAD_REQUEST);
    }

    @Test
    void typeMismatchMapsTo400() {
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex =
                mock(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("page");

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex, request);

        assertStatus(response, HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("page");
    }

    @Test
    void genericExceptionMapsTo500() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(new RuntimeException("boom"), request);

        assertStatus(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void assertStatus(ResponseEntity<ErrorResponse> response, HttpStatus expected) {
        assertThat(response.getStatusCode()).isEqualTo(expected);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(expected.value());
        assertThat(response.getBody().getPath()).isEqualTo("/api/references/1");
    }
}

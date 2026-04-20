package io.github.codexrm.server.component.exception;

import io.github.codexrm.server.exception.DuplicateResourceException;
import io.github.codexrm.server.exception.GlobalExceptionHandler;
import io.github.codexrm.server.exception.ResourceNotFoundException;
import io.github.codexrm.server.payload.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void shouldHandleResourceNotFound() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("User", "name", request);

        ResponseEntity<ErrorResponse> response =
                handler.handleResourceNotFound(ex, request);

        assertEquals(404, response.getStatusCode().value());

        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("User"));
        assertTrue(response.getBody().getMessage().contains("not found"));

        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleDuplicateResource() {
        DuplicateResourceException ex =
                new DuplicateResourceException("Email", "email", request);

        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicateResource(ex, request);

        assertEquals(409, response.getStatusCode().value());

        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Email"));
        assertTrue(response.getBody().getMessage().contains("already exists"));
    }

    @Test
    void shouldHandleGenericException() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(ex, request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Unexpected error", response.getBody().getMessage());
    }

    @Test
    void shouldHandleValidationErrors() throws Exception {

        Object target = new Object();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "object");

        bindingResult.addError(
                new FieldError("object", "email", "must not be blank")
        );

        Method method = this.getClass().getDeclaredMethod("dummyMethod", String.class);

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(
                        new MethodParameter(method, 0),
                        bindingResult
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationErrors(ex, request);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody()
                .getMessage()
                .contains("email: must not be blank"));
    }

    private void dummyMethod(String value) {}
}
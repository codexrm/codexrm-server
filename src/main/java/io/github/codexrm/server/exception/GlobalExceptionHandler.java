package io.github.codexrm.server.exception;

import io.github.codexrm.server.payload.response.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Resource not found (custom)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex, request, HttpStatus.NOT_FOUND);
    }

    // 404 - JPA EntityNotFoundException
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex, request, HttpStatus.NOT_FOUND);
    }

    // 409 - Duplicate resource
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex, request, HttpStatus.CONFLICT);
    }

    // 400 - Invalid operation (custom)
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(
            InvalidOperationException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex, request, HttpStatus.BAD_REQUEST);
    }

    //  400 - Illegal arguments
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex, request, HttpStatus.BAD_REQUEST);
    }

    // 403 - Token refresh error
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorResponse> handleTokenRefreshException(
            TokenRefreshException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex, request, HttpStatus.FORBIDDEN);
    }

    // 403 - Access denied (Spring Security)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex, request, HttpStatus.FORBIDDEN);
    }

    // 400 - @Valid validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 500 - fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ===================== HELPER =====================

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception ex,
            HttpServletRequest request,
            HttpStatus status
    ) {
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, status);
    }
}
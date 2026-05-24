package io.github.codexrm.server.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Setter
@Getter
@Schema(description = "Standard API error response")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred", example = "2026-03-15T10:30:00Z")
    private final Instant timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private final int status;

    @Schema(description = "HTTP error reason", example = "Not Found")
    private final String error;

    @Schema(description = "Detailed error message", example = "User not found with id: 1")
    private final String message;

    @Schema(description = "Request path", example = "/api/users/1")
    private final String path;

    public ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }


}
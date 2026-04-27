package io.github.codexrm.server.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Request payload used for user authentication")
public class LoginRequest {

    @Schema(description = "Username used to authenticate", example = "marynes")
    @NotBlank
    private String username;

    @Schema(description = "User password", example = "password123")
    @NotBlank
    private String password;
}
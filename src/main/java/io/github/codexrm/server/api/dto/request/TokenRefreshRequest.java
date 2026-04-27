package io.github.codexrm.server.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Request payload used to refresh an expired JWT token")
public class TokenRefreshRequest {

    @Schema(
            description = "Refresh token previously issued during authentication",
            example = "c2f8a9f1-6d7c-4a6e-bc3b-2c2f3e9f6d1a")
    @NotBlank
    private String refreshToken;
}

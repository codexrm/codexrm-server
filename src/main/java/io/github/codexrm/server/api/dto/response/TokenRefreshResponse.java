package io.github.codexrm.server.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Schema(description = "Response returned when a JWT token is refreshed")
public class TokenRefreshResponse {

    @Schema(description = "New JWT access token generated from the refresh token")
    private String accessToken;

    @Schema(description = "Refresh token used to generate the new access token")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Expiration date of the new JWT token")
    private Date tokenExpirationDate;

    public TokenRefreshResponse(String accessToken, String refreshToken, Date tokenExpirationDate) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpirationDate = tokenExpirationDate;
    }
}

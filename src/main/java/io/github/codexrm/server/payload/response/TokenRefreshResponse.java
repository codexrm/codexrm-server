package io.github.codexrm.server.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

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

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Date getTokenExpirationDate() {
        return tokenExpirationDate;
    }

    public void setTokenExpirationDate(Date tokenExpirationDate) {
        this.tokenExpirationDate = tokenExpirationDate;
    }
}

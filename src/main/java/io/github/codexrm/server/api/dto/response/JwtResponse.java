package io.github.codexrm.server.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@Schema(description = "Authentication response containing JWT token and user information")
public class JwtResponse {

    @Schema(description = "JWT access token used for authenticated requests")
    private String token;

    @Schema(description = "Token type", example = "Bearer")
    private String type = "Bearer";

    @Schema(description = "Refresh token used to obtain a new JWT when it expires")
    private String refreshToken;

    @Schema(description = "Expiration date of the JWT token")
    private Date tokenExpirationDate;

    @Schema(description = "Expiration date of the refresh token")
    private Date refreshTokenExpirationDate;

    @Schema(description = "Unique identifier of the authenticated user", example = "1")
    private Integer id;

    @Schema(description = "Username of the authenticated user", example = "marynes")
    private String username;

    @Schema(description = "Email address of the authenticated user", example = "marynes@email.com")
    private String email;

    @Schema(description = "First name of the authenticated user", example = "Marynes")
    private String name;

    @Schema(description = "Last name of the authenticated user", example = "Diaz")
    private String lastName;

    @Schema(description = "Indicates if the user account is enabled", example = "true")
    private boolean enabled;

    @Schema(description = "Roles assigned to the authenticated user", example = "[\"ROLE_USER\"]")
    private List<String> roles;

    public JwtResponse(String accessToken, String refreshToken, Date tokenExpirationDate, Date refreshTokenExpirationDate, Integer id, String username, String email, String name, String lastName,
                       boolean enabled, List<String> roles) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpirationDate = tokenExpirationDate;
        this.refreshTokenExpirationDate = refreshTokenExpirationDate;
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.lastName = lastName;
        this.enabled = enabled;
        this.roles = roles;
    }
}

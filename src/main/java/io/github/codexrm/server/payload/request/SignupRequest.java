package io.github.codexrm.server.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload used to register a new user")
public class SignupRequest {

    @Schema(description = "Unique username chosen by the user", example = "marynes")
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @Schema(description = "User email address", example = "marynes@email.com")
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @Schema(description = "Password for the user account", example = "password123")
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @Schema(description = "User first name", example = "Marynes")
    @NotBlank
    @Size(max = 20)
    private String name;

    @Schema(description = "User last name", example = "Diaz")
    @NotBlank
    @Size(max = 20)
    private String lastName;

    @Schema(description = "Indicates if the user account is enabled", example = "true")
    private boolean enabled;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

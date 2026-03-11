package io.github.codexrm.server.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Request payload used to create a new user")
public class AddUserRequest {

    @Schema(description = "Unique username of the user", example = "marynes")
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @Schema(description = "User email address", example = "marynes@email.com")
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @Schema(description = "User password", example = "password123")
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

    @Schema(description = "List of roles assigned to the user", example = "[\"ROLE_USER\"]")
    private List<String> roles;

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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}

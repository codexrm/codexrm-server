package io.github.codexrm.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "User data transfer object")
public class UserDTO {

    @Schema(description = "Unique identifier of the user", example = "1")
    private Integer id;

    @Schema(description = "Username of the user", example = "marynes")
    private String username;

    @Schema(description = "First name of the user", example = "Marynes")
    private String name;

    @Schema(description = "Last name of the user", example = "Diaz")
    private String lastName;

    @Schema(description = "Email address of the user", example = "marynes@email.com")
    private String email;

    @Schema(description = "Indicates whether the user account is enabled", example = "true")
    private boolean enabled;

    @Schema(description = "List of roles assigned to the user", example = "[\"ROLE_USER\"]")
    private List<String> roles;


    public UserDTO() {}

    public UserDTO(Integer id, String username, String name, String lastName, String email, boolean enabled) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.enabled = enabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public void setRol(String rol) {
        this.roles.add(rol);
    }
}

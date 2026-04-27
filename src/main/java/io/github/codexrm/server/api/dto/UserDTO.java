package io.github.codexrm.server.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Schema(description = "User data transfer object")
public class UserDTO {

    @Schema(description = "Unique identifier of the user", example = "1")
    private Integer id;

    @Schema(description = "Username of the user", example = "marynes")
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @Schema(description = "First name of the user", example = "Marynes")
    @NotBlank
    @Size(max = 20)
    private String name;

    @Schema(description = "Last name of the user", example = "Diaz")
    @NotBlank
    @Size(max = 20)
    private String lastName;

    @Schema(description = "Email address of the user", example = "marynes@email.com")
    @NotBlank
    @Size(max = 50)
    @Email
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

    public void setRol(String rol) {
        this.roles.add(rol);
    }
}

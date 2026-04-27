package io.github.codexrm.server.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Request payload used to update the password of the authenticated user")
public class UpdateUserPasswordRequest {

    @Schema(description = "Current password of the user", example = "oldPassword123")
    @NotBlank
    @Size(min = 6, max = 40)
    private String currentPassword;

    @Schema(description = "New password to replace the current one", example = "newSecurePassword123")
    @NotBlank
    @Size(min = 6, max = 40)
    private String newPassword;

    @Schema(description = "Confirmation of the new password", example = "newSecurePassword123")
    @NotBlank
    @Size(min = 6, max = 40)
    private String confirmationPassword;
}
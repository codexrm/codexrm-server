package io.github.codexrm.server.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload used to update the password of the authenticated user")
public class UpdateUserPasswordRequest {

    @Schema(description = "Current password of the user", example = "oldPassword123")
    private String currentPassword;

    @Schema(description = "New password to replace the current one", example = "newSecurePassword123")
    private String newPassword;

    @Schema(description = "Confirmation of the new password", example = "newSecurePassword123")
    private String confirmationPassword;

    public UpdateUserPasswordRequest() {}

    public UpdateUserPasswordRequest(String newPassword, String currentPassword, String confirmationPassword) {
        this.newPassword = newPassword;
        this.currentPassword = currentPassword;
        this.confirmationPassword = confirmationPassword;
    }

    public String getNewPassword() {return newPassword;}

    public void setNewPassword(String newPassword) {this.newPassword = newPassword;}

    public String getCurrentPassword() {return currentPassword;}

    public void setCurrentPassword(String currentPassword) {this.currentPassword = currentPassword;}

    public String getConfirmationPassword() {return confirmationPassword;}

    public void setConfirmationPassword(String confirmationPassword) {this.confirmationPassword = confirmationPassword;}
}
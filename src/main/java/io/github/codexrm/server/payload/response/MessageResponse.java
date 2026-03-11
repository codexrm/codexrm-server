package io.github.codexrm.server.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generic message response returned by the API")
public class MessageResponse {

    @Schema(description = "Response message", example = "User registered successfully!")
    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

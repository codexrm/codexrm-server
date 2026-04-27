package io.github.codexrm.server.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Generic message response returned by the API")
public class MessageResponse {

    @Schema(description = "Response message", example = "User registered successfully!")
    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }
}

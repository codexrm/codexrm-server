package io.github.codexrm.server.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Web page reference information")
public class WebPageReferenceDTO extends ReferenceDTO {

    @Schema(description = "Author of the web page content", example = "Doe,John")
    @Pattern(regexp = "^[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+[;(?=[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+)[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+]*")
    private String author;

    @Schema(description = "URL of the web page", example = "https://example.com/article")
    @Pattern(regexp = "^https://.*")
    private String url;

    public WebPageReferenceDTO() {}

    public WebPageReferenceDTO(String title, String year, String month, String note, Integer id, String author, String url) {
        super(title, year, month, note, id);
        this.author = author;
        this.url = url;
    }

    public WebPageReferenceDTO(String title, String year, String month, String note, String author, String url) {
        super(title, year, month, note);
        this.author = author;
        this.url = url;
    }
}

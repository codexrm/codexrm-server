package io.github.codexrm.server.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Thesis reference information")
public class ThesisReferenceDTO extends ReferenceDTO {

    @Schema(description = "Author of the thesis", example = "Gonzalez,Maria")
    @NotBlank
    @Pattern(regexp = "^[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+[;(?=[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+)[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+]*")
    private String author;

    @Schema(description = "University or institution where the thesis was submitted", example = "University of Havana")
    @NotBlank
    private String school;

    @Schema(description = "Type of thesis", example = "PhD Thesis")
    @Pattern(regexp = "^(MASTERS|PHD)$")
    private String type;

    @Schema(description = "Location of the university or institution", example = "La Habana, Cuba")
    @Pattern(regexp ="^$|^[A-ZÁÉÍÓÚÜÑ][A-ZÁÉÍÓÚÜÑa-záéíóúüñ\\s]*[A-ZÁÉÍÓÚÜÑa-záéíóúüñ]+,\\s[[A-Za-záéíóúüñÁÉÍÓÚÜÑ]+]*")
    private String address;

    public ThesisReferenceDTO() {}

    public ThesisReferenceDTO(String title, String year, String month, String note, Integer id, String author, String school, String type, String address) {
        super(title, year, month, note, id);
        this.author = author;
        this.school = school;
        this.type = type;
        this.address = address;
    }

    public ThesisReferenceDTO(String title, String year, String month, String note, String author, String school, String type, String address) {
        super(title, year, month, note);
        this.author = author;
        this.school = school;
        this.type = type;
        this.address = address;
    }
}

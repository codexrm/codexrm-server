package io.github.codexrm.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

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
    @Pattern(regexp = "^[A-ZÁÉÍÓÚÜÑ][A-ZÁÉÍÓÚÜÑa-záéíóúüñ\\s]*[A-ZÁÉÍÓÚÜÑa-záéíóúüñ]+,\\s[[A-Za-záéíóúüñÁÉÍÓÚÜÑ]+]*")
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

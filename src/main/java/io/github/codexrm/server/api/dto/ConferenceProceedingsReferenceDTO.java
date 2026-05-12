package io.github.codexrm.server.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Conference proceedings reference information")
public class ConferenceProceedingsReferenceDTO extends ReferenceDTO {

    @Schema(description = "Editor of the conference proceedings", example = "Smith,John")
    @Pattern(regexp = "^[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+[;(?=[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+)[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+]*")
    private String editor;

    @Schema(description = "Volume of the proceedings", example = "10")
    @Pattern(regexp = "^$|[\\d]*")
    private String volume;

    @Schema(description = "Issue number of the proceedings", example = "2")
    @Pattern(regexp = "[A-ZÁÉÍÓÚÜÑa-záéíóúüñ0-9\\s-]+")
    private String number;

    @Schema(description = "Series of the conference proceedings", example = "Lecture Notes in Computer Science")
    @Pattern(regexp = "[A-ZÁÉÍÓÚÜÑa-záéíóúüñ\\s]+")
    private String series;

    @Schema(description = "Location where the conference took place", example = "Paris, Francia")
    @Pattern(regexp =   "^$|^[A-ZÁÉÍÓÚÜÑ][A-ZÁÉÍÓÚÜÑa-záéíóúüñ\\s]*[A-ZÁÉÍÓÚÜÑa-záéíóúüñ]+,\\s[[A-Za-záéíóúüñÁÉÍÓÚÜÑ]+]*")
    private String address;

    @Schema(description = "Publisher of the conference proceedings", example = "Springer")
    private String publisher;

    @Schema(description = "Organization responsible for the conference", example = "ACM")
    private String organization;

    @Schema(description = "ISBN identifier of the proceedings", example = "978-3-16-148410-0")
    @Pattern(regexp = "^(?=(?:\\D*\\d){10}(?:(?:\\D*\\d){3})?$)[\\d-]+$")
    private String isbn;

    public ConferenceProceedingsReferenceDTO() {}

    public ConferenceProceedingsReferenceDTO(String title, String year, String month, String note, Integer id, String editor, String volume, String number, String series, String address, String publisher, String organization, String isbn) {
        super(title, year, month, note, id);
        this.editor = editor;
        this.volume = volume;
        this.number = number;
        this.series = series;
        this.address = address;
        this.publisher = publisher;
        this.organization = organization;
        this.isbn = isbn;
    }

    public ConferenceProceedingsReferenceDTO(String title, String year, String month, String note, String editor, String volume, String number, String series, String address, String publisher, String organization, String isbn) {
        super(title, year, month, note);
        this.editor = editor;
        this.volume = volume;
        this.number = number;
        this.series = series;
        this.address = address;
        this.publisher = publisher;
        this.organization = organization;
        this.isbn = isbn;
    }
}

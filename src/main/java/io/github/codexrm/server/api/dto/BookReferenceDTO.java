package io.github.codexrm.server.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Book reference information")
public class BookReferenceDTO extends ReferenceDTO {

    @Schema(description = "Author of the book", example = "Martin,Robert")
    @NotBlank
    @Pattern(regexp = "^[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+[;(?=[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+)[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+]*")
    protected String author;

    @Schema(description = "Editor of the book", example = "Smith,John")
    @NotBlank
    @Pattern(regexp = "^[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+[;(?=[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+)[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+]*")
    protected String editor;

    @Schema(description = "Publisher of the book", example = "Prentice Hall")
    @NotBlank
    protected String publisher;

    @Schema(description = "Volume of the book", example = "1")
    @Pattern(regexp = "^$|[\\d]*")
    protected String volume;

    @Schema(description = "Series to which the book belongs", example = "Computer Science Series")
    @Pattern(regexp = "[A-ZÁÉÍÓÚÜÑa-záéíóúüñ\\s]+")
    protected String series;

    @Schema(description = "Book number within the series", example = "12")
    @Pattern(regexp = "[A-ZÁÉÍÓÚÜÑa-záéíóúüñ0-9\\s-]+")
    protected String number;

    @Schema(description = "Publication address or location", example = "São Paulo, Brasil")
    @Pattern(regexp = "^$|^[A-ZÁÉÍÓÚÜÑ][A-ZÁÉÍÓÚÜÑa-záéíóúüñ\\s]*[A-ZÁÉÍÓÚÜÑa-záéíóúüñ]+,\\s[[A-Za-záéíóúüñÁÉÍÓÚÜÑ]+]*")
    protected String address;

    @Schema(description = "Edition of the book", example = "2.")
    @Pattern(regexp = "[A-ZÁÉÍÓÚÜÑa-záéíóúüñ]+|\\d+\\.")
    protected String edition;

    @Schema(description = "ISBN identifier of the book", example = "123-456-789-0")
    @Pattern(regexp = "^(?=(?:\\D*\\d){10}(?:(?:\\D*\\d){3})?$)[\\d-]+$")
    protected String isbn;

    public BookReferenceDTO() {}

    public BookReferenceDTO(String title, String year, String month, String note, Integer id, String author, String editor, String publisher, String volume, String series, String number, String address, String edition, String isbn) {
        super(title, year, month, note, id);
        this.author = author;
        this.editor = editor;
        this.publisher = publisher;
        this.volume = volume;
        this.series = series;
        this.number = number;
        this.address = address;
        this.edition = edition;
        this.isbn = isbn;
    }

    public BookReferenceDTO(String title, String year, String month, String note, String author, String editor, String publisher, String volume, String series, String number, String address, String edition, String isbn) {
        super(title, year, month, note);
        this.author = author;
        this.editor = editor;
        this.publisher = publisher;
        this.volume = volume;
        this.series = series;
        this.number = number;
        this.address = address;
        this.edition = edition;
        this.isbn = isbn;
    }
}

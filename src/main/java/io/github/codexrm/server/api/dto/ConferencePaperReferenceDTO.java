package io.github.codexrm.server.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Conference paper reference information")
public class ConferencePaperReferenceDTO extends ReferenceDTO {

    @Schema(description = "Author of the conference paper", example = "Navarro,Andrew")
    @NotBlank
    @Pattern(regexp = "^[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+[;(?=[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+)[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+]*")
    private String author;

    @Schema(description = "Title of the book or proceedings where the paper appears", example = "Proceedings of the International Conference on Machine Learning")
    @NotBlank
    private String bookTitle;

    @Schema(description = "Editor of the conference proceedings", example = "Smith,John")
    @Pattern(regexp = "^[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+[;(?=[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+)[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+]*")
    private String editor;

    @Schema(description = "Volume of the proceedings", example = "15")
    @Pattern(regexp = "^$|[\\d]*")
    private String volume;

    @Schema(description = "Issue number of the proceedings", example = "2")
    @Pattern(regexp = "[A-ZÁÉÍÓÚÜÑa-záéíóúüñ0-9\\s-]+")
    private String number;

    @Schema(description = "Series of the conference proceedings", example = "Lecture Notes in Computer Science")
    @Pattern(regexp = "[A-ZÁÉÍÓÚÜÑa-záéíóúüñ\\s]+")
    private String series;

    @Schema(description = "Page range of the paper", example = "210-225")
    @Pattern(regexp = "[IVXMLCD]+|[IVXMLCD]+,[IVXMLCD]+|[IVXMLCD]+-[IVXMLCD]+|[0-9]+|[0-9]+,[0-9]+|[0-9]+-[0-9]+")
    private String pages;

    @Schema(description = "Location where the conference took place", example = "Berlin, Alemania")
    @Pattern(regexp = "^[A-ZÁÉÍÓÚÜÑ][A-ZÁÉÍÓÚÜÑa-záéíóúüñ\\s]*[A-ZÁÉÍÓÚÜÑa-záéíóúüñ]+,\\s[[A-Za-záéíóúüñÁÉÍÓÚÜÑ]+]*")
    private String address;

    @Schema(description = "Organization responsible for the conference", example = "IEEE")
    private String organization;

    @Schema(description = "Publisher of the proceedings", example = "Springer")
    private String publisher;

    public ConferencePaperReferenceDTO() {}

    public ConferencePaperReferenceDTO(String title, String year, String month, String note, Integer id, String author, String bookTitle, String editor, String volume, String number, String series, String pages, String address, String organization, String publisher) {
        super(title, year, month, note, id);
        this.author = author;
        this.bookTitle = bookTitle;
        this.editor = editor;
        this.volume = volume;
        this.number = number;
        this.series = series;
        this.pages = pages;
        this.address = address;
        this.organization = organization;
        this.publisher = publisher;
    }

    public ConferencePaperReferenceDTO(String title, String year, String month, String note, String author, String bookTitle, String editor, String volume, String number, String series, String pages, String address, String organization, String publisher) {
        super(title, year, month, note);
        this.author = author;
        this.bookTitle = bookTitle;
        this.editor = editor;
        this.volume = volume;
        this.number = number;
        this.series = series;
        this.pages = pages;
        this.address = address;
        this.organization = organization;
        this.publisher = publisher;
    }
}

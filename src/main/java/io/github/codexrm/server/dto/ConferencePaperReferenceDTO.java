package io.github.codexrm.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }
}

package io.github.codexrm.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Conference proceedings reference information")
public class ConferenceProceedingsReferenceDTO extends ReferenceDTO {

    @Schema(description = "Editor of the conference proceedings", example = "John Smith")
    private String editor;

    @Schema(description = "Volume of the proceedings", example = "10")
    private String volume;

    @Schema(description = "Issue number of the proceedings", example = "2")
    private String number;

    @Schema(description = "Series of the conference proceedings", example = "Lecture Notes in Computer Science")
    private String series;

    @Schema(description = "Location where the conference took place", example = "Paris")
    private String address;

    @Schema(description = "Publisher of the conference proceedings", example = "Springer")
    private String publisher;

    @Schema(description = "Organization responsible for the conference", example = "ACM")
    private String organization;

    @Schema(description = "ISBN identifier of the proceedings", example = "978-3-16-148410-0")
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

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

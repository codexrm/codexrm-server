package io.github.codexrm.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Book reference information")
public class BookReferenceDTO extends ReferenceDTO {

    @Schema(description = "Author of the book", example = "Martin,Robert")
    protected String author;

    @Schema(description = "Editor of the book", example = "Smith,John")
    protected String editor;

    @Schema(description = "Publisher of the book", example = "Prentice Hall")
    protected String publisher;

    @Schema(description = "Volume of the book", example = "1")
    protected String volume;

    @Schema(description = "Series to which the book belongs", example = "Computer Science Series")
    protected String series;

    @Schema(description = "Book number within the series", example = "12")
    protected String number;

    @Schema(description = "Publication address or location", example = "São Paulo, Brasil")
    protected String address;

    @Schema(description = "Edition of the book", example = "2.")
    protected String edition;

    @Schema(description = "ISBN identifier of the book", example = "123-456-789-0")
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
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

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }
}

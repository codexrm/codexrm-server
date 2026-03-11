package io.github.codexrm.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Booklet reference information")
public class BookLetReferenceDTO extends ReferenceDTO {

    @Schema(description = "Author of the booklet", example = "John Smith")
    private String author;

    @Schema(description = "How the booklet was published", example = "Online publication")
    private String howpublished;

    @Schema(description = "Publication address or location", example = "New York")
    private String address;

    public BookLetReferenceDTO() {}

    public BookLetReferenceDTO(String title, String year, String month, String note, Integer id, String author, String howpublished, String address) {
        super(title, year, month, note, id);
        this.author = author;
        this.howpublished = howpublished;
        this.address = address;
    }

    public BookLetReferenceDTO(String title, String year, String month, String note, String author, String howpublished, String address) {
        super(title, year, month, note);
        this.author = author;
        this.howpublished = howpublished;
        this.address = address;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getHowpublished() {
        return howpublished;
    }

    public void setHowpublished(String howpublished) {
        this.howpublished = howpublished;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

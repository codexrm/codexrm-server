package io.github.codexrm.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Book section reference information")
public class BookSectionReferenceDTO extends BookReferenceDTO {

    @Schema(description = "Chapter of the book where the section appears", example = "5")
    private String chapter;

    @Schema(description = "Page range of the section", example = "120-135")
    private String pages;

    @Schema(description = "Type of section (e.g., chapter, appendix)", example = "chapter")
    private String type;

    public BookSectionReferenceDTO() {}

    public BookSectionReferenceDTO(String title, String year, String month, String note, Integer id, String author, String editor, String publisher, String volume, String series, String number, String address, String edition, String isbn, String chapter, String pages, String type) {
        super(title, year, month, note, id, author, editor, publisher, volume, series, number, address, edition, isbn);
        this.chapter = chapter;
        this.pages = pages;
        this.type = type;
    }

    public BookSectionReferenceDTO(String title, String year, String month, String note, String author, String editor, String publisher, String volume, String series, String number, String address, String edition, String isbn, String chapter, String pages, String type) {
        super(title, year, month, note, author, editor, publisher, volume, series, number, address, edition, isbn);
        this.chapter = chapter;
        this.pages = pages;
        this.type = type;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

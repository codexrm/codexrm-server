package io.github.codexrm.server.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Book section reference information")
public class BookSectionReferenceDTO extends BookReferenceDTO {

    @Schema(description = "Chapter of the book where the section appears", example = "5")
    @NotBlank
    @Pattern(regexp = "^$|[\\d]*")
    private String chapter;

    @Schema(description = "Page range of the section", example = "120-135")
    @NotBlank
    @Pattern(regexp = "[IVXMLCD]+|[IVXMLCD]+,[IVXMLCD]+|[IVXMLCD]+-[IVXMLCD]+|[0-9]+|[0-9]+,[0-9]+|[0-9]+-[0-9]+")
    private String pages;

    @Schema(description = "Type of section", example = "PHDTHESIS")
    @Pattern(regexp = "^(?i)(MATHESIS|PHDTHESIS|CANDTHESIS|TECHREPORT|RESREPORT|SOFTWARE|AUDIOCD|DataCD)$",
            message = "Invalid type")
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
}

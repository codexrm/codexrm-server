package io.github.codexrm.server.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Article reference information")
public class ArticleReferenceDTO extends ReferenceDTO {

    @Schema(description = "Author of the article", example = "Garcia,Juan")
    @NotBlank
    @Pattern(regexp = "^[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+[;(?=[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+)[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+,[A-ZÁÉÍÓÚÜÑ][a-záéíóúüñ]+]*")
    private String author;

    @Schema(description = "Journal where the article was published", example = "Nature")
    @NotBlank
    private String journal;

    @Schema(description = "Journal volume", example = "12")
    @Pattern(regexp = "^$|[\\d]*")
    private String volume;

    @Schema(description = "Journal issue number", example = "3")
    @Pattern(regexp = "[A-ZÁÉÍÓÚÜÑa-záéíóúüñ0-9\\s-]+")
    private String number;

    @Schema(description = "Page range of the article", example = "120-135")
    @Pattern(regexp = "[IVXMLCD]+|[IVXMLCD]+,[IVXMLCD]+|[IVXMLCD]+-[IVXMLCD]+|[0-9]+|[0-9]+,[0-9]+|[0-9]+-[0-9]+")
    private String pages;

    @Schema(description = "ISSN of the journal", example = "1234-5678")
    @Pattern(regexp = "\\d{4}-\\d{4}|\\d{4}-\\d{3}X")
    private String issn;

    public ArticleReferenceDTO() {}

    public ArticleReferenceDTO(String title, String year, String month, String note, Integer id, String author, String journal, String volume, String number, String pages, String issn) {
        super(title, year, month, note, id);
        this.author = author;
        this.journal = journal;
        this.volume = volume;
        this.number = number;
        this.pages = pages;
        this.issn = issn;
    }

    public ArticleReferenceDTO(String title, String year, String month, String note, String author, String journal, String volume, String number, String pages, String issn) {
        super(title, year, month, note);
        this.author = author;
        this.journal = journal;
        this.volume = volume;
        this.number = number;
        this.pages = pages;
        this.issn = issn;
    }
}

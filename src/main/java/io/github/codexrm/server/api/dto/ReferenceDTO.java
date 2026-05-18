package io.github.codexrm.server.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "referenceType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ArticleReferenceDTO.class, name = "ArticleReferenceDTO"),
        @JsonSubTypes.Type(value = BookReferenceDTO.class, name = "BookReferenceDTO"),
        @JsonSubTypes.Type(value = BookSectionReferenceDTO.class, name = "BookSectionReferenceDTO"),
        @JsonSubTypes.Type(value = ThesisReferenceDTO.class, name = "ThesisReferenceDTO"),
        @JsonSubTypes.Type(value = BookLetReferenceDTO.class, name = "BookLetReferenceDTO"),
        @JsonSubTypes.Type(value = ConferencePaperReferenceDTO.class, name = "ConferencePaperReferenceDTO"),
        @JsonSubTypes.Type(value = WebPageReferenceDTO.class, name = "WebPageReferenceDTO"),
        @JsonSubTypes.Type(value = ConferenceProceedingsReferenceDTO.class, name = "ConferenceProceedingsReferenceDTO")})
@Schema(
        description = "Base reference DTO used for different types of bibliographic references",
        discriminatorProperty = "referenceType")
public class ReferenceDTO {

    @Schema(description = "Title of the reference", example = "Deep Learning")
    protected String title;

    @Schema(description = "Year of publication", example = "2016 or 2020--2021")
    @Pattern(regexp = "\\d{4}|\\d{4}--\\d{4}")
    protected String year;

    @Schema(description = "Month of publication", example = "jan")
    @Pattern(regexp = "^(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)$")
    protected String month;

    @Schema(description = "Additional notes about the reference", example = "Second edition")
    protected String note;

    @Schema(description = "Unique identifier of the reference", example = "1")
    protected Integer id;
    public ReferenceDTO() {}

    public ReferenceDTO(String title, String year, String month, String note, Integer id) {
        this.title = title;
        this.year = year;
        this.month = month;
        this.note = note;
        this.id = id;
    }

    public ReferenceDTO(String title, String year, String month, String note) {
        this.title = title;
        this.year = year;
        this.month = month;
        this.note = note;
    }
}

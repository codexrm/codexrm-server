package io.github.codexrm.server.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

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

    @Schema(description = "Year of publication", example = "2016")
    protected String year;

    @Schema(description = "Month of publication", example = "July")
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}

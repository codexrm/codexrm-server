package io.github.codexrm.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Web page reference information")
public class WebPageReferenceDTO extends ReferenceDTO {

    @Schema(description = "Author of the web page content", example = "John Doe")
    private String author;

    @Schema(description = "URL of the web page", example = "https://example.com/article")
    private String url;

    public WebPageReferenceDTO() {}

    public WebPageReferenceDTO(String title, String year, String month, String note, Integer id, String author, String url) {
        super(title, year, month, note, id);
        this.author = author;
        this.url = url;
    }

    public WebPageReferenceDTO(String title, String year, String month, String note, String author, String url) {
        super(title, year, month, note);
        this.author = author;
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

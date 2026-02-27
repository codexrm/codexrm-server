package io.github.codexrm.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "webpagereference")
public class WebPageReference extends Reference {

    @Column(name = "author")
    private String author;

    @Column(name = "url")
    private String url;

    public WebPageReference() {}

    public WebPageReference(String title, String year, String month, String note, User user, String author, String url) {
        super(title, year, month, note, user);
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

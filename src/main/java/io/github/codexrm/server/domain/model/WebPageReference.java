package io.github.codexrm.server.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "webpagereference")
@Setter
@Getter
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
}

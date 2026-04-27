package io.github.codexrm.server.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "articlereference")
@Setter
@Getter
public class ArticleReference extends Reference {

    @Column(name = "author")
    private String author;

    @Column(name = "journal")
    private String journal;

    @Column(name = "volume")
    private String volume;

    @Column(name = "numbera")
    private String number;

    @Column(name = "pages")
    private String pages;

    @Column(name = "issn")
    private String issn;

    public ArticleReference() {}

    public ArticleReference(String title, String year, String month, String note, User user, String author, String journal, String volume, String number, String pages, String issn) {
        super(title, year, month, note, user);
        this.author = author;
        this.journal = journal;
        this.volume = volume;
        this.number = number;
        this.pages = pages;
        this.issn = issn;
    }
}
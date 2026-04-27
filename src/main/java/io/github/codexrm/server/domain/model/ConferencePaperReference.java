package io.github.codexrm.server.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "conferencepaperreference")
@Setter
@Getter
public class ConferencePaperReference extends Reference {

    @Column(name = "author")
    private String author;

    @Column(name = "booktitle")
    private String bookTitle;

    @Column(name = "editor")
    private String editor;

    @Column(name = "volume")
    private String volume;

    @Column(name = "numbera")
    private String number;

    @Column(name = "series")
    private String series;

    @Column(name = "pages")
    private String pages;

    @Column(name = "address")
    private String address;

    @Column(name = "organization")
    private String organization;

    @Column(name = "publisher")
    private String publisher;

    public ConferencePaperReference() {}

    public ConferencePaperReference(String title, String year, String month, String note, User user, String author, String bookTitle, String editor, String number, String series, String publisher, String volume, String address, String pages, String organization) {
        super(title, year, month, note, user);
        this.author = author;
        this.bookTitle = bookTitle;
        this.editor = editor;
        this.number = number;
        this.series = series;
        this.publisher = publisher;
        this.volume = volume;
        this.address = address;
        this.pages = pages;
        this.organization = organization;
    }
}

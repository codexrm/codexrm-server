package io.github.codexrm.server.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bookreference")
@Inheritance(strategy = InheritanceType.JOINED)
@Setter
@Getter
public class BookReference extends Reference {

    @Column(name = "author")
    private String author;

    @Column(name = "editor")
    private String editor;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "volume")
    private String volume;

    @Column(name = "numbera")
    private String number;

    @Column(name = "series")
    private String series;

    @Column(name = "address")
    private String address;

    @Column(name = "edition")
    private String edition;

    @Column(name = "isbn")
    private String isbn;

    public BookReference() {}

    public BookReference(String title, String year, String month, String note, User user, String author, String editor, String publisher, String volume, String number, String series, String address, String edition, String isbn) {
        super(title, year, month, note, user);
        this.author = author;
        this.editor = editor;
        this.publisher = publisher;
        this.volume = volume;
        this.number = number;
        this.series = series;
        this.address = address;
        this.edition = edition;
        this.isbn = isbn;
    }
}
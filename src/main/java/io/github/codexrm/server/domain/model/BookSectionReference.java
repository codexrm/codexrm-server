package io.github.codexrm.server.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "booksectionreference")
@Setter
@Getter
public class BookSectionReference extends BookReference {

    @Column(name = "chapter")
    private String chapter;

    @Column(name = "pages")
    private String pages;

    @Column(name = "type")
    private String type;

    public BookSectionReference() {}

    public BookSectionReference(String title, String year, String month, String note, User user, String author, String editor, String publisher, String volume, String number, String series, String address, String edition, String isbn,
                                String chapter, String pages, String type) {
        super(title, year, month, note, user, author, editor, publisher, volume, number, series, address, edition, isbn);
        this.chapter = chapter;
        this.pages = pages;
        this.type = type;
    }
}
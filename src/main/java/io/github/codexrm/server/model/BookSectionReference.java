package io.github.codexrm.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "booksectionreference")
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }
}
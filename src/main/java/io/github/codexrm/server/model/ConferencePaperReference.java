package io.github.codexrm.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "conferencepaperreference")
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

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}

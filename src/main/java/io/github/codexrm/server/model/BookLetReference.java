package io.github.codexrm.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "bookletreference")
public class BookLetReference extends Reference {

    @Column(name = "author")
    private String author;

    @Column(name = "howpublished")
    private String howpublished;

    @Column(name = "address")
    private String address;


    public BookLetReference() {}

    public BookLetReference(String title, String year, String month, String note, User user, String author, String howpublished, String address) {
        super(title, year, month, note, user);
        this.author = author;
        this.howpublished = howpublished;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHowpublished() {
        return howpublished;
    }

    public void setHowpublished(String howpublished) {
        this.howpublished = howpublished;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
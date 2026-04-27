package io.github.codexrm.server.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bookletreference")
@Setter
@Getter
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
}
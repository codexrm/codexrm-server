package io.github.codexrm.server.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "conferenceproceedingsreference")
@Setter
@Getter
public class ConferenceProceedingReference extends Reference {

    @Column(name = "editor")
    private String editor;

    @Column(name = "volume")
    private String volume;

    @Column(name = "numbera")
    private String number;

    @Column(name = "series")
    private String series;

    @Column(name = "address")
    private String address;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "organization")
    private String organization;

    @Column(name = "isbn")
    private String isbn;

    public ConferenceProceedingReference() {}

    public ConferenceProceedingReference(String title, String year, String month, String note, User user, String editor, String volume, String number, String series, String address, String publisher, String isbn, String organization) {
        super(title, year, month, note, user);
        this.editor = editor;
        this.volume = volume;
        this.number = number;
        this.series = series;
        this.address = address;
        this.publisher = publisher;
        this.isbn = isbn;
        this.organization = organization;
    }
}
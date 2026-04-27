package io.github.codexrm.server.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "thesisreference")
@Setter
@Getter
public class ThesisReference extends Reference {

    @Column(name = "author")
    private String author;

    @Column(name = "school")
    private String school;

    @Column(name = "type")
    private String type;

    @Column(name = "address")
    private String address;

    public ThesisReference() {}

    public ThesisReference(String title, String year, String month, String note, User user, String author, String school, String type, String address) {
        super(title, year, month, note, user);
        this.author = author;
        this.school = school;
        this.type = type;
        this.address = address;
    }
}
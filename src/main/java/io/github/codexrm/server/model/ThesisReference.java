package io.github.codexrm.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "thesisreference")
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
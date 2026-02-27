package io.github.codexrm.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reference")
@Inheritance(strategy = InheritanceType.JOINED)
public class Reference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "title")
    private String title;


    @Column(name = "year")
    private String year;

    @Column(name = "month")
    private String month;

    @Column(name = "note")
    private String note;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usercodex")
    private User user;

    public Reference() {}

    public Reference(String title, String year, String month, String note, User user) {
        this.title = title;
        this.year = year;
        this.month = month;
        this.note = note;
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
package io.github.codexrm.server.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reference")
@Inheritance(strategy = InheritanceType.JOINED)
@Setter
@Getter
public class Reference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "title")
    private String title;


    @Column(name = "ref_year")
    private String year;

    @Column(name = "ref_month")
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
}
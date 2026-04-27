package io.github.codexrm.server.domain.model;

import io.github.codexrm.server.domain.enums.ERole;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import jakarta.persistence.*;

@Entity
@Table(name = "rol")
@Setter
@Getter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, length = 20)
    @NaturalId
    private ERole name;

    public Role() {}

    public Role(ERole name) {
        this.name = name;
    }
}

package io.github.codexrm.server.infrastructure.persistence.repository;

import io.github.codexrm.server.domain.enums.ERole;
import io.github.codexrm.server.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(ERole name);
}

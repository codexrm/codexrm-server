package io.github.codexrm.server.repository;

import io.github.codexrm.server.model.Reference;
import io.github.codexrm.server.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferenceRepository extends JpaRepository<Reference, Integer> {

        Page<Reference> findByUser(User user, Pageable pageable);

        Page<Reference> findByUserAndYearContaining(User user, String year, Pageable pageable);

        Page<Reference> findByUserAndTitleContaining(User user, String title, Pageable pageable);

        Page<Reference> findByUserAndTitleContainingIgnoreCase(User user, String title, Pageable pageable);

        Page<Reference> findByYearContaining(String year, Pageable pageable);

        Page<Reference> findByTitleContaining(String title, Pageable pageable);

        Page<Reference> findByUserAndYearContainingAndTitleContaining(User user, String year, String title, Pageable pageable);

        Page<Reference> findByYearContainingAndTitleContaining(String year, String title, Pageable pageable);
}
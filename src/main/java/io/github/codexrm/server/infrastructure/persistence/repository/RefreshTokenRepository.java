package io.github.codexrm.server.infrastructure.persistence.repository;

import java.util.Optional;

import io.github.codexrm.server.domain.model.RefreshToken;
import io.github.codexrm.server.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

  Optional<RefreshToken> findByToken(String token);

  Optional<RefreshToken> findByUser(User user);

  @Modifying
  int deleteByUser(User user);

}
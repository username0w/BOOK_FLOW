package com.clover.bookflow.domain.auth.token.repository;

import com.clover.bookflow.domain.auth.token.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByJti(String jti);

  void deleteByJti(String jti);


}

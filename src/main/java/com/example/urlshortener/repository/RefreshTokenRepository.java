package com.example.urlshortener.repository;

import com.example.urlshortener.entity.RefreshToken;
import com.example.urlshortener.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}

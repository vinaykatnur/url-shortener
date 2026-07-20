package com.example.urlshortener.service.impl;

import com.example.urlshortener.entity.RefreshToken;
import com.example.urlshortener.entity.User;
import com.example.urlshortener.exception.ResourceNotFoundException;
import com.example.urlshortener.repository.RefreshTokenRepository;
import com.example.urlshortener.security.JwtTokenProvider;
import com.example.urlshortener.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(jwtTokenProvider.generateRefreshToken())
                .expiresAt(jwtTokenProvider.getRefreshTokenExpiryDate().toInstant())
                .user(user)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(refreshToken -> refreshToken.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token is invalid or expired"));
    }

    @Override
    @Transactional
    public RefreshToken refreshToken(RefreshToken token) {
        token.setToken(jwtTokenProvider.generateRefreshToken());
        token.setExpiresAt(jwtTokenProvider.getRefreshTokenExpiryDate().toInstant());
        return refreshTokenRepository.save(token);
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }
}

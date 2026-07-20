package com.example.urlshortener.service;

import com.example.urlshortener.entity.RefreshToken;
import com.example.urlshortener.entity.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    RefreshToken verifyRefreshToken(String token);
    RefreshToken refreshToken(RefreshToken token);
    void deleteByToken(String token);
}

package com.example.urlshortener.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final Key jwtSecret;
    private final long jwtAccessTokenExpirationMs;
    private final long jwtRefreshTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.access-token-expiration-ms}") long jwtAccessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms}") long jwtRefreshTokenExpirationMs) {
        this.jwtSecret = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtAccessTokenExpirationMs = jwtAccessTokenExpirationMs;
        this.jwtRefreshTokenExpirationMs = jwtRefreshTokenExpirationMs;
    }

    public String generateAccessToken(UserPrincipal userPrincipal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(jwtAccessTokenExpirationMs)))
                .claim("email", userPrincipal.getUsername())
                .claim("roles", userPrincipal.getAuthorities())
                .setId(UUID.randomUUID().toString())
                .signWith(jwtSecret)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(token)
                .getBody().get("email", String.class);
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public Date getRefreshTokenExpiryDate() {
        return Date.from(Instant.now().plusMillis(jwtRefreshTokenExpirationMs));
    }
}

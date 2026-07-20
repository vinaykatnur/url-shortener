package com.example.urlshortener.service.impl;

import com.example.urlshortener.dto.AuthResponse;
import com.example.urlshortener.dto.LoginRequest;
import com.example.urlshortener.dto.RefreshTokenRequest;
import com.example.urlshortener.dto.RegisterRequest;
import com.example.urlshortener.dto.UserResponse;
import com.example.urlshortener.entity.RefreshToken;
import com.example.urlshortener.entity.Role;
import com.example.urlshortener.entity.User;
import com.example.urlshortener.exception.DuplicateResourceException;
import com.example.urlshortener.exception.ResourceNotFoundException;
import com.example.urlshortener.exception.UnauthorizedException;
import com.example.urlshortener.repository.RoleRepository;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.security.JwtTokenProvider;
import com.example.urlshortener.security.UserPrincipal;
import com.example.urlshortener.service.AdminService;
import com.example.urlshortener.service.AuthService;
import com.example.urlshortener.service.AuditService;
import com.example.urlshortener.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_USER not found"));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(new HashSet<>(Collections.singletonList(userRole)))
                .build();
        userRepository.save(user);

        auditService.logEvent("REGISTER", user.getEmail(), "USER", user.getId(), "Registered user");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (DisabledException ex) {
            auditService.logEvent("LOGIN_FAILED", request.getEmail(), "USER", null, "Failed login attempt: Account is disabled");
            throw new UnauthorizedException("User account is disabled");
        } catch (BadCredentialsException ex) {
            boolean userExists = userRepository.existsByEmail(request.getEmail());
            if (!userExists) {
                auditService.logEvent("LOGIN_FAILED", request.getEmail(), "USER", null, "Failed login attempt: User not found");
            } else {
                auditService.logEvent("LOGIN_FAILED", request.getEmail(), "USER", null, "Failed login attempt: Bad credentials");
            }
            throw new UnauthorizedException("Invalid credentials");
        } catch (AuthenticationException ex) {
            auditService.logEvent("LOGIN_FAILED", request.getEmail(), "USER", null, "Failed login attempt: " + ex.getMessage());
            throw new UnauthorizedException("Authentication failed");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with provided email"));

        auditService.logEvent("LOGIN", user.getEmail(), "USER", user.getId(), "User logged in");

        UserPrincipal principal = UserPrincipal.fromUser(user);
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();
        UserPrincipal principal = UserPrincipal.fromUser(user);

        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String refreshTokenValue = refreshTokenService.refreshToken(refreshToken).getToken();

        auditService.logEvent("TOKEN_REFRESH", user.getEmail(), "USER", user.getId(), "Token refreshed");

        return new AuthResponse(accessToken, refreshTokenValue);
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        var refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        refreshTokenService.deleteByToken(request.getRefreshToken());
        auditService.logEvent("LOGOUT", refreshToken.getUser().getEmail(), "USER", refreshToken.getUser().getId(), "User logged out");
    }
}

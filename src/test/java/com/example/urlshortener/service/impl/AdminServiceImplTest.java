package com.example.urlshortener.service.impl;

import com.example.urlshortener.dto.UserManagementResponse;
import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.entity.Url;
import com.example.urlshortener.entity.User;
import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User user;
    private Url url;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .enabled(true)
                .roles(Collections.emptySet())
                .build();

        url = Url.builder()
                .id(100L)
                .originalUrl("https://example.com/dest")
                .shortCode("abc123")
                .active(true)
                .expiresAt(Instant.now().plusSeconds(3600))
                .user(user)
                .build();
    }

    @Test
    public void testSetUserEnabled_Disable_LogsEvent() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserManagementResponse response = adminService.setUserEnabled(1L, false);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).save(user);
        // Verify that ADMIN_USER_DISABLE audit event was triggered
        verify(auditService, times(1)).logEvent(
                eq("ADMIN_USER_DISABLE"),
                anyString(),
                eq("USER"),
                eq(1L),
                contains("test@example.com")
        );
    }

    @Test
    public void testSetUserEnabled_Enable_LogsEvent() {
        // Arrange
        user.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserManagementResponse response = adminService.setUserEnabled(1L, true);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).save(user);
        // Verify that ADMIN_USER_ENABLE audit event was triggered
        verify(auditService, times(1)).logEvent(
                eq("ADMIN_USER_ENABLE"),
                anyString(),
                eq("USER"),
                eq(1L),
                contains("test@example.com")
        );
    }

    @Test
    public void testSetUrlEnabled_Disable_LogsEvent() {
        // Arrange
        when(urlRepository.findById(100L)).thenReturn(Optional.of(url));
        when(urlRepository.save(any(Url.class))).thenReturn(url);
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        // Act
        UrlResponse response = adminService.setUrlEnabled(100L, false);

        // Assert
        assertNotNull(response);
        verify(urlRepository, times(1)).save(url);
        // Verify that ADMIN_URL_DISABLE audit event was triggered
        verify(auditService, times(1)).logEvent(
                eq("ADMIN_URL_DISABLE"),
                anyString(),
                eq("URL"),
                eq(100L),
                contains("abc123")
        );
    }

    @Test
    public void testSetUrlEnabled_Enable_LogsEvent() {
        // Arrange
        url.setActive(false);
        when(urlRepository.findById(100L)).thenReturn(Optional.of(url));
        when(urlRepository.save(any(Url.class))).thenReturn(url);
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        // Act
        UrlResponse response = adminService.setUrlEnabled(100L, true);

        // Assert
        assertNotNull(response);
        verify(urlRepository, times(1)).save(url);
        // Verify that ADMIN_URL_ENABLE audit event was triggered
        verify(auditService, times(1)).logEvent(
                eq("ADMIN_URL_ENABLE"),
                anyString(),
                eq("URL"),
                eq(100L),
                contains("abc123")
        );
    }
}

package com.example.urlshortener.service.impl;

import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.entity.Url;
import com.example.urlshortener.entity.User;
import com.example.urlshortener.exception.ResourceNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UrlServiceImplTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private UrlServiceImpl urlService;

    private User user;
    private Url url;
    private UrlResponse urlResponse;

    @BeforeEach
    public void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        url = Url.builder()
                .id(100L)
                .originalUrl("https://example.com/dest")
                .shortCode("abc123")
                .active(true)
                .clickCount(0L)
                .expiresAt(Instant.now().plusSeconds(3600))
                .user(user)
                .build();

        urlResponse = new UrlResponse(
                100L,
                "https://example.com/dest",
                "abc123",
                null,
                true,
                0L,
                Instant.now(),
                Instant.now(),
                url.getExpiresAt()
        );
    }

    @Test
    public void testFindByShortCode_CacheHit() {
        // Arrange
        when(cacheManager.getCache("urlCache")).thenReturn(cache);
        when(cache.get("abc123", UrlResponse.class)).thenReturn(urlResponse);

        // Act
        UrlResponse result = urlService.findByShortCode("abc123");

        // Assert
        assertNotNull(result);
        assertEquals("https://example.com/dest", result.getOriginalUrl());
        assertEquals("abc123", result.getShortCode());
        
        // Verify no database lookup occurred
        verify(urlRepository, never()).findByShortCodeOrCustomAliasIgnoreCase(anyString(), anyString());
    }

    @Test
    public void testFindByShortCode_CacheMiss() {
        // Arrange
        when(cacheManager.getCache("urlCache")).thenReturn(cache);
        when(cache.get("abc123", UrlResponse.class)).thenReturn(null);
        when(urlRepository.findByShortCodeOrCustomAliasIgnoreCase("abc123", "abc123"))
                .thenReturn(Optional.of(url));

        // Act
        UrlResponse result = urlService.findByShortCode("abc123");

        // Assert
        assertNotNull(result);
        assertEquals("https://example.com/dest", result.getOriginalUrl());
        
        // Verify database lookup did occur
        verify(urlRepository, times(1)).findByShortCodeOrCustomAliasIgnoreCase("abc123", "abc123");
        // Verify result is put in cache
        verify(cache, times(1)).put(eq("abc123"), any(UrlResponse.class));
    }

    @Test
    public void testFindByShortCode_RedisConnectionFailure_FallsBackToDB() {
        // Arrange
        when(cacheManager.getCache("urlCache")).thenReturn(cache);
        // Simulate Redis exception
        when(cache.get("abc123", UrlResponse.class)).thenThrow(new RuntimeException("Redis connection timed out"));
        when(urlRepository.findByShortCodeOrCustomAliasIgnoreCase("abc123", "abc123"))
                .thenReturn(Optional.of(url));

        // Act
        UrlResponse result = urlService.findByShortCode("abc123");

        // Assert
        assertNotNull(result);
        assertEquals("https://example.com/dest", result.getOriginalUrl());
        
        // Verify database lookup successfully occurred as fallback
        verify(urlRepository, times(1)).findByShortCodeOrCustomAliasIgnoreCase("abc123", "abc123");
    }

    @Test
    public void testFindByShortCode_ExpiredUrl_ThrowsException() {
        // Arrange
        url.setExpiresAt(Instant.now().minusSeconds(10));
        when(cacheManager.getCache("urlCache")).thenReturn(cache);
        when(cache.get("abc123", UrlResponse.class)).thenReturn(null);
        when(urlRepository.findByShortCodeOrCustomAliasIgnoreCase("abc123", "abc123"))
                .thenReturn(Optional.of(url));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            urlService.findByShortCode("abc123");
        });
    }

    @Test
    public void testUpdateUrl_EvictsBothOldAndNewCustomAliases() {
        // Arrange
        url.setCustomAlias("oldalias");
        
        com.example.urlshortener.dto.UrlUpdateRequest request = new com.example.urlshortener.dto.UrlUpdateRequest();
        request.setCustomAlias("newalias");
        request.setOriginalUrl("https://example.com/newdest");

        when(urlRepository.findById(100L)).thenReturn(Optional.of(url));
        when(urlRepository.existsByCustomAliasIgnoreCase("newalias")).thenReturn(false);
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        // Act
        urlService.updateUrl(100L, request, "test@example.com");

        // Assert
        // Verify we evict the short code, the old alias, and the new alias
        verify(cache, atLeastOnce()).evict("abc123");
        verify(cache, atLeastOnce()).evict("oldalias");
        verify(cache, atLeastOnce()).evict("newalias");
    }

    @Test
    public void testDeleteUrl_EvictsShortCodeCache() {
        // Arrange
        when(urlRepository.findById(100L)).thenReturn(Optional.of(url));
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        // Act
        urlService.deleteUrl(100L, "test@example.com");

        // Assert
        verify(urlRepository, times(1)).delete(url);
        verify(cache, atLeastOnce()).evict("abc123");
    }

    @Test
    public void testSetActive_EvictsShortCodeCache() {
        // Arrange
        when(urlRepository.findById(100L)).thenReturn(Optional.of(url));
        when(urlRepository.save(any(Url.class))).thenReturn(url);
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        // Act
        urlService.setActive(100L, false, "test@example.com");

        // Assert
        verify(cache, atLeastOnce()).evict("abc123");
    }

    @Test
    public void testFindByShortCode_UserIsDisabled_RedirectsSuccessfully() {
        // Arrange
        user.setEnabled(false);
        
        when(cacheManager.getCache("urlCache")).thenReturn(cache);
        when(cache.get("abc123", UrlResponse.class)).thenReturn(null);
        when(urlRepository.findByShortCodeOrCustomAliasIgnoreCase("abc123", "abc123"))
                .thenReturn(Optional.of(url));

        // Act
        UrlResponse result = urlService.findByShortCode("abc123");

        // Assert
        assertNotNull(result);
        assertEquals("https://example.com/dest", result.getOriginalUrl());
    }
}

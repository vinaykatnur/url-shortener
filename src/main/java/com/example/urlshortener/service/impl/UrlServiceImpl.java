package com.example.urlshortener.service.impl;

import com.example.urlshortener.dto.UrlCreateRequest;
import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.dto.UrlUpdateRequest;
import com.example.urlshortener.entity.Url;
import com.example.urlshortener.entity.User;
import com.example.urlshortener.exception.DuplicateResourceException;
import com.example.urlshortener.exception.ResourceNotFoundException;
import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.service.AuditService;
import com.example.urlshortener.service.UrlService;
import com.example.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public UrlResponse createUrl(UrlCreateRequest request, String userEmail) {
        User user = findUserByEmail(userEmail);
        if (request.getCustomAlias() != null && urlRepository.existsByCustomAliasIgnoreCase(request.getCustomAlias())) {
            throw new DuplicateResourceException("Custom alias is already in use");
        }

        String shortCode = generateUniqueShortCode();
        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .customAlias(toLowerCase(request.getCustomAlias()))
                .active(true)
                .expiresAt(normalizeExpiresAt(request.getExpiresAt()))
                .user(user)
                .build();
        Url saved = urlRepository.save(url);
        auditService.logEvent("URL_CREATE", userEmail, "URL", saved.getId(), "Created URL " + saved.getShortCode());
        evictCache(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UrlResponse updateUrl(Long id, UrlUpdateRequest request, String userEmail) {
        Url url = findUrlByIdAndAuthorize(id, userEmail);
        String oldCustomAlias = url.getCustomAlias();

        if (request.getCustomAlias() != null) {
            String normalizedAlias = toLowerCase(request.getCustomAlias());
            if (!normalizedAlias.equals(url.getCustomAlias()) && urlRepository.existsByCustomAliasIgnoreCase(normalizedAlias)) {
                throw new DuplicateResourceException("Custom alias is already in use");
            }
            url.setCustomAlias(normalizedAlias);
        }

        if (request.getOriginalUrl() != null) {
            url.setOriginalUrl(request.getOriginalUrl());
        }
        url.setExpiresAt(normalizeExpiresAt(request.getExpiresAt()));

        Url updated = urlRepository.save(url);
        auditService.logEvent("URL_UPDATE", userEmail, "URL", updated.getId(), "Updated URL " + updated.getShortCode());
        evictCache(updated);
        if (oldCustomAlias != null && !oldCustomAlias.equalsIgnoreCase(updated.getCustomAlias())) {
            evictCacheKey(oldCustomAlias);
        }
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUrl(Long id, String userEmail) {
        Url url = findUrlByIdAndAuthorize(id, userEmail);
        urlRepository.delete(url);
        auditService.logEvent("URL_DELETE", userEmail, "URL", url.getId(), "Deleted URL " + url.getShortCode());
        evictCache(url);
    }

    @Override
    @Transactional
    public UrlResponse setActive(Long id, boolean active, String userEmail) {
        Url url = findUrlByIdAndAuthorize(id, userEmail);
        url.setActive(active);
        Url updated = urlRepository.save(url);
        auditService.logEvent(active ? "URL_ENABLE" : "URL_DISABLE", userEmail, "URL", updated.getId(), (active ? "Enabled" : "Disabled") + " URL " + updated.getShortCode());
        evictCache(updated);
        return toResponse(updated);
    }

    @Override
    public UrlResponse getUrlDetails(Long id, String userEmail) {
        Url url = findUrlByIdAndAuthorize(id, userEmail);
        return toResponse(url);
    }

    @Override
    public Page<UrlResponse> listUrls(String userEmail, String search, Boolean active, Pageable pageable) {
        User user = findUserByEmail(userEmail);
        Page<Url> page = urlRepository.findByUserIdAndSearchTermAndActive(user.getId(), normalizeSearch(search), active, pageable);
        List<UrlResponse> responses = page.getContent().stream().map(this::toResponse).collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    @Override
    public UrlResponse findByShortCode(String shortCode) {
        var cache = cacheManager.getCache("urlCache");
        if (cache != null) {
            try {
                UrlResponse cachedResponse = cache.get(shortCode, UrlResponse.class);
                if (cachedResponse != null) {
                    Instant cachedExpires = normalizeExpiresAt(cachedResponse.getExpiresAt());
                    if (cachedExpires != null && cachedExpires.isBefore(Instant.now())) {
                        try {
                            cache.evict(shortCode);
                        } catch (Exception ignored) {}
                        throw new ResourceNotFoundException("URL has expired");
                    }
                    if (!cachedResponse.isActive()) {
                        try {
                            cache.evict(shortCode);
                        } catch (Exception ignored) {}
                        throw new ResourceNotFoundException("URL is disabled");
                    }
                    return cachedResponse;
                }
            } catch (Exception e) {
                log.error("Redis cache access failed in findByShortCode: {}", e.getMessage());
            }
        }

        Url url = urlRepository.findByShortCodeOrCustomAliasIgnoreCase(shortCode, shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short code not found"));
        validateUrlActive(url);
        validateUrlNotExpired(url);

        UrlResponse response = toResponse(url);

        if (cache != null) {
            try {
                cache.put(shortCode, response);
                if (url.getCustomAlias() != null && !url.getCustomAlias().equalsIgnoreCase(shortCode)) {
                    cache.put(url.getCustomAlias(), response);
                }
                if (url.getShortCode() != null && !url.getShortCode().equalsIgnoreCase(shortCode)) {
                    cache.put(url.getShortCode(), response);
                }
            } catch (Exception e) {
                log.error("Redis cache write failed: {}", e.getMessage());
            }
        }

        return response;
    }

    private Url findUrlByIdAndAuthorize(Long id, String userEmail) {
        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));
        if (!isOwnerOrAdmin(url.getUser().getEmail(), userEmail)) {
            throw new AccessDeniedException("Access denied");
        }
        return url;
    }

    private boolean isOwnerOrAdmin(String ownerEmail, String currentEmail) {
        if (ownerEmail.equalsIgnoreCase(currentEmail)) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String generateUniqueShortCode() {
        String shortCode;
        int attempts = 0;
        do {
            shortCode = Base62Encoder.generateRandomCode(6);
            attempts++;
            if (attempts > 10) {
                shortCode = Base62Encoder.generateRandomCode(8);
            }
        } while (urlRepository.existsByShortCode(shortCode));
        return shortCode;
    }

    private void validateUrlActive(Url url) {
        if (!url.isActive()) {
            throw new ResourceNotFoundException("URL is disabled");
        }
    }

    private void validateUrlNotExpired(Url url) {
        Instant expiresAt = normalizeExpiresAt(url.getExpiresAt());
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            throw new ResourceNotFoundException("URL has expired");
        }
    }



    private UrlResponse toResponse(Url url) {
        return new UrlResponse(
                url.getId(),
                url.getOriginalUrl(),
                url.getShortCode(),
                url.getCustomAlias(),
                url.isActive(),
                url.getClickCount(),
                url.getCreatedAt(),
                url.getUpdatedAt(),
                normalizeExpiresAt(url.getExpiresAt())
        );
    }

    private String normalizeSearch(String search) {
        return search == null || search.isBlank() ? null : search.toLowerCase();
    }

    private String toLowerCase(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private void evictCache(Url url) {
        if (url == null) return;
        try {
            var cache = cacheManager.getCache("urlCache");
            if (cache != null) {
                if (url.getShortCode() != null) {
                    cache.evict(url.getShortCode());
                }
                if (url.getCustomAlias() != null) {
                    cache.evict(url.getCustomAlias());
                }
            }
            var analyticsCache = cacheManager.getCache("urlAnalytics");
            if (analyticsCache != null && url.getId() != null) {
                analyticsCache.evict(url.getId());
            }
            var topUrlsCache = cacheManager.getCache("topUrls");
            if (topUrlsCache != null) {
                topUrlsCache.clear();
            }
            var dashboardStatsCache = cacheManager.getCache("dashboardStats");
            if (dashboardStatsCache != null) {
                dashboardStatsCache.clear();
            }
        } catch (Exception e) {
            log.error("Redis cache eviction failed: {}", e.getMessage());
        }
    }

    private void evictCacheKey(String key) {
        if (key == null) return;
        try {
            var cache = cacheManager.getCache("urlCache");
            if (cache != null) {
                cache.evict(key);
            }
        } catch (Exception e) {
            log.error("Redis cache key eviction failed: {}", e.getMessage());
        }
    }

    private Instant normalizeExpiresAt(Instant expiresAt) {
        if (expiresAt == null || expiresAt.getEpochSecond() <= 86400L * 31) {
            return null;
        }
        return expiresAt;
    }
}

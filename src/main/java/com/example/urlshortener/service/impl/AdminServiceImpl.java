package com.example.urlshortener.service.impl;

import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.dto.UserManagementResponse;
import com.example.urlshortener.entity.Role;
import com.example.urlshortener.entity.Url;
import com.example.urlshortener.entity.User;
import com.example.urlshortener.exception.ResourceNotFoundException;
import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.service.AdminService;
import com.example.urlshortener.service.AuditService;
import org.springframework.cache.CacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final UrlRepository urlRepository;
    private final AuditService auditService;
    private final CacheManager cacheManager;

    @Override
    public Page<UserManagementResponse> listUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream().map(this::toUserResponse).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public UserManagementResponse setUserEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        auditService.logEvent(enabled ? "ADMIN_USER_ENABLE" : "ADMIN_USER_DISABLE", getCurrentUserEmail(), "USER", saved.getId(), (enabled ? "Enabled" : "Disabled") + " user " + saved.getEmail());
        return toUserResponse(saved);
    }

    @Override
    public Page<UrlResponse> listAllUrls(String search, Pageable pageable) {
        Page<Url> page;
        if (search == null || search.isBlank()) {
            page = urlRepository.findAll(pageable);
        } else {
            page = urlRepository.findBySearchTerm(search.toLowerCase(), pageable);
        }
        return new PageImpl<>(page.getContent().stream().map(this::toUrlResponse).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public UrlResponse setUrlEnabled(Long urlId, boolean enabled) {
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));
        url.setActive(enabled);
        Url saved = urlRepository.save(url);
        auditService.logEvent(enabled ? "ADMIN_URL_ENABLE" : "ADMIN_URL_DISABLE", getCurrentUserEmail(), "URL", saved.getId(), (enabled ? "Enabled" : "Disabled") + " URL " + saved.getShortCode() + " by Admin");
        evictCache(saved);
        return toUrlResponse(saved);
    }

    private String getCurrentUserEmail() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
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

    private UserManagementResponse toUserResponse(User user) {
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        return new UserManagementResponse(user.getId(), user.getName(), user.getEmail(), user.isEnabled(), roles, user.getCreatedAt(), user.getUpdatedAt());
    }

    private UrlResponse toUrlResponse(Url url) {
        return new UrlResponse(url.getId(), url.getOriginalUrl(), url.getShortCode(), url.getCustomAlias(), url.isActive(), url.getClickCount(), url.getCreatedAt(), url.getUpdatedAt(), normalizeExpiresAt(url.getExpiresAt()));
    }

    private Instant normalizeExpiresAt(Instant expiresAt) {
        if (expiresAt == null || expiresAt.getEpochSecond() <= 86400L * 31) {
            return null;
        }
        return expiresAt;
    }
}

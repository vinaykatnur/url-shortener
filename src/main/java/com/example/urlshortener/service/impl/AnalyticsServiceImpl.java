package com.example.urlshortener.service.impl;

import com.example.urlshortener.dto.ClickEventResponse;
import com.example.urlshortener.dto.DashboardStatsResponse;
import com.example.urlshortener.dto.UrlAnalyticsResponse;
import com.example.urlshortener.dto.UserUrlAnalyticsResponse;
import com.example.urlshortener.entity.ClickEvent;
import com.example.urlshortener.entity.Url;
import com.example.urlshortener.entity.User;
import com.example.urlshortener.exception.ResourceNotFoundException;
import com.example.urlshortener.repository.ClickEventRepository;
import org.springframework.security.access.AccessDeniedException;
import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final ClickEventRepository clickEventRepository;
    private final CacheManager cacheManager;

    @Override
    @Transactional(readOnly = true)
    public List<UserUrlAnalyticsResponse> getMyUrlsAnalytics(String userEmail) {
        User user = findUserByEmail(userEmail);
        return urlRepository.findByUserId(user.getId(), PageRequest.of(0, Integer.MAX_VALUE)).getContent().stream()
                .map(url -> new UserUrlAnalyticsResponse(url.getId(), url.getOriginalUrl(), url.getShortCode(), clickEventRepository.countByUrlId(url.getId()), url.getCreatedAt()))
                .collect(Collectors.toList());
    }

    private Instant startOfToday() {
        return java.time.LocalDate.now(java.time.ZoneOffset.UTC).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
    }

    @Override
    @Transactional(readOnly = true)
    public UrlAnalyticsResponse getUrlAnalytics(Long urlId, String userEmail) {
        Url url = findUrlByIdAndAuthorize(urlId, userEmail);
        long totalClicks = clickEventRepository.countByUrlId(urlId);
        Instant now = Instant.now();
        Instant todayStart = startOfToday();
        long clicksToday = clickEventRepository.countByUrlIdSince(urlId, todayStart);
        long clicksLast7Days = clickEventRepository.countByUrlIdSince(urlId, now.minus(Duration.ofDays(7)));
        long clicksLast30Days = clickEventRepository.countByUrlIdSince(urlId, now.minus(Duration.ofDays(30)));
        Instant firstClick = clickEventRepository.findFirstClickDateByUrlId(urlId).orElse(null);
        Instant lastClick = clickEventRepository.findLastClickDateByUrlId(urlId).orElse(null);
        return new UrlAnalyticsResponse(url.getId(), url.getOriginalUrl(), url.getShortCode(), url.getCustomAlias(), totalClicks, clicksToday, clicksLast7Days, clicksLast30Days, firstClick, lastClick);
    }

    @Override
    @Cacheable(value = "dashboardStats")
    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalUrls = urlRepository.count();
        long activeUrls = urlRepository.countByActiveTrue();
        long totalClicks = clickEventRepository.countAllClicks();
        Instant todayStart = startOfToday();
        long urlsCreatedToday = urlRepository.countByCreatedAtAfter(todayStart);
        long usersRegisteredToday = userRepository.countByCreatedAtAfter(todayStart);
        return new DashboardStatsResponse(totalUsers, totalUrls, activeUrls, totalClicks, urlsCreatedToday, usersRegisteredToday);
    }

    @Override
    @Cacheable(value = "topUrls")
    public List<UrlAnalyticsResponse> getTopUrls() {
        Instant now = Instant.now();
        Instant todayStart = startOfToday();
        return urlRepository.findTop10ByOrderByClickCountDesc().stream()
                .map(url -> new UrlAnalyticsResponse(url.getId(), url.getOriginalUrl(), url.getShortCode(), url.getCustomAlias(), url.getClickCount(), clickEventRepository.countByUrlIdSince(url.getId(), todayStart), clickEventRepository.countByUrlIdSince(url.getId(), now.minus(Duration.ofDays(7))), clickEventRepository.countByUrlIdSince(url.getId(), now.minus(Duration.ofDays(30))), clickEventRepository.findFirstClickDateByUrlId(url.getId()).orElse(null), clickEventRepository.findLastClickDateByUrlId(url.getId()).orElse(null)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Async("analyticsExecutor")
    @CacheEvict(value = "urlAnalytics", key = "#urlId")
    public void recordClickEvent(Long urlId, String ipAddress, String userAgent, String referer, String country, String city) {
        Url url = urlRepository.findById(urlId)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));
        ClickEvent event = ClickEvent.builder()
                .url(url)
                .clickedAt(Instant.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .referer(referer)
                .country(country)
                .city(city)
                .build();
        clickEventRepository.save(event);
        urlRepository.incrementClickCount(urlId);
    }

    private Url findUrlByIdAndAuthorize(Long urlId, String userEmail) {
        Url url = urlRepository.findById(urlId)
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
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

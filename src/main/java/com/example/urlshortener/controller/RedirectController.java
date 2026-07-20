package com.example.urlshortener.controller;

import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.service.AnalyticsService;
import com.example.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Value;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;
    private final AnalyticsService analyticsService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @GetMapping("/")
    public ResponseEntity<Void> redirectToFrontend() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, frontendUrl);
        return ResponseEntity.status(302).headers(headers).build();
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        UrlResponse urlResponse = urlService.findByShortCode(shortCode);
        analyticsService.recordClickEvent(
                urlResponse.getId(),
                extractClientIp(request),
                request.getHeader("User-Agent"),
                request.getHeader("Referer"),
                null,
                null
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, urlResponse.getOriginalUrl());
        return ResponseEntity.status(302).headers(headers).build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

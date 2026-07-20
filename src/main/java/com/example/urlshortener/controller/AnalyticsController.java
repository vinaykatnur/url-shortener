package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ApiResponse;
import com.example.urlshortener.dto.DashboardStatsResponse;
import com.example.urlshortener.dto.UrlAnalyticsResponse;
import com.example.urlshortener.dto.UserUrlAnalyticsResponse;
import com.example.urlshortener.service.AnalyticsService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/my-urls")
    public ResponseEntity<ApiResponse<List<UserUrlAnalyticsResponse>>> getMyUrlsAnalytics(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Analytics retrieved", analyticsService.getMyUrlsAnalytics(userDetails.getUsername())));
    }

    @GetMapping("/urls/{id}")
    public ResponseEntity<ApiResponse<UrlAnalyticsResponse>> getUrlAnalytics(@PathVariable @NotNull Long id,
                                                                             @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(new ApiResponse<>(true, "URL analytics retrieved", analyticsService.getUrlAnalytics(id, userDetails.getUsername())));
    }

    @GetMapping("/top-urls")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UrlAnalyticsResponse>>> getTopUrls() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Top URLs retrieved", analyticsService.getTopUrls()));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard stats retrieved", analyticsService.getDashboardStats()));
    }
}

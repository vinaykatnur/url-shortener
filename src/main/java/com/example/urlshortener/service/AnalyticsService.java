package com.example.urlshortener.service;

import com.example.urlshortener.dto.ClickEventResponse;
import com.example.urlshortener.dto.DashboardStatsResponse;
import com.example.urlshortener.dto.UrlAnalyticsResponse;
import com.example.urlshortener.dto.UserUrlAnalyticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnalyticsService {
    List<UserUrlAnalyticsResponse> getMyUrlsAnalytics(String userEmail);
    UrlAnalyticsResponse getUrlAnalytics(Long urlId, String userEmail);
    DashboardStatsResponse getDashboardStats();
    List<UrlAnalyticsResponse> getTopUrls();
    void recordClickEvent(Long urlId, String ipAddress, String userAgent, String referer, String country, String city);
}

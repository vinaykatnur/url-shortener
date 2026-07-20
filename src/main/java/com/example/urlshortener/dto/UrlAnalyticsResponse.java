package com.example.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UrlAnalyticsResponse {
    private Long id;
    private String originalUrl;
    private String shortCode;
    private String customAlias;
    private long totalClicks;
    private long clicksToday;
    private long clicksLast7Days;
    private long clicksLast30Days;
    private Instant firstClickDate;
    private Instant lastClickDate;
}

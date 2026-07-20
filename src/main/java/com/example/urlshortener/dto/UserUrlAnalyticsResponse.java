package com.example.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class UserUrlAnalyticsResponse {
    private Long id;
    private String originalUrl;
    private String shortCode;
    private long totalClicks;
    private Instant createdAt;
}

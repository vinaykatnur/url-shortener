package com.example.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class ClickEventResponse {
    private Instant clickedAt;
    private String ipAddress;
    private String userAgent;
    private String referer;
    private String country;
    private String city;
}

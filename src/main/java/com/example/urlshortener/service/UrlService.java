package com.example.urlshortener.service;

import com.example.urlshortener.dto.UrlCreateRequest;
import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.dto.UrlUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UrlService {
    UrlResponse createUrl(UrlCreateRequest request, String userEmail);
    UrlResponse updateUrl(Long id, UrlUpdateRequest request, String userEmail);
    void deleteUrl(Long id, String userEmail);
    UrlResponse setActive(Long id, boolean active, String userEmail);
    UrlResponse getUrlDetails(Long id, String userEmail);
    Page<UrlResponse> listUrls(String userEmail, String search, Boolean active, Pageable pageable);
    UrlResponse findByShortCode(String shortCode);
}

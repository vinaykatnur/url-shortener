package com.example.urlshortener.service;

import com.example.urlshortener.dto.UserManagementResponse;
import com.example.urlshortener.dto.UrlResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    Page<UserManagementResponse> listUsers(Pageable pageable);
    UserManagementResponse setUserEnabled(Long userId, boolean enabled);
    Page<UrlResponse> listAllUrls(String search, Pageable pageable);
    UrlResponse setUrlEnabled(Long urlId, boolean enabled);
}

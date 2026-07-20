package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ApiResponse;
import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.dto.UserManagementResponse;
import com.example.urlshortener.service.AdminService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserManagementResponse>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserManagementResponse> users = adminService.listUsers(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved", users));
    }

    @PutMapping("/users/{id}/disable")
    public ResponseEntity<ApiResponse<UserManagementResponse>> disableUser(@PathVariable @NotNull Long id) {
        UserManagementResponse user = adminService.setUserEnabled(id, false);
        return ResponseEntity.ok(new ApiResponse<>(true, "User disabled", user));
    }

    @PutMapping("/users/{id}/enable")
    public ResponseEntity<ApiResponse<UserManagementResponse>> enableUser(@PathVariable @NotNull Long id) {
        UserManagementResponse user = adminService.setUserEnabled(id, true);
        return ResponseEntity.ok(new ApiResponse<>(true, "User enabled", user));
    }

    @GetMapping("/urls")
    public ResponseEntity<ApiResponse<Page<UrlResponse>>> listUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UrlResponse> urls = adminService.listAllUrls(search, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "URLs retrieved", urls));
    }

    @PutMapping("/urls/{id}/disable")
    public ResponseEntity<ApiResponse<UrlResponse>> disableUrl(@PathVariable @NotNull Long id) {
        UrlResponse url = adminService.setUrlEnabled(id, false);
        return ResponseEntity.ok(new ApiResponse<>(true, "URL disabled", url));
    }

    @PutMapping("/urls/{id}/enable")
    public ResponseEntity<ApiResponse<UrlResponse>> enableUrl(@PathVariable @NotNull Long id) {
        UrlResponse url = adminService.setUrlEnabled(id, true);
        return ResponseEntity.ok(new ApiResponse<>(true, "URL enabled", url));
    }
}

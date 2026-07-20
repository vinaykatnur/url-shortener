package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ApiResponse;
import com.example.urlshortener.dto.UrlCreateRequest;
import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.dto.UrlUpdateRequest;
import com.example.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    public ResponseEntity<ApiResponse<UrlResponse>> createUrl(@Valid @RequestBody UrlCreateRequest request,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        UrlResponse response = urlService.createUrl(request, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "URL created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UrlResponse>> updateUrl(@PathVariable Long id,
                                                              @Valid @RequestBody UrlUpdateRequest request,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        UrlResponse response = urlService.updateUrl(id, request, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "URL updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUrl(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        urlService.deleteUrl(id, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "URL deleted successfully", null));
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<UrlResponse>> disableUrl(@PathVariable Long id,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        UrlResponse response = urlService.setActive(id, false, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "URL disabled successfully", response));
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<UrlResponse>> enableUrl(@PathVariable Long id,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        UrlResponse response = urlService.setActive(id, true, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "URL enabled successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UrlResponse>> getUrlDetails(@PathVariable Long id,
                                                                  @AuthenticationPrincipal UserDetails userDetails) {
        UrlResponse response = urlService.getUrlDetails(id, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "URL details retrieved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UrlResponse>>> listUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UrlResponse> urls = urlService.listUrls(userDetails.getUsername(), search, active, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "URLs retrieved", urls));
    }
}

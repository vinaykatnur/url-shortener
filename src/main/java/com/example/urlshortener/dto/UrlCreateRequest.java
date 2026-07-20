package com.example.urlshortener.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UrlCreateRequest {

    @NotBlank(message = "Original URL is required")
    @Size(max = 2048, message = "Original URL must be at most 2048 characters")
    @Pattern(regexp = "^(https?://).+", message = "Original URL must start with http:// or https://")
    private String originalUrl;

    @Pattern(regexp = "^[A-Za-z0-9_-]{4,100}$", message = "Custom alias must be 4-100 characters and contain only letters, digits, hyphens, or underscores")
    private String customAlias;

    @Future(message = "Expiration date must be in the future")
    private Instant expiresAt;
}

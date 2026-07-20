package com.example.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class UserManagementResponse {
    private Long id;
    private String name;
    private String email;
    private boolean enabled;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}

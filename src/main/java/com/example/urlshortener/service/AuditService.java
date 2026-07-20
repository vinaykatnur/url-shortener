package com.example.urlshortener.service;

public interface AuditService {
    void logEvent(String eventType, String userEmail, String subjectType, Long subjectId, String detail);
}

package com.example.urlshortener.service.impl;

import com.example.urlshortener.entity.AuditLog;
import com.example.urlshortener.repository.AuditLogRepository;
import com.example.urlshortener.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEvent(String eventType, String userEmail, String subjectType, Long subjectId, String detail) {
        AuditLog log = AuditLog.builder()
                .eventType(eventType)
                .userEmail(userEmail)
                .subjectType(subjectType)
                .subjectId(subjectId)
                .detail(detail)
                .build();
        auditLogRepository.save(log);
    }
}

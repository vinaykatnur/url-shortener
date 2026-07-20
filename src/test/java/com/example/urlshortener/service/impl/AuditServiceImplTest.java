package com.example.urlshortener.service.impl;

import com.example.urlshortener.entity.AuditLog;
import com.example.urlshortener.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    @Test
    public void testLogEvent_DelegatesToRepositorySave() {
        // Act
        auditService.logEvent("URL_UPDATE", "user@example.com", "URL", 100L, "Details");

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertNotNull(saved);
        assertEquals("URL_UPDATE", saved.getEventType());
        assertEquals("user@example.com", saved.getUserEmail());
        assertEquals("URL", saved.getSubjectType());
        assertEquals(100L, saved.getSubjectId());
        assertEquals("Details", saved.getDetail());
    }
}

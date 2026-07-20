-- Migration V4: create click_events and audit_logs tables

CREATE TABLE IF NOT EXISTS click_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url_id BIGINT NOT NULL,
    clicked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent VARCHAR(512),
    referer VARCHAR(1024),
    country VARCHAR(100),
    city VARCHAR(100),
    CONSTRAINT fk_click_events_url FOREIGN KEY (url_id) REFERENCES urls(id),
    INDEX idx_click_events_url_id (url_id),
    INDEX idx_click_events_clicked_at (clicked_at)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    user_email VARCHAR(150),
    subject_type VARCHAR(100),
    subject_id BIGINT,
    detail VARCHAR(1024),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_logs_user_email (user_email),
    INDEX idx_audit_logs_event_type (event_type)
);

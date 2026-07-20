-- Migration V4: create click_events and audit_logs tables

CREATE TABLE click_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url_id BIGINT NOT NULL,
    clicked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent VARCHAR(512),
    referer VARCHAR(1024),
    country VARCHAR(100),
    city VARCHAR(100),
    CONSTRAINT fk_click_events_url FOREIGN KEY (url_id) REFERENCES urls(id)
);

CREATE INDEX idx_click_events_url_id ON click_events (url_id);
CREATE INDEX idx_click_events_clicked_at ON click_events (clicked_at);


CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    user_email VARCHAR(150),
    subject_type VARCHAR(100),
    subject_id BIGINT,
    detail VARCHAR(1024),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user_email ON audit_logs (user_email);
CREATE INDEX idx_audit_logs_event_type ON audit_logs (event_type);

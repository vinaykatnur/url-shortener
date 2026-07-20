CREATE TABLE urls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_url VARCHAR(2048) NOT NULL,
    short_code VARCHAR(8) NOT NULL UNIQUE,
    custom_alias VARCHAR(100) UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    click_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_urls_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_urls_short_code ON urls (short_code);
CREATE INDEX idx_urls_custom_alias ON urls (custom_alias);
CREATE INDEX idx_urls_user_id ON urls (user_id);

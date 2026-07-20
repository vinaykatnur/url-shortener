-- V5: Convert legacy epoch expiration values to NULL
UPDATE urls
SET expires_at = NULL
WHERE expires_at IS NOT NULL AND expires_at <= '1970-01-31 23:59:59';

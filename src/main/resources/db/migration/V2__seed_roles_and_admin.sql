INSERT IGNORE INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

INSERT IGNORE INTO users (name, email, password, enabled) VALUES
('System Admin', 'admin@example.com', '$2a$10$Dow1K4AWX9d5P8AvV/3bMOgAcvhmNciqE.lZsMGhVQHi2TzY7eD8S', true);

INSERT IGNORE INTO users_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.email = 'admin@example.com';

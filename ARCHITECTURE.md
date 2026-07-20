# URL Shortener Platform Architecture

## 1. Package Structure

- `controller`
- `service`
- `repository`
- `entity`
- `dto`
- `mapper`
- `security`
- `config`
- `exception`
- `util`

## 2. ER Diagram

Entities:
- `User` (1) --- (M) `RefreshToken`
- `User` (M) --- (M) `Role`

## 3. Database Design

Tables:
- `users`
- `roles`
- `refresh_tokens`

Relationships:
- `users` has many `refresh_tokens`
- `users_roles` join table maps users to roles

## 4. Entity Design

User:
- `id`, `name`, `email`, `password`, `enabled`, `createdAt`, `updatedAt`
- Many-to-many relationship with `Role`

Role:
- `id`, `name`

RefreshToken:
- `id`, `token`, `expiresAt`, `createdAt`, `user`

## 5. DTO Design

Requests:
- `RegisterRequest`
- `LoginRequest`
- `RefreshTokenRequest`

Responses:
- `AuthResponse`
- `UserResponse`

## 6. API Design

Endpoints:
- `POST /api/v1/auth/register` — register new users
- `POST /api/v1/auth/login` — authenticate and issue tokens
- `POST /api/v1/auth/refresh` — refresh access token
- `POST /api/v1/auth/logout` — revoke refresh token

## 7. Security Design

- Spring Security with stateless JWT authentication
- `JwtAuthenticationFilter` validates access tokens
- `UserDetailsService` loads users with roles
- `AuthenticationManager` handles credential authentication
- `BCryptPasswordEncoder` secures passwords
- `TokenBlacklistService` prepared for future access token revocation
- RBAC with `ROLE_USER` and `ROLE_ADMIN`

## 8. Redis Usage Plan

Phase 1:
- Cache user authentication metadata if needed
- Store refresh tokens and token state in Redis
- Prepare keyspace for blacklisted JWT IDs

Future:
- caching URL lookup data
- session lockouts and rate limiting counters

## 9. Flyway Migration Plan

- `V1__initial_schema.sql` creates users, roles, refresh_tokens, and join table
- `V2__seed_roles_and_admin.sql` inserts `ROLE_USER`, `ROLE_ADMIN`, and seeded admin account

## 10. Risks & Mitigations

- `JWT_SECRET` exposure: use environment variables and secure vaults
- DB credential leakage: do not commit credentials in code
- brute force logins: add rate limiting later
- refresh token theft: store and validate tokens server-side
- invalid entity exposure: use DTOs only, avoid entity payloads

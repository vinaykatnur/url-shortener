package com.example.urlshortener.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final StringRedisTemplate redisTemplate;
    private static final int LOGIN_LIMIT = 10;
    private static final int REGISTER_LIMIT = 5;
    private static final int CREATE_URL_LIMIT = 20;
    private static final int REDIRECT_LIMIT = 200;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String key = null;
        int limit = 0;
        Duration window = Duration.ofMinutes(1);

        if (path.startsWith("/api/v1/auth/login") && method.equals("POST")) {
            key = getKey(request, "login");
            limit = LOGIN_LIMIT;
        } else if (path.startsWith("/api/v1/auth/register") && method.equals("POST")) {
            key = getKey(request, "register");
            limit = REGISTER_LIMIT;
        } else if (path.startsWith("/api/v1/urls") && method.equals("POST")) {
            key = getKey(request, "create-url");
            limit = CREATE_URL_LIMIT;
        } else if (method.equals("GET") && !path.startsWith("/api/") && !path.startsWith("/swagger") && !path.startsWith("/v3/") && !path.startsWith("/actuator")) {
            key = getKey(request, "redirect");
            limit = REDIRECT_LIMIT;
            window = Duration.ofMinutes(5);
        }

        if (key != null) {
            try {
                Long val = redisTemplate.opsForValue().increment(key);
                long count = val != null ? val : 0;
                if (count == 1) {
                    redisTemplate.expire(key, window);
                }
                if (count > limit) {
                    response.setStatus(429);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"message\":\"Too many requests\",\"errors\":[]}");
                    return;
                }
            } catch (Exception e) {
                log.error("Rate limiting Redis call failed, failing open: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getKey(HttpServletRequest request, String prefix) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return prefix + ":" + ip;
    }
}

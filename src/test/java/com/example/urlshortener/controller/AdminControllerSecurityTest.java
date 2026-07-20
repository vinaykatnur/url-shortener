package com.example.urlshortener.controller;

import com.example.urlshortener.config.SecurityConfig;
import com.example.urlshortener.security.CustomUserDetailsService;
import com.example.urlshortener.security.JwtAuthenticationFilter;
import com.example.urlshortener.security.JwtTokenProvider;
import com.example.urlshortener.security.RateLimitingFilter;
import com.example.urlshortener.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, RateLimitingFilter.class, JwtAuthenticationFilter.class})
public class AdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    public void setUp() {
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    public void testListUsers_WithAdminRole_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    public void testListUsers_WithUserRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testListUsers_AnonymousUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isForbidden());
    }
}

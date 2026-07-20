package com.example.urlshortener.controller;

import com.example.urlshortener.dto.LoginRequest;
import com.example.urlshortener.dto.RefreshTokenRequest;
import com.example.urlshortener.dto.RegisterRequest;
import com.example.urlshortener.entity.RefreshToken;
import com.example.urlshortener.entity.Role;
import com.example.urlshortener.entity.User;
import com.example.urlshortener.repository.AuditLogRepository;
import com.example.urlshortener.repository.RefreshTokenRepository;
import com.example.urlshortener.repository.RoleRepository;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private AuditLogRepository auditLogRepository;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    private User testUser;
    private Role userRole;

    @BeforeEach
    public void setUp() {
        userRole = new Role(1L, "ROLE_USER");
        testUser = User.builder()
                .id(1L)
                .name("Integration User")
                .email("integration@example.com")
                .password(passwordEncoder.encode("password123"))
                .enabled(true)
                .roles(new HashSet<>(Collections.singletonList(userRole)))
                .build();

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testAuthenticationWorkflow() throws Exception {
        // 1. Verify Registration Flow (returns void, logs REGISTER)
        when(userRepository.existsByEmail("integration@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Integration User");
        registerRequest.setEmail("integration@example.com");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.data").isEmpty());

        // Verify user was persisted
        verify(userRepository, times(1)).save(any(User.class));

        // 2. Verify Login Flow (returns JWTs)
        when(userRepository.findByEmail("integration@example.com")).thenReturn(Optional.of(testUser));
        
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("mock-refresh-token-uuid");
        mockRefreshToken.setUser(testUser);
        mockRefreshToken.setExpiresAt(Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("integration@example.com");
        loginRequest.setPassword("password123");

        String loginResponseContent = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").value("mock-refresh-token-uuid"))
                .andReturn().getResponse().getContentAsString();

        // Extract token
        String accessToken = objectMapper.readTree(loginResponseContent)
                .path("data").path("accessToken").asText();

        // 3. Verify access to protected route using JWT token
        mockMvc.perform(get("/api/v1/urls")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 4. Verify Logout Flow
        when(refreshTokenRepository.findByToken("mock-refresh-token-uuid")).thenReturn(Optional.of(mockRefreshToken));

        RefreshTokenRequest logoutRequest = new RefreshTokenRequest();
        logoutRequest.setRefreshToken("mock-refresh-token-uuid");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(refreshTokenRepository, times(1)).delete(mockRefreshToken);
    }

    @Test
    public void testLoginWithInvalidEmail_ReturnsBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("VINAY@777");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testRegisterWithInvalidEmail_ReturnsBadRequest() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Invalid Email User");
        registerRequest.setEmail("VINAY@777");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}

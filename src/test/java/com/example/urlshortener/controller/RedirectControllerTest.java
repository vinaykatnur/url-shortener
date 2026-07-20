package com.example.urlshortener.controller;

import com.example.urlshortener.dto.UrlResponse;
import com.example.urlshortener.service.AnalyticsService;
import com.example.urlshortener.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RedirectController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
public class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlService urlService;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private com.example.urlshortener.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedirect_TriggersAnalyticsAndRedirects() throws Exception {
        // Arrange
        String shortCode = "abc123";
        UrlResponse response = new UrlResponse(
                100L,
                "https://example.com/target",
                shortCode,
                null,
                true,
                5L,
                Instant.now(),
                Instant.now(),
                null
        );

        when(urlService.findByShortCode(shortCode)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/" + shortCode)
                .header("User-Agent", "TestAgent")
                .header("Referer", "TestReferer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "https://example.com/target"));

        // Verify that findByShortCode was called exactly once to resolve
        verify(urlService, times(1)).findByShortCode(shortCode);

        // Verify that recordClickEvent was called exactly once, ensuring no duplicate increments
        verify(analyticsService, times(1)).recordClickEvent(
                eq(100L),
                anyString(),
                eq("TestAgent"),
                eq("TestReferer"),
                isNull(),
                isNull()
        );
    }

    @Test
    public void testRedirectToFrontend_RedirectsSuccessfully() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost:3000"));
    }
}

package com.example.urlshortener.config;

import com.example.urlshortener.dto.DashboardStatsResponse;
import com.example.urlshortener.dto.UrlAnalyticsResponse;
import com.example.urlshortener.dto.UrlResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class RedisSerializationTest {

    private final GenericJackson2JsonRedisSerializer serializer;

    public RedisSerializationTest() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL,
            com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );
        this.serializer = new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Test
    public void testUrlResponseSerializationRoundTrip() {
        // Arrange
        UrlResponse original = new UrlResponse(
                100L,
                "https://example.com/dest",
                "abc123",
                "custom-alias",
                true,
                10L,
                Instant.now(),
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        // Act
        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        UrlResponse deserialized = (UrlResponse) serializer.deserialize(serialized);

        // Assert
        assertNotNull(deserialized);
        assertEquals(original.getId(), deserialized.getId());
        assertEquals(original.getOriginalUrl(), deserialized.getOriginalUrl());
        assertEquals(original.getShortCode(), deserialized.getShortCode());
        assertEquals(original.getCustomAlias(), deserialized.getCustomAlias());
        assertEquals(original.isActive(), deserialized.isActive());
        assertEquals(original.getClickCount(), deserialized.getClickCount());
        // Truncate comparison to epoch seconds to avoid minor nanosecond formatting discrepancies in jackson vs java
        assertEquals(original.getCreatedAt().getEpochSecond(), deserialized.getCreatedAt().getEpochSecond());
        assertEquals(original.getUpdatedAt().getEpochSecond(), deserialized.getUpdatedAt().getEpochSecond());
        assertEquals(original.getExpiresAt().getEpochSecond(), deserialized.getExpiresAt().getEpochSecond());
    }

    @Test
    public void testUrlAnalyticsResponseSerializationRoundTrip() {
        // Arrange
        UrlAnalyticsResponse original = new UrlAnalyticsResponse(
                200L,
                "https://example.com/dest2",
                "def456",
                "custom-alias2",
                25L,
                5L,
                15L,
                20L,
                Instant.now().minusSeconds(86400),
                Instant.now()
        );

        // Act
        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        UrlAnalyticsResponse deserialized = (UrlAnalyticsResponse) serializer.deserialize(serialized);

        // Assert
        assertNotNull(deserialized);
        assertEquals(original.getId(), deserialized.getId());
        assertEquals(original.getOriginalUrl(), deserialized.getOriginalUrl());
        assertEquals(original.getShortCode(), deserialized.getShortCode());
        assertEquals(original.getCustomAlias(), deserialized.getCustomAlias());
        assertEquals(original.getTotalClicks(), deserialized.getTotalClicks());
        assertEquals(original.getClicksToday(), deserialized.getClicksToday());
        assertEquals(original.getClicksLast7Days(), deserialized.getClicksLast7Days());
        assertEquals(original.getClicksLast30Days(), deserialized.getClicksLast30Days());
        assertEquals(original.getFirstClickDate().getEpochSecond(), deserialized.getFirstClickDate().getEpochSecond());
        assertEquals(original.getLastClickDate().getEpochSecond(), deserialized.getLastClickDate().getEpochSecond());
    }

    @Test
    public void testDashboardStatsResponseSerializationRoundTrip() {
        // Arrange
        DashboardStatsResponse original = new DashboardStatsResponse(
                100L,
                200L,
                150L,
                5000L,
                10L,
                5L
        );

        // Act
        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        DashboardStatsResponse deserialized = (DashboardStatsResponse) serializer.deserialize(serialized);

        // Assert
        assertNotNull(deserialized);
        assertEquals(original.getTotalUsers(), deserialized.getTotalUsers());
        assertEquals(original.getTotalUrls(), deserialized.getTotalUrls());
        assertEquals(original.getActiveUrls(), deserialized.getActiveUrls());
        assertEquals(original.getTotalClicks(), deserialized.getTotalClicks());
        assertEquals(original.getUrlsCreatedToday(), deserialized.getUrlsCreatedToday());
        assertEquals(original.getUsersRegisteredToday(), deserialized.getUsersRegisteredToday());
    }
}

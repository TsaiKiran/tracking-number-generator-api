package com.track;

import com.track.dto.TrackingNumberRequest;
import com.track.exception.TrackingNumberGenerationException;
import com.track.service.TrackingNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TrackingNumberGeneratorTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Value("${tracking.retry-count:5}")
    private int maxRetries;

    @Value("${tracking.random-length:8}")
    private int randomLength;

    private TrackingNumberGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        generator = new TrackingNumberGenerator(redisTemplate, 5, 8);
    }

    @Test
    void testGenerateTrackingNumber_Success() {
        //  Redis accepting new key (no collision) scenario
        when(valueOperations.setIfAbsent(anyString(), anyString())).thenReturn(true);
        TrackingNumberRequest request = TrackingNumberRequest.builder()
                .originCountryId("US")
                .destinationCountryId("IN")
                .createdAt(OffsetDateTime.parse("2024-02-14T10:15:30+00:00"))
                .customerName("Test Customer")
                .customerSlug("test-customer")
                .build();
        String trackingNumber = generator.generateTrackingNumber(request);
        // Should start with UI (US, IN first letters), then 24045 (2024, day 45), then 8 random chars (no O or 0)
        assertTrue(trackingNumber.matches("UI24045[ABCDEFGHIJKLMNPQRSTUVWXYZ123456789]{8}"));
        assertEquals(15, trackingNumber.length());
    }

    @Test
    void testGenerateTrackingNumber_RetriesAndFails() {
        //  Redis colliding (not accepting new key)
        when(valueOperations.setIfAbsent(anyString(), anyString())).thenReturn(false);
        TrackingNumberRequest request = TrackingNumberRequest.builder()
                .originCountryId("US")
                .destinationCountryId("IN")
                .createdAt(OffsetDateTime.now())
                .customerName("Dummy Test Customer")
                .customerSlug("dummy-test-customer")
                .build();
        assertThrows(TrackingNumberGenerationException.class, () -> generator.generateTrackingNumber(request));
    }

    @Test
    void testGenerateTrackingNumber_Format() {
        // Code to acceptt the key on first try
        when(valueOperations.setIfAbsent(anyString(), anyString())).thenReturn(true);
        TrackingNumberRequest request = TrackingNumberRequest.builder()
                .originCountryId("GB")
                .destinationCountryId("FR")
                .createdAt(OffsetDateTime.parse("2024-12-31T23:59:59+00:00"))
                .customerName("Test Customer")
                .customerSlug("test-customer")
                .build();
        String trackingNumber = generator.generateTrackingNumber(request);
        // Should start with GF (GB, FR), then 24366 (2024, day 366), then 8 random chars (no O or 0)
        assertTrue(trackingNumber.matches("GF24366[ABCDEFGHIJKLMNPQRSTUVWXYZ123456789]{8}"));
        assertEquals(15, trackingNumber.length());
    }
} 
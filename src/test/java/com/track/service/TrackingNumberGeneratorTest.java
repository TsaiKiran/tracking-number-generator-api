package com.track.service;

import com.track.dto.TrackingNumberRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingNumberGeneratorTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RedisAtomicLong redisAtomicLong;

    private TrackingNumberGenerator trackingNumberGenerator;

    @BeforeEach
    void setUp() {
        when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
        trackingNumberGenerator = new TrackingNumberGenerator(redisTemplate);
    }

    @Test
    void generateTrackingNumber_WithValidRequest_ReturnsCorrectFormat() {
        // dummy request
        TrackingNumberRequest request = TrackingNumberRequest.builder()
                .originCountryId("US")
                .destinationCountryId("GB")
                .createdAt(OffsetDateTime.of(2024, 3, 15, 10, 30, 0, 0, ZoneOffset.UTC))
                .build();

        // Mocking Redis atomic long behavior
        try (var mockStatic = mockStatic(RedisAtomicLong.class)) {
            mockStatic.when(() -> new RedisAtomicLong(anyString(), any(RedisConnectionFactory.class)))
                    .thenReturn(redisAtomicLong);
            when(redisAtomicLong.incrementAndGet()).thenReturn(12345L);


            String trackingNumber = trackingNumberGenerator.generateTrackingNumber(request);

            // Assert
            assertEquals("UG24031500012345", trackingNumber);
            verify(redisAtomicLong).expire(30, java.util.concurrent.TimeUnit.DAYS);
        }
    }

    @Test
    void generateTrackingNumber_WithNullCreatedAt_UsesCurrentDate() {
        // Dummy request
        TrackingNumberRequest request = TrackingNumberRequest.builder()
                .originCountryId("FR")
                .destinationCountryId("DE")
                .createdAt(null)
                .build();

        // Mock Redis atomic long behavior
        try (var mockStatic = mockStatic(RedisAtomicLong.class)) {
            mockStatic.when(() -> new RedisAtomicLong(anyString(), any(RedisConnectionFactory.class)))
                    .thenReturn(redisAtomicLong);
            when(redisAtomicLong.incrementAndGet()).thenReturn(1L);


            String trackingNumber = trackingNumberGenerator.generateTrackingNumber(request);


            assertTrue(trackingNumber.matches("FD\\d{6}00000001"));
            verify(redisAtomicLong).expire(30, java.util.concurrent.TimeUnit.DAYS);
        }
    }

    @Test
    void generateTrackingNumberPaddingZeros() {
//dummy
        TrackingNumberRequest request = TrackingNumberRequest.builder()
                .originCountryId("CN")
                .destinationCountryId("IN")
                .createdAt(OffsetDateTime.of(2024, 3, 15, 10, 30, 0, 0, ZoneOffset.UTC))
                .build();


        try (var mockStatic = mockStatic(RedisAtomicLong.class)) {
            mockStatic.when(() -> new RedisAtomicLong(anyString(), any(RedisConnectionFactory.class)))
                    .thenReturn(redisAtomicLong);
            when(redisAtomicLong.incrementAndGet()).thenReturn(5L);

            String trackingNumber = trackingNumberGenerator.generateTrackingNumber(request);

            assertEquals("CI24031500000005", trackingNumber);
            verify(redisAtomicLong).expire(30, java.util.concurrent.TimeUnit.DAYS);
        }
    }

} 
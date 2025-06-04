package com.track.service;

import com.track.dto.TrackingNumberRequest;
import com.track.exception.TrackingNumberGenerationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class TrackingNumberGenerator {
    private final int maxRetries;
    private final int randomLength;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789";
    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public TrackingNumberGenerator(
            RedisTemplate<String, String> redisTemplate,
            @Value("${tracking.retry-count:5}") int maxRetries,
            @Value("${tracking.random-length:8}") int randomLength) {
        this.redisTemplate = redisTemplate;
        this.maxRetries = maxRetries;
        this.randomLength = randomLength;
    }

    public String generateTrackingNumber(TrackingNumberRequest request) {
        String origin = request.getOriginCountryId().substring(0, 1).toUpperCase();
        String destination = request.getDestinationCountryId().substring(0, 1).toUpperCase();
        OffsetDateTime createdAt = request.getCreatedAt() != null ? request.getCreatedAt() : OffsetDateTime.now();
        int year = createdAt.getYear() % 100; // last two digits
        int dayOfYear = createdAt.toLocalDate().getDayOfYear();//3 digit indicating the nth day of the year
        String datePart = String.format("%02d%03d", year, dayOfYear); // YYDDD
        ValueOperations<String, String> valueOpsMap = redisTemplate.opsForValue();
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            String randomPart = generateRandomString(randomLength);
            String trackingNumber = origin + destination + datePart + randomPart;
            Boolean success = valueOpsMap.setIfAbsent(trackingNumber, "1");
            if (Boolean.TRUE.equals(success)) {
                redisTemplate.expire(trackingNumber, 30, TimeUnit.DAYS);
                return trackingNumber;
            }
        }
        throw new TrackingNumberGenerationException("Failed to generate a unique tracking number after " + maxRetries + " attempts");
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(idx));
        }
        return sb.toString();
    }
} 
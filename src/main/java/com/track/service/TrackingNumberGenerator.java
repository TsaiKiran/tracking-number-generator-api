package com.track.service;

import com.track.dto.TrackingNumberRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
public class TrackingNumberGenerator {
    private static final String COUNTER_KEY = "tracking-counter:";
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public TrackingNumberGenerator(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateTrackingNumber(TrackingNumberRequest request) {
        //convert offsetdate to yyMMdd format to use in tracker id to reset everyday
        String datePrefix = DateTimeFormatter.ofPattern("yyMMdd")
                .format(request.getCreatedAt() != null ? request.getCreatedAt() : java.time.OffsetDateTime.now());
        
        String counterKey = COUNTER_KEY + datePrefix;
        RedisAtomicLong counter = new RedisAtomicLong(counterKey, redisTemplate.getConnectionFactory());
        
        // Setting expiry for the counter (30 days)
        counter.expire(30, TimeUnit.DAYS);

        long sequence = counter.incrementAndGet();
        
        // Format: CCDDDDDDNNNNNN
        // CC = Country codes (First Letter of each country - origin and dest)
        // DDDDDD = Prefix formatted above (YYMMDD)
        // NNNNNN = 8 digit Sequence number (padded with zeros)
        return String.format("%s%s%s%08d",
                request.getOriginCountryId().substring(0, 1),
                request.getDestinationCountryId().substring(0, 1),
                datePrefix,
                sequence);
    }
} 
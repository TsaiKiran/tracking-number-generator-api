package com.track.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.URI;

@Configuration
public class RedisConfig {
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        String redisUrl = System.getenv("REDIS_URL");
        try {
            URI uri = new URI(redisUrl);
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(uri.getHost());
            config.setPort(uri.getPort());
            
            String userInfo = uri.getUserInfo();
            if (userInfo != null) {
                String[] credentials = userInfo.split(":", 2);
                config.setUsername(credentials[0]);
                if (credentials.length > 1) {
                    config.setPassword(credentials[1]);
                }
            }
            
            return new LettuceConnectionFactory(config);
        } catch (Exception e) {
            logger.error("Redis connection error: " + e.getMessage());
            throw new RuntimeException("Redis connection failed", e);
        }
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }
} 
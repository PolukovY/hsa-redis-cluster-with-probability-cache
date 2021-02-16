package com.levik.redis.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@NoArgsConstructor
@Data
@Component
@ConfigurationProperties(value = "redis")
public class RedisProperties {
    private String host;
    private int port;
    private int defaultTTL;
    private Map<String, Long> cachesTTL;
}

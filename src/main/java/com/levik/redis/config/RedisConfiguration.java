package com.levik.redis.config;

import com.levik.redis.config.properties.RedisProperties;
import com.levik.redis.model.User;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
@Configuration
@EnableRedisRepositories(basePackages = "com.levik.redis")
@EnableCaching
public class RedisConfiguration extends CachingConfigurerSupport {

    private final RedisProperties redisProperties;

    private RedisCacheConfiguration createCacheConfiguration(long timeoutInSeconds) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(timeoutInSeconds));
    }

    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory redisConnectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        if (Objects.nonNull(redisProperties.getCachesTTL())) {
            for (Map.Entry<String, Long> cacheNameAndTimeout : redisProperties.getCachesTTL().entrySet()) {
                cacheConfigurations.put(
                        cacheNameAndTimeout.getKey(),
                        createCacheConfiguration(cacheNameAndTimeout.getValue())
                );
            }
        }
        return RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(createCacheConfiguration(redisProperties.getDefaultTTL()))
                .withInitialCacheConfigurations(cacheConfigurations).build();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisProperties.getHost());
        redisStandaloneConfiguration.setPort(redisProperties.getPort());
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }


    @Bean
    public RedisTemplate<String, User> redisTemplate() {
        RedisTemplate<String, User> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }
}

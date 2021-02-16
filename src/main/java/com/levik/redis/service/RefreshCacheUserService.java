package com.levik.redis.service;

import com.levik.redis.config.properties.RedisProperties;
import com.levik.redis.model.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Timer;

@Slf4j
@AllArgsConstructor
@Service
public class RefreshCacheUserService implements UserApi {

    private static final int DEFAULT_TTL_SEC = 15;

    private final RedisProperties redisProperties;
    private final UserApi userService;

    public User getUserById(String id) {
        return userService.getUserById(id);
    }

    @CachePut(cacheNames = "user", key = "#user.id")
    public void saveOrUpdate(User user) {
        userService.saveOrUpdate(user);
        autoRefreshUserKey(user);
    }


    public void autoRefreshUserKey(User user) {
        long userTTLMillisecond = Math.abs(redisProperties.getCachesTTL().get("user") - DEFAULT_TTL_SEC) * 1000;
        log.info("Start scheduler for {} close ttl {} millisecond now {}", user, userTTLMillisecond, LocalDateTime.now());
        new Timer("refresh-key-user", true).schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        log.info("Trigger update -> {} now {}", user, LocalDateTime.now());
                        saveOrUpdate(user);
                    }
                },
                userTTLMillisecond
        );
    }
}

package com.levik.redis.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value = "user", timeToLive = 3600)
@NoArgsConstructor
@Data
public class User implements Serializable {
    private String id;
}

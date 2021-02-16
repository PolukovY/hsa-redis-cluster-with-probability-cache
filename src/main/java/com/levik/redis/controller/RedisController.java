package com.levik.redis.controller;

import com.levik.redis.model.User;
import com.levik.redis.service.UserApi;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/keys")
public class RedisController {

    private final UserApi refreshCacheUserService;

    @GetMapping
    public User getUserByName(@RequestParam String id) {
        return refreshCacheUserService.getUserById(id);
    }

    @PostMapping
    public void createUserOrUpdate(@RequestBody User user) {
        refreshCacheUserService.saveOrUpdate(user);
    }
}

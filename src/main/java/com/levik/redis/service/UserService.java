package com.levik.redis.service;

import com.levik.redis.model.User;
import com.levik.redis.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class UserService implements UserApi {

    private final UserRepository userRepository;

    @Override
    public User getUserById(String id) {
        log.info("getUserById -> {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Key %s not found", id)));
    }

    @Override
    public void saveOrUpdate(User user) {
        log.info("saveOrUpdate -> {}", user);
        userRepository.save(user);
    }
}

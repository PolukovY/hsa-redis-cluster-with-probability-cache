package com.levik.redis.service;

import com.levik.redis.model.User;

public interface UserApi {

    User getUserById(String id);

    void saveOrUpdate(User user);
}

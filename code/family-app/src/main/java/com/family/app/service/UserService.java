package com.family.app.service;

import com.family.app.entity.User;

import java.util.Optional;

public interface UserService {

    User createUser(String username, String nickname);

    Optional<User> login(String username, String password);
}


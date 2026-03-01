package com.family.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.app.entity.User;
import com.family.app.repository.UserRepository;
import com.family.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(String username, String nickname) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (exists(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setNickname(StringUtils.hasText(nickname) ? nickname : username);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setIsValid(true);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userRepository.insert(user);
        return user;
    }

    @Override
    public Optional<User> login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
                .eq(User::getIsValid, true)
                .last("LIMIT 1");
        User user = userRepository.selectOne(wrapper);
        if (user == null) {
            return Optional.empty();
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    private boolean exists(String username) {
        return userRepository.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0;
    }
}


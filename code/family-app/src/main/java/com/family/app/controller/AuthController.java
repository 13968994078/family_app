package com.family.app.controller;

import com.family.app.common.ApiResponse;
import com.family.app.dto.LoginRequest;
import com.family.app.dto.LoginResponse;
import com.family.app.dto.UserResponse;
import com.family.app.entity.User;
import com.family.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            return ApiResponse.failure(400, "用户名和密码不能为空");
        }
        return userService.login(request.getUsername(), request.getPassword())
                .map(this::buildSuccessResponse)
                .orElse(ApiResponse.failure(401, "用户名或密码错误"));
    }

    private ApiResponse<LoginResponse> buildSuccessResponse(User user) {
        LoginResponse response = LoginResponse.builder()
                .token(UUID.randomUUID().toString())
                .user(UserResponse.from(user))
                .build();
        return ApiResponse.success("登录成功", response);
    }
}


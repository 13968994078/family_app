package com.family.app.controller;

import com.family.app.common.ApiResponse;
import com.family.app.dto.CreateUserRequest;
import com.family.app.dto.UserResponse;
import com.family.app.entity.User;
import com.family.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/createUser")
    public ApiResponse<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            return ApiResponse.failure(400, "用户名不能为空");
        }
        try {
            User created = userService.createUser(request.getUsername(), request.getNickname());
            return ApiResponse.success("生成用户成功（默认密码：123456）", UserResponse.from(created));
        } catch (IllegalArgumentException e) {
            return ApiResponse.failure(400, e.getMessage());
        }
    }
}


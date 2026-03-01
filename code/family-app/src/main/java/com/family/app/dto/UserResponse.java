package com.family.app.dto;

import com.family.app.entity.User;
import lombok.Value;

@Value
public class UserResponse {

    Long id;
    String username;
    String nickname;

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getNickname());
    }
}


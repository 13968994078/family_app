package com.family.app.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponse {

    String token;
    UserResponse user;
}


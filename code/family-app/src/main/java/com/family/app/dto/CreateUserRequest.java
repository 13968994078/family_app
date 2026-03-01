package com.family.app.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class CreateUserRequest {

    private String username;

    private String nickname;
}


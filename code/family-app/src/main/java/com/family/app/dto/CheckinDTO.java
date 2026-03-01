package com.family.app.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CheckinDTO {
    private Long userId;
    private String username; // 可选，前端展示用
    private String type; // "WAKE_UP" 或 "BABY"
    private String babyName; // 仅 baby 时有
    private LocalDateTime checkinTime;
}
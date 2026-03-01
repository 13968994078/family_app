package com.family.app.dto;

import lombok.Data;

// DTO
@Data
public class BabyCheckinRequest {
    private String babyName;
    private String remark;
}

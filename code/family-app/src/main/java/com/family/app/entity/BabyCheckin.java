package com.family.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

// BabyCheckin.java
@Data
@TableName("baby_checkin")
public class BabyCheckin {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String babyName;
    private LocalDateTime checkinTime;
    private LocalDate checkinDate;
    private String remark;
    private Boolean isValid = true;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
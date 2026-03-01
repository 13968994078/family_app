package com.family.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

// WakeUpCheckin.java
@Data
@TableName("wake_up_checkin")
public class WakeUpCheckin {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDateTime checkinTime;
    private LocalDate checkinDate;
    private Boolean isValid = true;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


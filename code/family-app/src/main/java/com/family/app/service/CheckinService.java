package com.family.app.service;

import com.family.app.common.ApiResponse;
import com.family.app.dto.CheckinDTO;
import com.family.app.entity.BabyCheckin;
import com.family.app.entity.WakeUpCheckin;

import java.util.List;
import java.util.Map;

public interface CheckinService {
    ApiResponse<WakeUpCheckin> wakeUpCheckin(CheckinDTO checkinDTO);
    ApiResponse<BabyCheckin> babyCheckin(CheckinDTO checkinDTO);

    // 获取某月打卡情况（userId 为空时返回所有用户）
    ApiResponse<Map<String, List<Map<String, Object>>>> getMonthlyCheckin(int year, int month, Long userId);
}

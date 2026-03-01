package com.family.app.controller;

import com.family.app.common.ApiResponse;
import com.family.app.dto.CheckinDTO;
import com.family.app.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @PostMapping("/wake-up")
    public ApiResponse<?> wakeUpCheckin(@RequestBody CheckinDTO checkinDTO) {
        // 服务层会自动处理 type 字段
        return checkinService.wakeUpCheckin(checkinDTO);
    }

    @PostMapping("/baby")
    public ApiResponse<?> babyCheckin(@RequestBody CheckinDTO checkinDTO) {
        return checkinService.babyCheckin(checkinDTO);
    }

    @GetMapping("/calendar")
    public ApiResponse<Map<String, List<Map<String, Object>>>> calendar(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long userId) {
        return checkinService.getMonthlyCheckin(year, month, userId);
    }
}

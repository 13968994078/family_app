package com.family.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.app.common.ApiResponse;
import com.family.app.dto.CheckinDTO;
import com.family.app.entity.BabyCheckin;
import com.family.app.entity.User;
import com.family.app.entity.WakeUpCheckin;
import com.family.app.repository.BabyCheckinRepository;
import com.family.app.repository.UserRepository;
import com.family.app.repository.WakeUpCheckinRepository;
import com.family.app.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final WakeUpCheckinRepository wakeUpRepo;
    private final BabyCheckinRepository babyRepo;
    private final UserRepository userRepository;

    // 起床打卡
    @Override
    @Transactional
    public ApiResponse<WakeUpCheckin> wakeUpCheckin(CheckinDTO checkinDTO) {
        Long userId = checkinDTO.getUserId();
        if (!isValidUser(userId)) {
            return ApiResponse.failure(400, "用户不存在或已失效");
        }
        if (isTodayChecked(userId, "WAKE_UP")) {
            return ApiResponse.failure(409, "今日已打卡");
        }

        LocalDateTime now = LocalDateTime.now();
        WakeUpCheckin record = new WakeUpCheckin();
        record.setUserId(userId);
        record.setCheckinTime(now);
        record.setCheckinDate(now.toLocalDate());
        record.setIsValid(true);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        wakeUpRepo.insert(record);
        return ApiResponse.success("起床打卡成功", record);
    }

    // 宝宝签到
    @Override
    @Transactional
    public ApiResponse<BabyCheckin> babyCheckin(CheckinDTO checkinDTO) {
        Long userId = checkinDTO.getUserId();
        if (!isValidUser(userId)) {
            return ApiResponse.failure(400, "用户不存在或已失效");
        }
        if (!StringUtils.hasText(checkinDTO.getBabyName())) {
            return ApiResponse.failure(400, "宝宝姓名不能为空");
        }
        String babyName = checkinDTO.getBabyName().trim();
        if (isTodayChecked(userId, "BABY", babyName)) {
            return ApiResponse.failure(409, "宝宝今日已签到");
        }

        LocalDateTime now = LocalDateTime.now();
        BabyCheckin record = new BabyCheckin();
        record.setUserId(userId);
        record.setBabyName(babyName);
        record.setRemark("");
        record.setCheckinTime(now);
        record.setCheckinDate(now.toLocalDate());
        record.setIsValid(true);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        babyRepo.insert(record);
        return ApiResponse.success("宝宝签到成功", record);
    }

    private boolean isTodayChecked(Long userId, String type, String... extra) {
        if (userId == null) {
            return false;
        }
        LocalDate today = LocalDate.now();

        if ("WAKE_UP".equals(type)) {
            return wakeUpRepo.selectCount(
                    new LambdaQueryWrapper<WakeUpCheckin>()
                            .eq(WakeUpCheckin::getUserId, userId)
                            .eq(WakeUpCheckin::getCheckinDate, today)
                            .eq(WakeUpCheckin::getIsValid, true)
            ) > 0;
        } else {
            return babyRepo.selectCount(
                    new LambdaQueryWrapper<BabyCheckin>()
                            .eq(BabyCheckin::getUserId, userId)
                            .eq(BabyCheckin::getBabyName, extra[0])
                            .eq(BabyCheckin::getCheckinDate, today)
                            .eq(BabyCheckin::getIsValid, true)
            ) > 0;
        }
    }

    @Override
    public ApiResponse<Map<String, List<Map<String, Object>>>> getMonthlyCheckin(int year, int month, Long userId) {
        if (month < 1 || month > 12) {
            return ApiResponse.failure(400, "月份范围应为 1-12");
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        LambdaQueryWrapper<WakeUpCheckin> wakeWrapper = new LambdaQueryWrapper<WakeUpCheckin>()
                .eq(WakeUpCheckin::getIsValid, true)
                .ge(WakeUpCheckin::getCheckinDate, startDate)
                .le(WakeUpCheckin::getCheckinDate, endDate)
                .orderByAsc(WakeUpCheckin::getCheckinDate)
                .orderByAsc(WakeUpCheckin::getCheckinTime);
        if (userId != null) {
            wakeWrapper.eq(WakeUpCheckin::getUserId, userId);
        }
        List<WakeUpCheckin> wakeUpRecords = wakeUpRepo.selectList(wakeWrapper);

        LambdaQueryWrapper<BabyCheckin> babyWrapper = new LambdaQueryWrapper<BabyCheckin>()
                .eq(BabyCheckin::getIsValid, true)
                .ge(BabyCheckin::getCheckinDate, startDate)
                .le(BabyCheckin::getCheckinDate, endDate)
                .orderByAsc(BabyCheckin::getCheckinDate)
                .orderByAsc(BabyCheckin::getCheckinTime);
        if (userId != null) {
            babyWrapper.eq(BabyCheckin::getUserId, userId);
        }
        List<BabyCheckin> babyRecords = babyRepo.selectList(babyWrapper);

        Map<Long, User> userMap = buildUserMap(wakeUpRecords, babyRecords);
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();

        for (WakeUpCheckin record : wakeUpRecords) {
            appendEntry(result, record.getCheckinDate().toString(), buildWakeEntry(record, userMap.get(record.getUserId())));
        }
        for (BabyCheckin record : babyRecords) {
            appendEntry(result, record.getCheckinDate().toString(), buildBabyEntry(record, userMap.get(record.getUserId())));
        }

        return ApiResponse.success(result);
    }

    private boolean isValidUser(Long userId) {
        if (userId == null) {
            return false;
        }
        User user = userRepository.selectById(userId);
        return user != null && Boolean.TRUE.equals(user.getIsValid());
    }

    private Map<Long, User> buildUserMap(List<WakeUpCheckin> wakeUpRecords, List<BabyCheckin> babyRecords) {
        Set<Long> userIds = new HashSet<>();
        for (WakeUpCheckin record : wakeUpRecords) {
            userIds.add(record.getUserId());
        }
        for (BabyCheckin record : babyRecords) {
            userIds.add(record.getUserId());
        }
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> users = userRepository.selectBatchIds(userIds);
        Map<Long, User> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getId(), user);
        }
        return userMap;
    }

    private void appendEntry(Map<String, List<Map<String, Object>>> result, String date, Map<String, Object> entry) {
        result.computeIfAbsent(date, ignored -> new ArrayList<>()).add(entry);
    }

    private Map<String, Object> buildWakeEntry(WakeUpCheckin record, User user) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("type", "WAKE_UP");
        entry.put("userId", record.getUserId());
        entry.put("username", resolveUsername(user, record.getUserId()));
        entry.put("nickname", resolveNickname(user));
        entry.put("time", record.getCheckinTime().format(TIME_FORMATTER));
        return entry;
    }

    private Map<String, Object> buildBabyEntry(BabyCheckin record, User user) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("type", "BABY");
        entry.put("userId", record.getUserId());
        entry.put("username", resolveUsername(user, record.getUserId()));
        entry.put("nickname", resolveNickname(user));
        entry.put("babyName", record.getBabyName());
        entry.put("time", record.getCheckinTime().format(TIME_FORMATTER));
        return entry;
    }

    private String resolveUsername(User user, Long userId) {
        if (user != null && StringUtils.hasText(user.getUsername())) {
            return user.getUsername();
        }
        return "user-" + userId;
    }

    private String resolveNickname(User user) {
        if (user != null && StringUtils.hasText(user.getNickname())) {
            return user.getNickname();
        }
        return "";
    }
}

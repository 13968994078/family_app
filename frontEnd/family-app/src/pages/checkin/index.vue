<template>
  <view class="container">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

    <view class="navbar">
      <text class="nav-title">签到</text>
    </view>

    <view class="content">
      <view class="checkin-section">
        <view class="checkin-card">
          <text class="checkin-title">起床签到</text>
          <text class="checkin-desc">记录每日起床时间，养成良好习惯</text>
          <button
            class="checkin-btn"
            :class="{ checked: wakeUpChecked }"
            :loading="wakeUpLoading"
            @click="handleWakeUpCheckin"
          >
            {{ wakeUpChecked ? '今日已签到' : '起床签到' }}
          </button>
          <text v-if="wakeUpCheckinTime" class="checkin-time">签到时间：{{ wakeUpCheckinTime }}</text>
        </view>

        <view class="checkin-card">
          <text class="checkin-title">宝宝签到</text>
          <text class="checkin-desc">记录宝宝日常，关爱屁屁健康</text>
          <button
            class="checkin-btn"
            :class="{ checked: babyChecked }"
            :loading="babyLoading"
            @click="handleBabyCheckin"
          >
            {{ babyChecked ? '今日已签到' : '宝宝签到' }}
          </button>
          <text v-if="babyCheckinTime" class="checkin-time">签到时间：{{ babyCheckinTime }}</text>
        </view>
      </view>

      <view class="calendar-section">
        <view class="calendar-header">
          <text class="calendar-title">签到日历</text>
        </view>

        <view class="month-nav">
          <button class="month-btn" @click="goPrevMonth">上个月</button>
          <text class="month-label">{{ currentMonthLabel }}</text>
          <button class="month-btn" @click="goNextMonth">下个月</button>
        </view>

        <view class="week-row">
          <text v-for="week in weekLabels" :key="week" class="week-item">{{ week }}</text>
        </view>

        <view class="calendar-grid">
          <view
            v-for="cell in monthCells"
            :key="cell.dateKey"
            class="day-cell"
            :class="{
              'not-current': !cell.inCurrentMonth,
              selected: selectedDate === cell.dateKey,
              today: isToday(cell.dateKey)
            }"
            @click="selectDay(cell)"
          >
            <text class="day-num">{{ cell.day }}</text>
            <view v-if="hasDateMark(cell.dateKey)" class="dot"></view>
          </view>
        </view>
      </view>

      <view class="detail-section">
        <text class="detail-title">{{ selectedDateLabel }}签到记录</text>
        <view v-if="selectedDateEntries.length === 0" class="empty-record">当天暂无签到记录</view>
        <view v-else class="record-list">
          <view v-for="(item, index) in selectedDateEntries" :key="`${item.userId}-${item.type}-${index}`" class="record-item">
            <view class="record-main">
              <text class="record-user">{{ getDisplayName(item) }}</text>
              <text class="record-type">{{ getTypeLabel(item) }}</text>
            </view>
            <text class="record-time">{{ item.time }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { babyCheckin, getCheckinCalendar, wakeUpCheckin, type CheckinCalendar, type CheckinItem } from '@/api';

interface CalendarCell {
  dateKey: string;
  day: number;
  inCurrentMonth: boolean;
}

const weekLabels = ['日', '一', '二', '三', '四', '五', '六'];

const statusBarHeight = ref(0);
const wakeUpChecked = ref(false);
const babyChecked = ref(false);
const wakeUpLoading = ref(false);
const babyLoading = ref(false);
const wakeUpCheckinTime = ref('');
const babyCheckinTime = ref('');

const now = new Date();
const currentYear = ref(now.getFullYear());
const currentMonth = ref(now.getMonth() + 1);
const selectedDate = ref('');
const calendarMap = ref<CheckinCalendar>({});

const userInfo = uni.getStorageSync('userInfo');
const currentUserId = userInfo?.id ? Number(userInfo.id) : null;

const pad2 = (num: number) => String(num).padStart(2, '0');

const getDateKey = (year: number, month: number, day: number) => `${year}-${pad2(month)}-${pad2(day)}`;

const getLocalDateKey = () => {
  const d = new Date();
  return getDateKey(d.getFullYear(), d.getMonth() + 1, d.getDate());
};

const extractTime = (value?: string) => {
  if (!value) {
    return '';
  }
  if (value.includes('T')) {
    return value.split('T')[1].slice(0, 8);
  }
  if (value.includes(' ')) {
    return value.split(' ')[1].slice(0, 8);
  }
  return value.slice(0, 8);
};

const currentMonthLabel = computed(() => `${currentYear.value}年${currentMonth.value}月`);

const monthCells = computed<CalendarCell[]>(() => {
  const year = currentYear.value;
  const month = currentMonth.value;

  const firstDay = new Date(year, month - 1, 1);
  const startOffset = firstDay.getDay();
  const daysInCurrent = new Date(year, month, 0).getDate();

  const prevYear = month === 1 ? year - 1 : year;
  const prevMonth = month === 1 ? 12 : month - 1;
  const daysInPrev = new Date(prevYear, prevMonth, 0).getDate();

  const cells: CalendarCell[] = [];

  for (let i = 0; i < startOffset; i += 1) {
    const day = daysInPrev - startOffset + i + 1;
    cells.push({
      day,
      dateKey: getDateKey(prevYear, prevMonth, day),
      inCurrentMonth: false
    });
  }

  for (let day = 1; day <= daysInCurrent; day += 1) {
    cells.push({
      day,
      dateKey: getDateKey(year, month, day),
      inCurrentMonth: true
    });
  }

  let nextDay = 1;
  const nextYear = month === 12 ? year + 1 : year;
  const nextMonth = month === 12 ? 1 : month + 1;
  while (cells.length < 42) {
    cells.push({
      day: nextDay,
      dateKey: getDateKey(nextYear, nextMonth, nextDay),
      inCurrentMonth: false
    });
    nextDay += 1;
  }

  return cells;
});

const selectedDateEntries = computed(() => {
  const items = calendarMap.value[selectedDate.value] || [];
  return [...items].sort((a, b) => a.time.localeCompare(b.time));
});

const selectedDateLabel = computed(() => {
  if (!selectedDate.value) {
    return '当日';
  }
  const [year, month, day] = selectedDate.value.split('-');
  return `${year}年${month}月${day}日`;
});

const hasDateMark = (dateKey: string) => (calendarMap.value[dateKey] || []).length > 0;

const isToday = (dateKey: string) => dateKey === getLocalDateKey();

const getDisplayName = (item: CheckinItem) => item.nickname || item.username || `用户${item.userId}`;

const getTypeLabel = (item: CheckinItem) => {
  if (item.type === 'WAKE_UP') {
    return '起床签到';
  }
  if (item.babyName) {
    return `宝宝签到（${item.babyName}）`;
  }
  return '宝宝签到';
};

const syncTodayStatus = () => {
  if (!currentUserId) {
    return;
  }
  const today = getLocalDateKey();
  const todayEntries = calendarMap.value[today] || [];
  const ownEntries = todayEntries.filter((item) => item.userId === currentUserId);

  const wake = ownEntries.find((item) => item.type === 'WAKE_UP');
  const baby = ownEntries.find((item) => item.type === 'BABY');

  wakeUpChecked.value = Boolean(wake);
  babyChecked.value = Boolean(baby);
  wakeUpCheckinTime.value = wake?.time || '';
  babyCheckinTime.value = baby?.time || '';
};

const ensureSelectedDateInCurrentMonth = () => {
  const monthPrefix = `${currentYear.value}-${pad2(currentMonth.value)}-`;
  if (selectedDate.value.startsWith(monthPrefix)) {
    return;
  }

  const today = getLocalDateKey();
  if (today.startsWith(monthPrefix)) {
    selectedDate.value = today;
  } else {
    selectedDate.value = getDateKey(currentYear.value, currentMonth.value, 1);
  }
};

const loadMonthCheckin = async () => {
  try {
    const result = await getCheckinCalendar({
      year: currentYear.value,
      month: currentMonth.value
    });
    calendarMap.value = result || {};
    ensureSelectedDateInCurrentMonth();
    syncTodayStatus();
  } catch (error: any) {
    uni.showToast({ title: error?.message || '获取签到日历失败', icon: 'none' });
  }
};

const shiftMonth = async (offset: number) => {
  let month = currentMonth.value + offset;
  let year = currentYear.value;

  if (month < 1) {
    month = 12;
    year -= 1;
  } else if (month > 12) {
    month = 1;
    year += 1;
  }

  currentYear.value = year;
  currentMonth.value = month;
  await loadMonthCheckin();
};

const goPrevMonth = async () => {
  await shiftMonth(-1);
};

const goNextMonth = async () => {
  await shiftMonth(1);
};

const selectDay = async (cell: CalendarCell) => {
  if (cell.inCurrentMonth) {
    selectedDate.value = cell.dateKey;
    return;
  }

  const [yearText, monthText] = cell.dateKey.split('-');
  currentYear.value = Number(yearText);
  currentMonth.value = Number(monthText);
  selectedDate.value = cell.dateKey;
  await loadMonthCheckin();
};

const handleWakeUpCheckin = async () => {
  if (!currentUserId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    uni.navigateTo({ url: '/pages/login/index' });
    return;
  }

  if (wakeUpChecked.value) {
    uni.showToast({ title: wakeUpCheckinTime.value ? `今日已签到 ${wakeUpCheckinTime.value}` : '今日已签到', icon: 'none' });
    return;
  }

  wakeUpLoading.value = true;
  try {
    const record = await wakeUpCheckin({ userId: currentUserId, type: 'WAKE_UP' });
    wakeUpCheckinTime.value = extractTime(record.checkinTime);
    wakeUpChecked.value = true;
    uni.showToast({ title: `签到成功 ${wakeUpCheckinTime.value}`, icon: 'success' });

    selectedDate.value = getLocalDateKey();
    await loadMonthCheckin();
  } catch (error: any) {
    uni.showToast({ title: error?.message || '签到失败', icon: 'none' });
  } finally {
    wakeUpLoading.value = false;
  }
};

const handleBabyCheckin = async () => {
  if (!currentUserId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    uni.navigateTo({ url: '/pages/login/index' });
    return;
  }

  if (babyChecked.value) {
    uni.showToast({ title: babyCheckinTime.value ? `今日已签到 ${babyCheckinTime.value}` : '今日已签到', icon: 'none' });
    return;
  }

  babyLoading.value = true;
  try {
    const record = await babyCheckin({
      userId: currentUserId,
      babyName: '小宝',
      remark: '',
      type: 'BABY'
    });
    babyCheckinTime.value = extractTime(record.checkinTime);
    babyChecked.value = true;
    uni.showToast({ title: `签到成功 ${babyCheckinTime.value}`, icon: 'success' });

    selectedDate.value = getLocalDateKey();
    await loadMonthCheckin();
  } catch (error: any) {
    uni.showToast({ title: error?.message || '签到失败', icon: 'none' });
  } finally {
    babyLoading.value = false;
  }
};

onMounted(async () => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;

  if (!currentUserId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    uni.navigateTo({ url: '/pages/login/index' });
    return;
  }

  selectedDate.value = getLocalDateKey();
  await loadMonthCheckin();
});
</script>

<style scoped lang="scss">
.container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
  padding-bottom: env(safe-area-inset-bottom);
}

.status-bar {
  width: 100%;
  background: transparent;
}

.navbar {
  display: flex;
  align-items: center;
  padding: 20rpx 32rpx;
  background: #fff;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.nav-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1a1a1a;
}

.content {
  padding: 24rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.checkin-section {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.checkin-card,
.calendar-section,
.detail-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.checkin-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a1a;
}

.checkin-desc {
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #666;
}

.checkin-btn {
  margin-top: 16rpx;
  width: 220rpx;
  height: 68rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 500;
  color: #fff;
  border: none;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.checkin-btn.checked {
  background: #a0a0a0;
}

.checkin-time {
  display: block;
  margin-top: 10rpx;
  color: #4a5568;
  font-size: 24rpx;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.calendar-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a1a;
}

.month-nav {
  margin-top: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.month-btn {
  width: 150rpx;
  height: 56rpx;
  font-size: 24rpx;
  border-radius: 12rpx;
  background: #f2f6ff;
  color: #3c9cff;
  border: none;
}

.month-label {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
}

.week-row {
  margin-top: 20rpx;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.week-item {
  text-align: center;
  font-size: 24rpx;
  color: #6b7280;
}

.calendar-grid {
  margin-top: 12rpx;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8rpx;
}

.day-cell {
  height: 78rpx;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.day-num {
  font-size: 24rpx;
  color: #111827;
}

.day-cell.not-current .day-num {
  color: #c0c4cc;
}

.day-cell.selected {
  background: #eef6ff;
}

.day-cell.today {
  border: 2rpx solid #3c9cff;
}

.dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #ff4d4f;
  margin-top: 4rpx;
}

.detail-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
}

.empty-record {
  margin-top: 18rpx;
  font-size: 24rpx;
  color: #9ca3af;
}

.record-list {
  margin-top: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.record-item {
  border-radius: 12rpx;
  background: #f8fafc;
  padding: 16rpx;
}

.record-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.record-user {
  font-size: 26rpx;
  font-weight: 600;
  color: #1f2937;
}

.record-type {
  font-size: 22rpx;
  color: #2563eb;
}

.record-time {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #4b5563;
}
</style>
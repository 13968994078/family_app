<template>
  <view class="container">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

    <view class="navbar">
      <view class="nav-left"></view>
      <view class="nav-center">
        <text class="nav-title">吃什么</text>
      </view>
      <view class="nav-right"></view>
    </view>

    <view class="content">
      <view class="region-card">
        <text class="region-label">地区筛选</text>
        <input
          v-model.trim="regionKeyword"
          class="region-input"
          placeholder="输入地区，如：川渝 / 广东 / 东北"
        />
        <text class="region-tip">会在菜名、类别、描述中匹配地区关键词</text>
        <button class="location-btn" @click="onLocateSearch" :loading="locating">
          {{ locating ? '定位中…' : '定位并联网搜本地美食' }}
        </button>
      </view>

      <view v-if="cityFoods.length > 0" class="city-food-card">
        <text class="city-food-title">当前定位：{{ currentCity }}</text>
        <view class="city-food-list">
          <text
            v-for="food in cityFoods"
            :key="food"
            class="city-food-item"
            @click="applyDishFilter(food)"
          >
            {{ food }}
          </text>
        </view>
        <text class="city-food-tip">点任意菜名可直接用于转盘筛选</text>
      </view>

      <view class="recommend-card">
        <view class="food-badge">{{ currentFood?.type || '美食推荐' }}</view>
        <text class="food-title" :class="{ rolling: spinning }">
          {{ spinning ? rollingName : currentFood?.name || '今天吃什么？' }}
        </text>
        <text class="food-desc">
          {{ spinning ? '转盘正在挑选中…' : (currentFood?.description || '点击开始转盘，让妙妙屋来帮你决定。') }}
        </text>
      </view>

      <view class="action-buttons">
        <button class="action-btn shuffle-btn" @click="onShuffle" :loading="loading" :disabled="spinning">
          <text class="btn-text">{{ spinning ? '转盘中…' : '开始转盘' }}</text>
        </button>
        <button class="action-btn add-btn" @click="showCreator = true">
          <text class="btn-text">添加菜品</text>
        </button>
        <button class="action-btn list-btn" @click="goToList">
          <text class="btn-text">菜品列表</text>
        </button>
      </view>
    </view>

    <view v-if="showCreator" class="creator-overlay" @click.self="closeCreator">
      <view class="creator-modal">
        <view class="modal-header">
          <text class="modal-title">添加菜品</text>
          <text class="close-btn" @click="closeCreator">×</text>
        </view>

        <view class="form-group">
          <view class="input-item">
            <text class="input-label">菜品名称</text>
            <input v-model.trim="form.name" placeholder="例如：番茄牛腩" class="input-field" />
          </view>

          <view class="input-item">
            <text class="input-label">类别</text>
            <input v-model.trim="form.type" placeholder="例如：川菜 / 家常菜" class="input-field" />
          </view>

          <view class="input-item">
            <text class="input-label">描述（可选）</text>
            <textarea
              v-model="form.description"
              placeholder="可写地区/口味，如：广东清淡、湖南香辣"
              class="textarea-field"
              :auto-height="true"
            ></textarea>
          </view>
        </view>

        <view class="modal-actions">
          <button class="modal-btn cancel-btn" @click="closeCreator">取消</button>
          <button class="modal-btn submit-btn" @click="onSubmit" :loading="submitting">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import { createFood, fetchAllFoods, fetchCityFoodRecommend, fetchRandomFood, type Food } from '@/api';

const statusBarHeight = ref(0);
const currentFood = ref<Food | null>(null);
const loading = ref(false);
const submitting = ref(false);
const showCreator = ref(false);
const spinning = ref(false);
const rollingName = ref('');
const regionKeyword = ref('');
const foodPool = ref<Food[]>([]);
const locating = ref(false);
const currentCity = ref('');
const cityFoods = ref<string[]>([]);

const form = reactive({
  name: '',
  type: '',
  description: ''
});

let spinTimer: ReturnType<typeof setInterval> | null = null;

const sleep = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms));

const normalize = (value: string) => value.toLowerCase().replace(/\s+/g, '');

const randomPick = (foods: Food[]) => foods[Math.floor(Math.random() * foods.length)];

const stopSpinTimer = () => {
  if (spinTimer) {
    clearInterval(spinTimer);
    spinTimer = null;
  }
};

const loadFoodPool = async () => {
  try {
    const foods = await fetchAllFoods();
    foodPool.value = foods || [];
  } catch {
    foodPool.value = [];
  }
};

const initRecommendFood = async () => {
  await loadFoodPool();

  if (foodPool.value.length > 0) {
    currentFood.value = randomPick(foodPool.value);
    return;
  }

  try {
    currentFood.value = await fetchRandomFood();
  } catch {
    currentFood.value = null;
  }
};

const getCandidatesByRegion = () => {
  const keyword = normalize(regionKeyword.value);
  if (!keyword) {
    return foodPool.value;
  }

  return foodPool.value.filter((food) => {
    const haystack = normalize(`${food.name} ${food.type} ${food.description || ''}`);
    return haystack.includes(keyword);
  });
};

const runRoulette = async (candidates: Food[]) => {
  spinning.value = true;
  loading.value = true;

  rollingName.value = randomPick(candidates).name;
  spinTimer = setInterval(() => {
    rollingName.value = randomPick(candidates).name;
  }, 110);

  await sleep(1800);
  stopSpinTimer();

  const winner = randomPick(candidates);
  currentFood.value = winner;
  rollingName.value = winner.name;

  await sleep(220);
  spinning.value = false;
  loading.value = false;
};

const onShuffle = async () => {
  if (spinning.value) {
    return;
  }

  if (foodPool.value.length === 0) {
    await loadFoodPool();
  }

  const candidates = getCandidatesByRegion();
  if (candidates.length === 0) {
    uni.showToast({
      title: regionKeyword.value ? '该地区暂无匹配菜品' : '暂时没有菜品',
      icon: 'none'
    });
    return;
  }

  try {
    await runRoulette(candidates);
  } catch {
    stopSpinTimer();
    spinning.value = false;
    loading.value = false;
    uni.showToast({ title: '推荐失败，请重试', icon: 'none' });
  }
};

const getLocation = () =>
  new Promise<any>((resolve, reject) => {
    uni.getLocation({
      type: 'gcj02',
      success: resolve,
      fail: reject
    });
  });

const onLocateSearch = async () => {
  if (locating.value) {
    return;
  }
  locating.value = true;
  try {
    const location = await getLocation();
    const result = await fetchCityFoodRecommend({
      latitude: location.latitude,
      longitude: location.longitude
    });
    currentCity.value = result.city || '';
    cityFoods.value = result.foods || [];
    if (currentCity.value) {
      regionKeyword.value = currentCity.value.replace('市', '');
    }
    if (cityFoods.value.length === 0) {
      uni.showToast({ title: '未检索到城市美食', icon: 'none' });
    } else {
      uni.showToast({ title: `已获取${currentCity.value}美食`, icon: 'success' });
    }
  } catch (error: any) {
    const message = error?.message || '定位或联网搜索失败';
    uni.showToast({ title: message, icon: 'none' });
  } finally {
    locating.value = false;
  }
};

const applyDishFilter = (dish: string) => {
  regionKeyword.value = dish;
};

const goToList = () => {
  uni.navigateTo({ url: '/pages/food/list' });
};

const resetForm = () => {
  form.name = '';
  form.type = '';
  form.description = '';
};

const closeCreator = () => {
  showCreator.value = false;
  resetForm();
};

const onSubmit = async () => {
  if (!form.name || !form.type) {
    uni.showToast({ title: '请填写名称和类别', icon: 'none' });
    return;
  }

  submitting.value = true;
  try {
    const created = await createFood({ ...form });
    uni.showToast({ title: '添加成功', icon: 'success' });
    closeCreator();
    await loadFoodPool();
    currentFood.value = created;
  } catch {
    uni.showToast({ title: '添加失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;

  await initRecommendFood();
});

onUnmounted(() => {
  stopSpinTimer();
});
</script>

<style scoped lang="scss">
.container {
  min-height: 100vh;
  background: #f9fafc;
  padding-bottom: env(safe-area-inset-bottom);
}

.status-bar {
  width: 100%;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  background: #ffffff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.nav-center {
  flex: 1;
  text-align: center;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2937;
}

.content {
  padding: 30rpx 32rpx 80rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.region-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.region-label {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
}

.region-input {
  margin-top: 14rpx;
  height: 76rpx;
  border-radius: 14rpx;
  border: 2rpx solid #dbe4ff;
  padding: 0 22rpx;
  background: #f8faff;
  font-size: 28rpx;
  color: #111827;
}

.region-tip {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #6b7280;
}

.location-btn {
  margin-top: 16rpx;
  height: 68rpx;
  border-radius: 14rpx;
  border: none;
  color: #fff;
  background: linear-gradient(135deg, #10b981 0%, #14b8a6 100%);
  font-size: 26rpx;
  font-weight: 600;
}

.city-food-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.city-food-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
}

.city-food-list {
  margin-top: 14rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.city-food-item {
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  color: #ef4444;
  background: #fff1f2;
}

.city-food-tip {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #6b7280;
}

.recommend-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 56rpx 48rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
  text-align: center;
}

.food-badge {
  display: inline-block;
  padding: 8rpx 24rpx;
  border-radius: 100rpx;
  background: #eef7ff;
  color: #4a90e2;
  font-size: 24rpx;
  font-weight: 600;
  margin-bottom: 24rpx;
}

.food-title {
  display: block;
  font-size: 48rpx;
  font-weight: 800;
  color: #111827;
  line-height: 1.2;
  margin: 0 0 20rpx;
}

.food-title.rolling {
  animation: pulse 0.5s ease-in-out infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.04);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0.7;
  }
}

.food-desc {
  display: block;
  font-size: 28rpx;
  color: #6b7280;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 20rpx;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
  color: #fff;
}

.shuffle-btn {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
}

.add-btn {
  background: linear-gradient(135deg, #ec4899 0%, #f43f5e 100%);
}

.list-btn {
  background: linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%);
}

.creator-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: flex-end;
  z-index: 1000;
  padding: 0 32rpx 40rpx;
}

.creator-modal {
  background: #ffffff;
  border-radius: 28rpx;
  width: 100%;
  max-width: 680rpx;
  padding: 40rpx;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}

.modal-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #1f2937;
}

.close-btn {
  font-size: 40rpx;
  color: #9ca3af;
}

.input-label {
  display: block;
  font-size: 28rpx;
  color: #374151;
  margin-bottom: 12rpx;
  font-weight: 600;
}

.input-field,
.textarea-field {
  width: 100%;
  border: 2rpx solid #e5e7eb;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  color: #1f2937;
  background: #ffffff;
  box-sizing: border-box;
}

.input-field {
  height: 80rpx;
}

.textarea-field {
  min-height: 120rpx;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.modal-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 20rpx;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
}

.cancel-btn {
  background: #f9fafb;
  color: #6b7280;
}

.submit-btn {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
}
</style>

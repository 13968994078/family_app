<template>
  <view class="container">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @click="goback()">
        ←
      </view>
      <text class="nav-title">菜品列表</text>
      <view class="nav-right"></view>
    </view>

    <!-- 列表区域 -->
    <view class="list-content">
      <view v-if="foods.length === 0" class="empty-tip">
        暂无菜品，快去添加吧！
      </view>

      <view v-for="food in foods" :key="food.id" class="food-item">
        <view class="food-header">
          <text class="food-name">{{ food.name }}</text>
          <text class="food-type">{{ food.type }}</text>
        </view>
        <text v-if="food.description" class="food-desc">{{ food.description }}</text>
        
        <!-- 可选：删除按钮（需权限控制） -->
        
        <view class="food-actions">
          <button class="delete-btn" @click="deleteFood(food.id)">删除</button>
        </view>
       
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { fetchAllFoods } from '@/api'; // 需要新增 API

const statusBarHeight = ref(0);
const foods = ref<any[]>([]);

onMounted(async () => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;

  await loadFoods();
});

const goback = () => {
	uni.switchTab({ url: '/pages/food/index' });
}

const loadFoods = async () => {
  try {
    const data = await fetchAllFoods();
    foods.value = data || [];
  } catch (error) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  }
};

// 可选：删除功能
const deleteFood = async (_id: number) => {
  uni.showToast({ title: '删除功能暂未开放', icon: 'none' });
};
</script>

<style scoped lang="scss">
.container {
  min-height: 100vh;
  background: #f8fafd;
}

.status-bar {
  width: 100%;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 32rpx;
  background: #fff;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.08);
  font-size: 32rpx;
  font-weight: bold;
}

.nav-left, .nav-right {
  width: 80rpx;
  text-align: center;
  color: #4a90e2;
}

.list-content {
  padding: 20rpx 32rpx;
}

.empty-tip {
  text-align: center;
  color: #999;
  font-size: 28rpx;
  margin-top: 100rpx;
}

.food-item {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.food-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.food-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a1a;
}

.food-type {
  font-size: 24rpx;
  color: #4a90e2;
  background: #ecf5ff;
  padding: 4rpx 16rpx;
  border-radius: 50rpx;
}

.food-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

.food-actions {
  margin-top: 16rpx;
  text-align: right;
}

.delete-btn {
  background: #ff4d4f;
  color: white;
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 12rpx;
  height: auto;
}
</style>
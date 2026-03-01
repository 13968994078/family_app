<template>
  <view class="container">
    <!-- 个人信息区域 -->
    <view class="profile-section">
      <image class="avatar" src="@/static/sk.png" mode="aspectFill" />
      <view class="profile-info">
        <text class="name">{{ user.nickname || '访客' }}</text>
        <text class="meta">账号：{{ user.username || '未登录' }}</text>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="function-section">
      <view class="function-item" @click="handleItemClick('settings')">
        <view class="item-left">
          <text class="item-icon">⚙️</text>
          <text class="item-label">设置</text>
        </view>
        <text class="item-arrow">›</text>
      </view>
      
      <view class="function-item" @click="handleItemClick('about')">
        <view class="item-left">
          <text class="item-icon">ℹ️</text>
          <text class="item-label">关于我们</text>
        </view>
        <text class="item-arrow">›</text>
      </view>
      
      <view class="function-item" @click="handleItemClick('help')">
        <view class="item-left">
          <text class="item-icon">❓</text>
          <text class="item-label">帮助中心</text>
        </view>
        <text class="item-arrow">›</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-section">
      <!-- <button class="action-btn refresh-btn" @click="refresh">刷新</button> -->
      <button class="action-btn logout-btn" @click="logout">退出登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive } from 'vue';

interface UserStore {
  nickname: string;
  username: string;
  avatar?: string;
}

const user = reactive<UserStore>({
  nickname: '访客',
  username: 'visitor'
});

const loadUser = () => {
  const cache = uni.getStorageSync('userInfo');
  if (cache) {
    const data = typeof cache === 'string' ? JSON.parse(cache) : cache;
    Object.assign(user, data);
  }
};

const refresh = () => {
  loadUser();
  uni.showToast({ title: '已刷新', icon: 'none' });
};

const logout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token');
        uni.removeStorageSync('userInfo');
        uni.redirectTo({ url: '/pages/login/index' });
      }
    }
  });
};

const handleItemClick = (type: string) => {
	uni.showToast({ title: '暂未实现', icon: 'none' });
  // switch(type) {
  //   case 'settings':
  //     uni.navigateTo({ url: '/pages/settings/index' });
  //     break;
  //   case 'about':
  //     uni.navigateTo({ url: '/pages/about/index' });
  //     break;
  //   case 'help':
  //     uni.navigateTo({ url: '/pages/help/index' });
  //     break;
  // }
};

loadUser();
</script>

<style scoped lang="scss">
.container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f4ff 0%, #e6f0ff 100%);
  padding: 32rpx;
  box-sizing: border-box;
}

.profile-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
  margin-bottom: 32rpx;
  position: relative;
  overflow: hidden;
}

// .profile-section::before {
//   content: '';
//   position: absolute;
//   top: 0;
//   right: 0;
//   width: 120rpx;
//   height: 120rpx;
//   background: linear-gradient(135deg, #4a90e2 0%, #5e72e4 100%);
//   border-radius: 0 24rpx 0 24rpx;
//   opacity: 0.1;
// }

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  border: 4rpx solid #e6f0ff;
  position: relative;
  z-index: 1;
}

.profile-info {
  flex: 1;
  position: relative;
  z-index: 1;
}

.name {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8rpx;
}

.meta {
  font-size: 26rpx;
  color: #666;
}

.function-section {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
  margin-bottom: 32rpx;
  overflow: hidden;
}

.function-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f4ff;
}

.function-item:last-child {
  border-bottom: none;
}

.function-item:active {
  background-color: #f8fafd;
}

.item-left {
  display: flex;
  align-items: center;
}

.item-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.item-label {
  font-size: 30rpx;
  color: #1a1a1a;
  font-weight: 500;
}

.item-arrow {
  font-size: 32rpx;
  color: #c0c8d5;
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.action-btn {
  height: 80rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.action-btn:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
}

.refresh-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.logout-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
}
</style>




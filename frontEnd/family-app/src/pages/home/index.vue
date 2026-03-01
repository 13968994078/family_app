<template>
  <view class="container">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
    
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="nav-left">
        <text class="greeting">{{ greeting }}，{{ user.username }}</text>
      </view>
      <view class="nav-right">
        <view class="user-avatar" @click="handleProfile">
          <image src="@/static/sk.png" mode="aspectFill" class="avatar-img"></image>
        </view>
      </view>
    </view>

    <!-- 主要功能卡片 -->
    <view class="main-section">
      <view class="section-header">
        <text class="section-title">家庭服务</text>
      </view>
      
      <view class="service-cards">
        <view 
          class="service-card" 
          v-for="(service, index) in services" 
          :key="index"
          @click="handleService(service)"
        >
          <view class="card-icon-large" :style="{ backgroundColor: service.color }">
            <image src="@/static/icon/tp.png" class="service-icon" mode="aspectFit"></image>
          </view>
          <view class="card-content">
            <text class="card-title">{{ service.name }}</text>
            <text class="card-desc">{{ service.desc }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部安全区域 -->
    <view class="safe-area-bottom"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted,reactive } from 'vue';
import { onShow } from '@dcloudio/uni-app';

// 状态栏高度
const statusBarHeight = ref(0);

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

// 数据
const services = ref([
  { 
    name: '起床签到', 
    desc: '记录起床时间，养成良好习惯', 
    icon: '@/static/icon/tp.png',
    color: '#FF9A8B' 
  },
  { 
    name: '哦粑签到', 
    desc: '记录宝宝日常，关爱屁屁健康',
    icon: '@/static/icon/hh.png',
    color: '#667eea' 
  },
  { 
    name: '今天吃什么', 
    desc: '随机推荐美食，解决选择困难', 
    icon: '@/static/icon/qie.png',
    color: '#4facfe' 
  },
  { 
    name: '日程共享', 
    desc: '分享你的日程，记录美好生活', 
    icon: '@/static/icon/lyk.png',
    color: '#43e97b' 
  }
]);

// 获取问候语
const greeting = ref('早上好');

const getGreeting = () => {
  const hour = new Date().getHours();
  if (hour < 6) return '凌晨好';
  if (hour < 9) return '早上好';
  if (hour < 12) return '上午好';
  if (hour < 14) return '中午好';
  if (hour < 17) return '下午好';
  if (hour < 19) return '傍晚好';
  return '晚上好';
};

// 检查登录
function checkAuth() {
  const token = uni.getStorageSync('token');
  if (!token) {
    uni.redirectTo({ url: '/pages/login/index' });
  }
}

onShow(() => {
  checkAuth();
});

onMounted(() => {
  // 获取状态栏高度
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
  
  // 设置问候语
  greeting.value = getGreeting();
});

// 事件处理
const handleProfile = () => {
  console.log('点击头像');
};

const handleService = (service: any) => {
  console.log('点击服务:', service.name);
  // 根据服务类型跳转到对应页面
  switch(service.name) {
    case '起床签到':
      uni.switchTab({ url: '/pages/checkin/index' });
      break;
    case '哦粑签到':
      uni.switchTab({ url: '/pages/checkin/index' });
      break;
    case '今天吃什么':
      uni.switchTab({ url: '/pages/food/index' });
      break;
    case '日程共享':
      uni.showToast({ title: '功能开发中，敬请期待', icon: 'none' });
      break;
  }
};

loadUser();
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
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 32rpx 20rpx;
  background: #fff;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 100;
}

.nav-left {
  flex: 1;
}

.greeting {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a1a;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.user-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 2rpx solid #e0e0e0;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.main-section {
  margin: 0 32rpx 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1a1a1a;
}

.service-cards {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.service-card {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.service-card:last-child {
  border-bottom: none;
}

.service-card:active {
  background-color: #f8f9fa;
}

.card-icon-large {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.service-icon {
  width: 32rpx;
  height: 32rpx;
}

.card-content {
  flex: 1;
}

.card-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8rpx;
  line-height: 1.3;
}

.card-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.4;
}

.safe-area-bottom {
  height: env(safe-area-inset-bottom);
}
</style>
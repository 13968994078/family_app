<template>
  <view class="login-container">
    <!-- 背景装饰 -->
    <view class="background-decor">
      <view class="circle circle-1"></view>
      <view class="circle circle-2"></view>
      <view class="circle circle-3"></view>
    </view>

    <!-- 登录内容 -->
    <view class="login-content">
      <!-- 品牌区域 -->
      <view class="brand-section">
        <text class="app-name">妙妙屋</text>
        <text class="slogan">米斯嘎，木斯嘎</text>
      </view>

      <!-- 登录表单 -->
      <view class="login-panel">
        <text class="panel-title">欢迎回到妙妙屋</text>
        <text class="panel-subtitle">请输入您的账户信息</text>

        <view class="form-group">
          <view class="input-wrapper">
            <text class="input-label">用户名</text>
            <input 
              v-model.trim="form.username" 
              placeholder="请输入用户名" 
              class="input-field"
              @input="onUsernameInput"
            />
          </view>

          <view class="input-wrapper">
            <text class="input-label">密码</text>
            <input 
              v-model.trim="form.password" 
              password 
              placeholder="请输入密码" 
              class="input-field"
            />
          </view>
        </view>

        <view class="tips-text">
          默认密码：123456，可先生成自己的账户
        </view>

        <button 
          class="login-btn" 
          :loading="loading" 
          @click="handleLogin"
          :disabled="!form.username || !form.password"
        >
          {{ loading ? '登录中...' : '立即登录' }}
        </button>

        <button 
          class="generate-btn" 
          :loading="creating" 
          @click="handleGenerateUser"
        >
          {{ creating ? '生成中...' : '生成新用户' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { generateUser, login, type LoginResult } from '@/api';

const form = reactive({
  username: '',
  password: '123456' // 默认密码
});

const loading = ref(false);
const creating = ref(false);

// 保存登录状态
const persistLogin = (result: LoginResult) => {
  uni.setStorageSync('token', result.token);
  uni.setStorageSync('userInfo', {
    id: result.user.id,
    username: result.user.username,
    nickname: result.user.nickname
  });
};

const navigateHome = () => {
  uni.switchTab({ url: '/pages/home/index' });
};

const handleLogin = async () => {
  if (!form.username || !form.password) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' });
    return;
  }
  
  loading.value = true;
  try {
    const data = await login({ ...form });
    persistLogin(data);
    uni.showToast({ title: '欢迎回来', icon: 'success' });
    setTimeout(navigateHome, 400);
  } catch (error: any) {
    uni.showToast({ title: error?.message || '登录失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const handleGenerateUser = async () => {
  if (!form.username) {
    uni.showToast({ title: '请先输入用户名', icon: 'none' });
    return;
  }
  
  creating.value = true;
  try {
    const user = await generateUser({ 
      username: form.username, 
      nickname: form.username 
    });
    
    // 生成新用户后自动填充
    form.username = user.username;
    form.password = '123456';
    
    uni.showToast({ title: '账号已生成', icon: 'success' });
    
    // 自动登录
    await handleLogin();
  } catch (error: any) {
    uni.showToast({ title: error?.message || '生成失败', icon: 'none' });
  } finally {
    creating.value = false;
  }
};

const onUsernameInput = () => {
  // 清除用户名中的特殊字符
  form.username = form.username.replace(/[^\w\u4e00-\u9fa5]/g, '');
};

onMounted(() => {
  // 检查是否已登录
  const token = uni.getStorageSync('token');
  if (token) {
    navigateHome();
  }
});
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.background-decor {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 200rpx;
  height: 200rpx;
  top: -100rpx;
  left: -100rpx;
}

.circle-2 {
  width: 300rpx;
  height: 300rpx;
  bottom: -150rpx;
  right: -150rpx;
}

.circle-3 {
  width: 150rpx;
  height: 150rpx;
  top: 30%;
  right: 20%;
}

.login-content {
  position: relative;
  z-index: 10;
  padding: 100rpx 40rpx 60rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.brand-section {
  text-align: center;
  margin-bottom: 60rpx;
}

.app-name {
  font-size: 56rpx;
  font-weight: 800;
  color: #fff;
  text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
  display: block;
  letter-spacing: 4rpx;
}

.slogan {
  display: block;
  margin-top: 16rpx;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.85);
}

.login-panel {
  background: #fff;
  border-radius: 32rpx;
  padding: 48rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
}

.panel-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1a1a;
  display: block;
  margin-bottom: 8rpx;
}

.panel-subtitle {
  font-size: 26rpx;
  color: #666;
  display: block;
  margin-bottom: 32rpx;
}

.form-group {
  margin-bottom: 24rpx;
}

.input-wrapper {
  margin-bottom: 24rpx;
}

.input-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
  font-weight: 500;
  padding-right: 20rpx;
}

.input-field {
  width: 100%;
  height: 80rpx;
  border-radius: 16rpx;
  border: 2rpx solid #e0e7ff;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #1a1a1a;
  background: #f8fafd;
  transition: border-color 0.3s;
}

.input-field:focus {
  border-color: #4a90e2;
  outline: none;
  box-shadow: 0 0 0 2rpx rgba(74, 144, 226, 0.2);
}

.tips-text {
  font-size: 24rpx;
  color: #888;
  margin: 24rpx 0 32rpx;
  line-height: 1.5;
}

.login-btn {
  width: 100%;
  height: 80rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  background: linear-gradient(135deg, #4a90e2 0%, #667eea 100%);
  color: #fff;
  border: none;
  margin-bottom: 16rpx;
  transition: transform 0.2s, box-shadow 0.2s;
}

.login-btn:active {
  transform: scale(0.98);
  box-shadow: 0 8rpx 20rpx rgba(74, 144, 226, 0.3);
}

.login-btn:disabled {
  background: #e0e7ff;
  color: #999;
  transform: none;
}

.generate-btn {
  width: 100%;
  height: 80rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  background: #f8fafd;
  color: #4a90e2;
  border: 2rpx solid #e0e7ff;
  transition: transform 0.2s, box-shadow 0.2s;
}

.generate-btn:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}
</style>
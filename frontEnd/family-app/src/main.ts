import { createSSRApp } from "vue";
import App from "./App.vue";
import { setTokenProvider } from '@/utils/request';

export function createApp() {
  const app = createSSRApp(App);

  setTokenProvider(() => {
    const token = uni.getStorageSync('token');
    return token ? String(token) : null;
  });

  return {
    app,
  };
}
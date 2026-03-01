export const storage = {
  get: <T = string>(key: string, defaultValue?: T): T => {
    try {
      const value = uni.getStorageSync(key);
      if (value === undefined || value === null) {
        return defaultValue as T;
      }
      return typeof value === 'string' ? (JSON.parse(value) as T) : (value as T);
    } catch (err) {
      console.error(`读取存储 ${key} 失败：`, err);
      return defaultValue as T;
    }
  },
  set: <T = string>(key: string, value: T): boolean => {
    try {
      const data = typeof value === 'object' ? JSON.stringify(value) : value;
      uni.setStorageSync(key, data);
      return true;
    } catch (err) {
      console.error(`设置存储 ${key} 失败：`, err);
      return false;
    }
  }
};
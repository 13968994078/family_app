import { get, post } from '@/utils/request';

export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
}

export interface LoginResult {
  token: string;
  user: UserInfo;
}

export interface Food {
  id: number;
  name: string;
  type: string;
  description?: string | null;
}

export interface CheckinRecord {
  id: number;
  userId: number;
  checkinTime: string;
  checkinDate: string;
  babyName?: string;
  remark?: string;
}

export interface CheckinItem {
  type: 'WAKE_UP' | 'BABY';
  userId: number;
  username: string;
  nickname?: string;
  time: string;
  babyName?: string;
}

export type CheckinCalendar = Record<string, CheckinItem[]>;

export interface CityFoodRecommend {
  city: string;
  foods: string[];
  source: 'online' | 'fallback';
}

const unwrap = async <T>(promise: Promise<any>): Promise<T> => {
  const res = await promise;
  if (res.code !== 200) {
    throw new Error(res.message || '请求失败');
  }
  return res.data as T;
};

export const login = (data: { username: string; password: string }) =>
  unwrap<LoginResult>(post('/auth/login', data));

export const generateUser = (data: { username: string; nickname?: string }) =>
  unwrap<UserInfo>(post('/users/createUser', data));

export const fetchRandomFood = () =>
  unwrap<Food>(get('/food/random'));

export const createFood = (data: { name: string; type: string; description?: string }) =>
  unwrap<Food>(post('/food', data));

export const wakeUpCheckin = (data: { userId: number; type?: string }) =>
  unwrap<CheckinRecord>(post('/checkin/wake-up', data));

export const babyCheckin = (data: { userId: number; babyName: string; remark?: string; type?: string }) =>
  unwrap<CheckinRecord>(post('/checkin/baby', data));

export const fetchAllFoods = (params?: Record<string, any>) =>
  unwrap<Food[]>(get('/food/listAll', params));

export const fetchCityFoodRecommend = (params: { latitude: number; longitude: number }) =>
  unwrap<CityFoodRecommend>(get('/food/city-recommend', params));

export const getCheckinCalendar = (params: { year: number; month: number; userId?: number }) =>
  unwrap<CheckinCalendar>(get('/checkin/calendar', params));

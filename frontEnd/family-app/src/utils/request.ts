type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';

interface RequestOptions {
  url: string;
  method?: HttpMethod;
  data?: any;
  header?: UniNamespace.RequestOptions['header'];
  showLoading?: boolean;
  timeout?: number;
}

interface RequestConfig extends RequestOptions {
  fullUrl: string;
}

type RequestInterceptor = (config: RequestConfig) => RequestConfig;
type ResponseInterceptor = (response: UniApp.RequestSuccessCallbackResult) => UniApp.RequestSuccessCallbackResult;

const BASE_URL_MAP: Record<string, string> = {
  development: 'http://localhost:8080/api',
  production: 'http://localhost:8080/api'
};

const BASE_URL =
  import.meta.env.VITE_API_BASE_URL ||
  BASE_URL_MAP[import.meta.env.MODE] ||
  BASE_URL_MAP.production;

let tokenProvider: () => string | null = () => null;

export const setTokenProvider = (provider: () => string | null) => {
  tokenProvider = provider;
};

const requestInterceptors: RequestInterceptor[] = [
  (config) => {
    const token = tokenProvider?.();
    if (token) {
      config.header = {
        ...config.header,
        Authorization: `Bearer ${token}`
      };
    }
    return config;
  }
];

const responseInterceptors: ResponseInterceptor[] = [
  (response) => {
    if (response.statusCode >= 400) {
      handleError(response);
    }
    return response;
  }
];

const applyRequestInterceptors = (config: RequestConfig) =>
  requestInterceptors.reduce((acc, interceptor) => interceptor(acc), config);

const applyResponseInterceptors = (response: UniApp.RequestSuccessCallbackResult) =>
  responseInterceptors.reduce((acc, interceptor) => interceptor(acc), response);

const handleError = (response: UniApp.RequestSuccessCallbackResult) => {
  const message =
    (response.data as any)?.message ||
    {
      400: '请求参数错误',
      401: '未授权，请登录',
      403: '无访问权限',
      404: '资源不存在',
      500: '服务器异常'
    }[response.statusCode] ||
    '网络异常，请稍后重试';

  uni.showToast({
    title: message,
    icon: 'none',
    duration: 2000
  });
};

export const request = <R = any>(options: RequestOptions) => {
  const {
    url,
    method = 'GET',
    data,
    header,
    timeout = 15000,
    showLoading = true
  } = options;

  const initialConfig: RequestConfig = {
    ...options,
    fullUrl: `${BASE_URL}${url}`,
    header
  };

  const config = applyRequestInterceptors(initialConfig);

  if (showLoading) {
    uni.showLoading({ title: '加载中', mask: true });
  }

  return new Promise<R>((resolve, reject) => {
    uni.request({
      url: config.fullUrl,
      method: (config.method || method) as HttpMethod,
      data: config.data ?? data,
      header: config.header,
      timeout: config.timeout ?? timeout,
      success: (res) => {
        const intercepted = applyResponseInterceptors(res as UniApp.RequestSuccessCallbackResult);
        if (intercepted.statusCode >= 200 && intercepted.statusCode < 300) {
          resolve(intercepted.data as R);
        } else {
          reject(intercepted);
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误，请检查连接', icon: 'none' });
        reject(err);
      },
      complete: () => {
        if (showLoading) {
          uni.hideLoading();
        }
      }
    });
  });
};

export const get = <R = any>(url: string, params?: Record<string, any>, options: Partial<RequestOptions> = {}) => {
  return request<R>({
    url,
    method: 'GET',
    data: params,
    ...options
  });
};

export const post = <R = any>(url: string, data?: any, options: Partial<RequestOptions> = {}) => {
  return request<R>({
    url,
    method: 'POST',
    data,
    header: {
      'Content-Type': 'application/json',
      ...(options.header || {})
    },
    ...options
  });
};

export const put = <R = any>(url: string, data?: any, options: Partial<RequestOptions> = {}) => {
  return request<R>({
    url,
    method: 'PUT',
    data,
    ...options
  });
};

export const del = <R = any>(url: string, options: Partial<RequestOptions> = {}) => {
  return request<R>({
    url,
    method: 'DELETE',
    ...options
  });
};
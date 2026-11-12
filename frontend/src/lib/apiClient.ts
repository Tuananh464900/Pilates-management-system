import axios from 'axios';
import { supabase } from './supabaseClient';

// Base URL chuẩn của Backend Pilates
const RAW_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
const BASE_URL = RAW_URL.endsWith('/api/v1') ? RAW_URL : `${RAW_URL.replace(/\/+$/, '')}/api/v1`;

export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(async (config) => {
  // 1. Tự động loại bỏ tiền tố '/api/v1' nếu endpoint trong code bị lặp
  if (config.url) {
    if (config.url.startsWith('/api/v1/')) {
      config.url = config.url.substring(7);
    } else if (config.url.startsWith('api/v1/')) {
      config.url = config.url.substring(6);
    } else if (config.url === '/api/v1' || config.url === 'api/v1') {
      config.url = '';
    }
  }

  try {
    // 2. Thử lấy token từ session của Supabase Auth
    const { data: { session } } = await supabase.auth.getSession();
    let token = session?.access_token;

    // 3. Nếu không có, lấy token lưu trong localStorage
    if (!token) {
      token = localStorage.getItem('pilates_token') || localStorage.getItem('access_token') || undefined;
    }

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  } catch (error) {
    console.error('Lỗi khi nạp Authorization token:', error);
  }

  return config;
}, (error) => {
  return Promise.reject(error);
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Xử lý tài khoản bị khóa
    if (error.response?.status === 403) {
      const msg = error.response.data?.message;
      if (msg && msg.includes('khóa')) {
        alert('Tài khoản của bạn đã bị khóa bởi Quản trị viên. Hệ thống sẽ đăng xuất.');
        localStorage.removeItem('pilates_token');
        localStorage.removeItem('access_token');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

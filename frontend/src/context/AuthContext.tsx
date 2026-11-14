import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, Role, MemberProfile, TrainerProfile, ApiResponse } from '../types/auth';
import { apiClient } from '../lib/apiClient';
import { supabase } from '../lib/supabaseClient';

interface AuthContextType {
  user: User | null;
  profile: MemberProfile | TrainerProfile | any | null;
  role: Role | null;
  loading: boolean;
  login: (email: string, password?: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
  changePassword: (oldPassword: string, newPassword: string) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [profile, setProfile] = useState<any>(null);
  const [role, setRole] = useState<Role | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const fetchCurrentUser = async () => {
    try {
      const response = await apiClient.get<ApiResponse<{ user: User; profile: any }>>('/auth/me');
      if (response.data.success && response.data.data) {
        setUser(response.data.data.user);
        setProfile(response.data.data.profile);
        setRole(response.data.data.user.role);
      }
    } catch (err) {
      // Xử lý luồng xác nhận email (P1 có điều kiện):
      // Người dùng vừa xác thực email từ Supabase và có session hợp lệ nhưng chưa có hồ sơ nghiệp vụ trong Backend
      const pendingStr = localStorage.getItem('pending_member_registration');
      if (pendingStr) {
        try {
          const pending = JSON.parse(pendingStr);
          const { data: { session } } = await supabase.auth.getSession();
          if (session?.access_token) {
            await apiClient.post('/auth/register-member', {
              userId: session.user.id,
              token: session.access_token,
              email: session.user.email || pending.email,
              fullName: pending.fullName,
              phone: pending.phone,
              gender: pending.gender,
              dateOfBirth: pending.dateOfBirth,
              address: pending.address,
            });
            localStorage.removeItem('pending_member_registration');
            const retryRes = await apiClient.get<ApiResponse<{ user: User; profile: any }>>('/auth/me');
            if (retryRes.data.success && retryRes.data.data) {
              setUser(retryRes.data.data.user);
              setProfile(retryRes.data.data.profile);
              setRole(retryRes.data.data.user.role);
              return;
            }
          }
        } catch (regErr) {
          console.error('Không thể hoàn tất tạo hồ sơ học viên sau khi xác nhận email:', regErr);
        }
      }
      setUser(null);
      setProfile(null);
      setRole(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const initAuth = async () => {
      const token = localStorage.getItem('pilates_token') || localStorage.getItem('access_token');
      const { data: { session } } = await supabase.auth.getSession();
      
      if (session?.access_token) {
        localStorage.setItem('pilates_token', session.access_token);
        localStorage.setItem('access_token', session.access_token);
        await fetchCurrentUser();
      } else if (token) {
        await fetchCurrentUser();
      } else {
        setLoading(false);
      }
    };

    initAuth();

    const { data: authListener } = supabase.auth.onAuthStateChange(async (_event, session) => {
      if (session?.access_token) {
        localStorage.setItem('pilates_token', session.access_token);
        localStorage.setItem('access_token', session.access_token);
        await fetchCurrentUser();
      } else if (!localStorage.getItem('pilates_token') && !localStorage.getItem('access_token')) {
        setUser(null);
        setProfile(null);
        setRole(null);
        setLoading(false);
      }
    });

    return () => {
      authListener.subscription.unsubscribe();
    };
  }, []);

  const login = async (email: string, password?: string) => {
    setLoading(true);
    const trimmedEmail = email.toLowerCase().trim();

    try {
      let loggedIn = false;
      const isPlaceholderSupabase = !import.meta.env.VITE_SUPABASE_URL || import.meta.env.VITE_SUPABASE_URL.includes('placeholder');

      // 1. Đăng nhập chuẩn qua Supabase Auth nếu cấu hình đầy đủ
      if (!isPlaceholderSupabase) {
        if (!password) {
          throw new Error('Vui lòng nhập mật khẩu tài khoản.');
        }

        let authResult = await supabase.auth.signInWithPassword({
          email: trimmedEmail,
          password
        });

        // Tự động thử lại với password đã cắt khoảng trắng nếu copy-paste vô tình dính dấu cách
        if (authResult.error && password.trim() !== password) {
          authResult = await supabase.auth.signInWithPassword({
            email: trimmedEmail,
            password: password.trim()
          });
        }

        const { data, error } = authResult;

        if (error) {
          // Báo lỗi xác thực rõ ràng, tuyệt đối KHÔNG fallback sang mock trong production
          throw new Error(error.message || 'Email hoặc mật khẩu không chính xác.');
        }

        if (data.session?.access_token) {
          localStorage.setItem('pilates_token', data.session.access_token);
          localStorage.setItem('access_token', data.session.access_token);
          loggedIn = true;
        }
      } else {
        // 2. Chế độ Mock Login chỉ cho phép khi ở môi trường Development và được bật rõ ràng
        const allowDevMock = import.meta.env.DEV && import.meta.env.VITE_ALLOW_MOCK_LOGIN === 'true';
        if (allowDevMock) {
          const response = await apiClient.post<ApiResponse<{ token: string }>>(
            `/auth/login-mock?email=${encodeURIComponent(trimmedEmail)}`
          );
          if (response.data.success && response.data.data?.token) {
            const token = response.data.data.token;
            localStorage.setItem('pilates_token', token);
            localStorage.setItem('access_token', token);
            loggedIn = true;
          }
        } else {
          throw new Error('Chưa cấu hình thông tin Supabase Auth trên hệ thống.');
        }
      }

      if (!loggedIn) {
        throw new Error('Đăng nhập không thành công. Vui lòng kiểm tra lại email hoặc mật khẩu.');
      }

      await fetchCurrentUser();
    } catch (error: any) {
      setLoading(false);
      throw error;
    }
  };

  const changePassword = async (oldPassword: string, newPassword: string) => {
    if (!user) {
      throw new Error('Phiên làm việc đã hết hạn hoặc bạn chưa đăng nhập. Vui lòng đăng nhập lại.');
    }
    if (!newPassword || newPassword.trim().length < 6) {
      throw new Error('Mật khẩu mới phải có ít nhất 6 ký tự.');
    }
    if (oldPassword === newPassword) {
      throw new Error('Mật khẩu mới không được trùng với mật khẩu hiện tại.');
    }

    // Thống nhất 1 luồng duy nhất: Gửi yêu cầu đổi mật khẩu tới Backend API
    // Backend chịu trách nhiệm xác minh mật khẩu hiện tại (oldPassword) với Supabase Auth
    // và cập nhật mật khẩu mới qua Supabase Admin API với Service Role Key.
    // Tuyệt đối KHÔNG gọi supabase.auth.updateUser() trước ở Frontend để tránh làm mất mật khẩu cũ trước khi Backend xác minh.
    const res = await apiClient.post<ApiResponse<void>>('/auth/change-password', {
      oldPassword,
      newPassword,
    });

    if (!res.data.success) {
      throw new Error(res.data.message || 'Đổi mật khẩu thất bại.');
    }
  };

  const logout = async () => {
    try {
      await supabase.auth.signOut();
    } catch (e) {
      // Ignore
    }
    localStorage.removeItem('pilates_token');
    localStorage.removeItem('access_token');
    setUser(null);
    setProfile(null);
    setRole(null);
  };

  const refreshUser = async () => {
    await fetchCurrentUser();
  };

  return (
    <AuthContext.Provider value={{ user, profile, role, loading, login, logout, refreshUser, changePassword }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

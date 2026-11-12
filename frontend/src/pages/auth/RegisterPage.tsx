import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiClient } from '../../lib/apiClient';
import { supabase } from '../../lib/supabaseClient';
import { ApiResponse, Gender } from '../../types/auth';
import { Activity, AlertCircle, CheckCircle2 } from 'lucide-react';

export const RegisterPage: React.FC = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    fullName: '',
    phone: '',
    gender: 'NU' as Gender,
    dateOfBirth: '',
    address: '',
  });

  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (formData.password !== formData.confirmPassword) {
      setError('Mật khẩu xác nhận không trùng khớp.');
      return;
    }

    setIsSubmitting(true);
    let createdUserId: string | undefined = undefined;
    let createdToken: string | undefined = undefined;

    try {

      // 1. Đăng ký qua Supabase Auth để xác thực tài khoản và lấy UID/Token
      if (!import.meta.env.VITE_SUPABASE_URL?.includes('placeholder')) {
        const { data: authData, error: authError } = await supabase.auth.signUp({
          email: formData.email,
          password: formData.password,
        });

        if (authError) throw authError;
        createdUserId = authData.user?.id;
        createdToken = authData.session?.access_token;

        // Nếu Supabase chưa trả về session ngay, thử đăng nhập để lấy session (khi Supabase tắt confirm email)
        if (!createdToken) {
          try {
            const { data: loginData } = await supabase.auth.signInWithPassword({
              email: formData.email,
              password: formData.password,
            });
            if (loginData.session) {
              createdUserId = loginData.user?.id;
              createdToken = loginData.session.access_token;
            }
          } catch {
            // Đang chờ xác nhận email từ người dùng
          }
        }
      }

      // Xử lý hoàn thiện luồng xác nhận email (P1 có điều kiện):
      // Nếu Supabase yêu cầu xác nhận email trước khi cấp session/token, lưu thông tin đăng ký chờ
      // và hướng dẫn người dùng xác nhận qua email thay vì gửi request thiếu token lên backend
      if (!createdToken && !import.meta.env.VITE_SUPABASE_URL?.includes('placeholder')) {
        localStorage.setItem(
          'pending_member_registration',
          JSON.stringify({
            userId: createdUserId,
            email: formData.email,
            fullName: formData.fullName,
            phone: formData.phone,
            gender: formData.gender,
            dateOfBirth: formData.dateOfBirth || null,
            address: formData.address,
          })
        );

        setSuccess(
          `Đăng ký tài khoản xác thực thành công! Hệ thống đã gửi email kích hoạt đến ${formData.email}. ` +
          `Vui lòng kiểm tra hộp thư email (inbox hoặc spam) và nhấp vào liên kết xác nhận, sau đó đăng nhập để hoàn tất kích hoạt hồ sơ học viên.`
        );
        return;
      }

      // 2. Gửi request đăng ký sang backend (đã có token JWT hợp lệ để xác minh quyền sở hữu)
      const payload = {
        userId: createdUserId,
        token: createdToken,
        email: formData.email,
        password: formData.password,
        fullName: formData.fullName,
        phone: formData.phone,
        gender: formData.gender,
        dateOfBirth: formData.dateOfBirth || null,
        address: formData.address,
      };

      const res = await apiClient.post<ApiResponse<any>>('/auth/register-member', payload);

      if (res.data.success) {
        setSuccess('Đăng ký tài khoản Học viên thành công! Đang chuyển hướng sang trang đăng nhập...');
        setTimeout(() => {
          navigate('/login');
        }, 1800);
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.';
      setError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={{ display: 'flex', minHeight: '85vh', alignItems: 'center', justifyContent: 'center', padding: '2rem 1rem' }}>
      <div className="card" style={{ maxWidth: '580px', width: '100%', padding: '2.5rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '1.75rem' }}>
          <div style={{ display: 'inline-flex', padding: '0.75rem', background: 'var(--primary-subtle)', borderRadius: 'var(--radius-full)', color: 'var(--primary)', marginBottom: '0.75rem' }}>
            <Activity size={32} />
          </div>
          <h2 style={{ fontSize: '1.5rem', marginBottom: '0.25rem' }}>Đăng ký tài khoản</h2>
          <p style={{ fontSize: '0.9rem' }}>Tham gia các lớp Pilates chuyên nghiệp cùng chúng tôi</p>
        </div>

        {error && (
          <div className="alert-banner alert-danger">
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        {success && (
          <div className="alert-banner alert-success">
            <CheckCircle2 size={18} />
            <span>{success}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="grid grid-cols-2">
            <div className="form-group">
              <label className="form-label">Họ và tên *</label>
              <input
                type="text"
                name="fullName"
                required
                className="form-input"
                placeholder="Nguyễn Văn A"
                value={formData.fullName}
                onChange={handleChange}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Số điện thoại *</label>
              <input
                type="tel"
                name="phone"
                required
                className="form-input"
                placeholder="0912 345 678"
                value={formData.phone}
                onChange={handleChange}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Địa chỉ Email *</label>
            <input
              type="email"
              name="email"
              required
              className="form-input"
              placeholder="name@example.com"
              value={formData.email}
              onChange={handleChange}
            />
          </div>

          <div className="grid grid-cols-2">
            <div className="form-group">
              <label className="form-label">Mật khẩu *</label>
              <input
                type="password"
                name="password"
                required
                minLength={6}
                className="form-input"
                placeholder="Tối thiểu 6 ký tự"
                value={formData.password}
                onChange={handleChange}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Xác nhận mật khẩu *</label>
              <input
                type="password"
                name="confirmPassword"
                required
                className="form-input"
                placeholder="Nhập lại mật khẩu"
                value={formData.confirmPassword}
                onChange={handleChange}
              />
            </div>
          </div>

          <div className="grid grid-cols-2">
            <div className="form-group">
              <label className="form-label">Giới tính</label>
              <select
                name="gender"
                className="form-select"
                value={formData.gender}
                onChange={handleChange}
              >
                <option value="NU">Nữ</option>
                <option value="NAM">Nam</option>
                <option value="KHAC">Khác</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Ngày sinh</label>
              <input
                type="date"
                name="dateOfBirth"
                className="form-input"
                value={formData.dateOfBirth}
                onChange={handleChange}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Địa chỉ</label>
            <input
              type="text"
              name="address"
              className="form-input"
              placeholder="Số nhà, đường, quận/huyện..."
              value={formData.address}
              onChange={handleChange}
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '1rem', padding: '0.75rem' }}
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Đang tạo tài khoản...' : 'Đăng ký ngay'}
          </button>
        </form>

        <div style={{ marginTop: '1.75rem', textAlign: 'center', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
          Đã có tài khoản?{' '}
          <Link to="/login" style={{ fontWeight: 600 }}>
            Đăng nhập ngay
          </Link>
        </div>
      </div>
    </div>
  );
};

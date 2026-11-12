import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { apiClient } from '../../lib/apiClient';
import { ApiResponse, Gender } from '../../types/auth';
import { User, Shield, CheckCircle2, AlertCircle, KeyRound, Save, Eye, EyeOff } from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const { user, profile, refreshUser, changePassword } = useAuth();

  const [formData, setFormData] = useState({
    fullName: '',
    phone: '',
    gender: 'NAM' as Gender,
    dateOfBirth: '',
    address: '',
    specialization: '',
    bio: '',
  });

  const [passwordData, setPasswordData] = useState({
    oldPassword: '',
    newPassword: '',
    confirmNewPassword: '',
  });

  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [profileSuccess, setProfileSuccess] = useState<string | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [passwordSuccess, setPasswordSuccess] = useState<string | null>(null);
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (profile) {
      setFormData({
        fullName: profile.fullName || '',
        phone: profile.phone || '',
        gender: profile.gender || 'NAM',
        dateOfBirth: profile.dateOfBirth || '',
        address: profile.address || '',
        specialization: profile.specialization || '',
        bio: profile.bio || '',
      });
    }
  }, [profile]);

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setProfileSuccess(null);
    setProfileError(null);
    setIsSaving(true);

    try {
      const res = await apiClient.put<ApiResponse<any>>('/profile/me', formData);
      if (res.data.success) {
        setProfileSuccess('Cập nhật hồ sơ cá nhân thành công!');
        await refreshUser();
      }
    } catch (err: any) {
      setProfileError(err.response?.data?.message || 'Không thể cập nhật hồ sơ cá nhân.');
    } finally {
      setIsSaving(false);
    }
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setPasswordSuccess(null);
    setPasswordError(null);

    if (!passwordData.oldPassword) {
      setPasswordError('Vui lòng nhập mật khẩu hiện tại.');
      return;
    }

    if (passwordData.newPassword.length < 6) {
      setPasswordError('Mật khẩu mới phải có ít nhất 6 ký tự.');
      return;
    }

    if (passwordData.newPassword !== passwordData.confirmNewPassword) {
      setPasswordError('Mật khẩu mới xác nhận không khớp.');
      return;
    }

    if (passwordData.newPassword === passwordData.oldPassword) {
      setPasswordError('Mật khẩu mới không được trùng với mật khẩu hiện tại.');
      return;
    }

    try {
      await changePassword(passwordData.oldPassword, passwordData.newPassword);
      setPasswordSuccess('Đổi mật khẩu thành công!');
      setPasswordData({ oldPassword: '', newPassword: '', confirmNewPassword: '' });
    } catch (err: any) {
      setPasswordError(err.response?.data?.message || err.message || 'Đổi mật khẩu thất bại.');
    }
  };

  return (
    <div className="main-content">
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', marginBottom: '0.25rem' }}>Hồ sơ cá nhân</h1>
        <p>Quản lý thông tin tài khoản và bảo mật cá nhân</p>
      </div>

      <div className="grid grid-cols-3">
        {/* Cột trái: Tóm tắt tài khoản */}
        <div className="card" style={{ height: 'fit-content' }}>
          <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
            <div style={{
              width: '72px', height: '72px', borderRadius: '50%', background: 'var(--primary-subtle)',
              color: 'var(--primary)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
              fontSize: '1.75rem', fontWeight: 700, marginBottom: '0.75rem'
            }}>
              {formData.fullName ? formData.fullName.charAt(0).toUpperCase() : 'U'}
            </div>
            <h3 style={{ fontSize: '1.2rem', marginBottom: '0.25rem' }}>{formData.fullName || 'Người dùng'}</h3>
            <p style={{ fontSize: '0.85rem' }}>{user?.email}</p>
            <div style={{ marginTop: '0.75rem' }}>
              <span className={`badge badge-${user?.role.toLowerCase()}`}>
                {user?.role}
              </span>
              {' '}
              <span className={`badge badge-${user?.status.toLowerCase()}`}>
                {user?.status}
              </span>
            </div>
          </div>

          <hr style={{ border: 'none', borderTop: '1px solid var(--border-light)', margin: '1rem 0' }} />

          <div style={{ fontSize: '0.875rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.4rem 0' }}>
              <span style={{ color: 'var(--text-muted)' }}>Mã người dùng:</span>
              <span style={{ fontFamily: 'monospace', fontWeight: 600 }}>{user?.id.substring(0, 8)}...</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.4rem 0' }}>
              <span style={{ color: 'var(--text-muted)' }}>Ngày tạo:</span>
              <span>{user?.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : 'Mới tạo'}</span>
            </div>
          </div>
        </div>

        {/* 2 Cột phải: Form cập nhật thông tin & Đổi mật khẩu */}
        <div style={{ gridColumn: 'span 2', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Cập nhật hồ sơ */}
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Thông tin cá nhân</h3>
            </div>

            {profileSuccess && (
              <div className="alert-banner alert-success">
                <CheckCircle2 size={18} />
                <span>{profileSuccess}</span>
              </div>
            )}

            {profileError && (
              <div className="alert-banner alert-danger">
                <AlertCircle size={18} />
                <span>{profileError}</span>
              </div>
            )}

            <form onSubmit={handleProfileSubmit}>
              <div className="grid grid-cols-2">
                <div className="form-group">
                  <label className="form-label">Họ và tên *</label>
                  <input
                    type="text"
                    required
                    className="form-input"
                    value={formData.fullName}
                    onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Số điện thoại *</label>
                  <input
                    type="tel"
                    required
                    className="form-input"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  />
                </div>
              </div>

              {user?.role === 'MEMBER' && (
                <>
                  <div className="grid grid-cols-2">
                    <div className="form-group">
                      <label className="form-label">Giới tính</label>
                      <select
                        className="form-select"
                        value={formData.gender}
                        onChange={(e) => setFormData({ ...formData, gender: e.target.value as Gender })}
                      >
                        <option value="NAM">Nam</option>
                        <option value="NU">Nữ</option>
                        <option value="KHAC">Khác</option>
                      </select>
                    </div>

                    <div className="form-group">
                      <label className="form-label">Ngày sinh</label>
                      <input
                        type="date"
                        className="form-input"
                        value={formData.dateOfBirth}
                        onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Địa chỉ</label>
                    <input
                      type="text"
                      className="form-input"
                      value={formData.address}
                      onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                    />
                  </div>
                </>
              )}

              {user?.role === 'TRAINER' && (
                <>
                  <div className="form-group">
                    <label className="form-label">Chuyên môn / Chứng chỉ Pilates</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="Ví dụ: Reformer Pilates, Mat Pilates, Pilates Phục hồi..."
                      value={formData.specialization}
                      onChange={(e) => setFormData({ ...formData, specialization: e.target.value })}
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Tiểu sử / Giới thiệu kinh nghiệm</label>
                    <textarea
                      rows={3}
                      className="form-textarea"
                      value={formData.bio}
                      onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
                    />
                  </div>
                </>
              )}

              <button type="submit" className="btn btn-primary" disabled={isSaving}>
                <Save size={16} />
                <span>{isSaving ? 'Đang lưu...' : 'Lưu thay đổi'}</span>
              </button>
            </form>
          </div>

          {/* Đổi mật khẩu */}
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Đổi mật khẩu</h3>
            </div>

            {passwordSuccess && (
              <div className="alert-banner alert-success">
                <CheckCircle2 size={18} />
                <span>{passwordSuccess}</span>
              </div>
            )}

            {passwordError && (
              <div className="alert-banner alert-danger">
                <AlertCircle size={18} />
                <span>{passwordError}</span>
              </div>
            )}

            <form onSubmit={handlePasswordSubmit}>
              <div className="form-group">
                <label className="form-label">Mật khẩu hiện tại</label>
                <div style={{ position: 'relative' }}>
                  <input
                    type={showOldPassword ? 'text' : 'password'}
                    required
                    className="form-input"
                    placeholder="Nhập mật khẩu hiện tại"
                    value={passwordData.oldPassword}
                    onChange={(e) => setPasswordData({ ...passwordData, oldPassword: e.target.value })}
                    style={{ paddingRight: '2.5rem' }}
                  />
                  <button
                    type="button"
                    onClick={() => setShowOldPassword(!showOldPassword)}
                    title={showOldPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                    style={{
                      position: 'absolute',
                      right: '0.75rem',
                      top: '50%',
                      transform: 'translateY(-50%)',
                      background: 'none',
                      border: 'none',
                      cursor: 'pointer',
                      color: 'var(--text-subtle)',
                      padding: 0,
                      display: 'flex',
                      alignItems: 'center'
                    }}
                  >
                    {showOldPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-2">
                <div className="form-group">
                  <label className="form-label">Mật khẩu mới</label>
                  <div style={{ position: 'relative' }}>
                    <input
                      type={showNewPassword ? 'text' : 'password'}
                      required
                      minLength={6}
                      className="form-input"
                      placeholder="Tối thiểu 6 ký tự"
                      value={passwordData.newPassword}
                      onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
                      style={{ paddingRight: '2.5rem' }}
                    />
                    <button
                      type="button"
                      onClick={() => setShowNewPassword(!showNewPassword)}
                      title={showNewPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                      style={{
                        position: 'absolute',
                        right: '0.75rem',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                        color: 'var(--text-subtle)',
                        padding: 0,
                        display: 'flex',
                        alignItems: 'center'
                      }}
                    >
                      {showNewPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Xác nhận mật khẩu mới</label>
                  <div style={{ position: 'relative' }}>
                    <input
                      type={showConfirmPassword ? 'text' : 'password'}
                      required
                      className="form-input"
                      placeholder="Nhập lại mật khẩu mới"
                      value={passwordData.confirmNewPassword}
                      onChange={(e) => setPasswordData({ ...passwordData, confirmNewPassword: e.target.value })}
                      style={{ paddingRight: '2.5rem' }}
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      title={showConfirmPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                      style={{
                        position: 'absolute',
                        right: '0.75rem',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                        color: 'var(--text-subtle)',
                        padding: 0,
                        display: 'flex',
                        alignItems: 'center'
                      }}
                    >
                      {showConfirmPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </div>
                </div>
              </div>

              <button type="submit" className="btn btn-secondary">
                <KeyRound size={16} />
                <span>Cập nhật mật khẩu</span>
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

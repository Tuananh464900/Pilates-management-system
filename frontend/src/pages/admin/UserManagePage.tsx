import React, { useState, useEffect } from 'react';
import { apiClient } from '../../lib/apiClient';
import { User, Role, UserStatus, PageResponse, ApiResponse } from '../../types/auth';
import { useAuth } from '../../context/AuthContext';
import { UserPlus, Search, Filter, Lock, Unlock, CheckCircle2, AlertCircle, X } from 'lucide-react';

export const UserManagePage: React.FC = () => {
  const { user: currentAdmin } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [roleFilter, setRoleFilter] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [search, setSearch] = useState<string>('');
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);

  // Modal thêm Trainer
  const [showAddTrainerModal, setShowAddTrainerModal] = useState(false);
  const [trainerForm, setTrainerForm] = useState({
    email: '',
    password: '',
    fullName: '',
    phone: '',
    specialization: '',
    bio: '',
  });

  const [notification, setNotification] = useState<{ type: 'success' | 'danger'; message: string } | null>(null);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (roleFilter) params.append('role', roleFilter);
      if (statusFilter) params.append('status', statusFilter);
      if (search) params.append('search', search);
      params.append('page', page.toString());
      params.append('size', '10');

      const res = await apiClient.get<ApiResponse<PageResponse<User>>>(`/admin/users?${params.toString()}`);
      if (res.data.success && res.data.data) {
        setUsers(res.data.data.items);
        setTotalPages(res.data.data.totalPages);
      }
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || 'Lỗi khi tải danh sách người dùng' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [roleFilter, statusFilter, page]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchUsers();
  };

  const handleToggleStatus = async (user: User) => {
    if (user.id === currentAdmin?.id) {
      setNotification({ type: 'danger', message: 'Bạn không thể tự khóa tài khoản của chính mình!' });
      return;
    }

    const nextStatus: UserStatus = user.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE';
    const actionText = nextStatus === 'LOCKED' ? 'khóa' : 'mở khóa';

    if (!window.confirm(`Bạn có chắc chắn muốn ${actionText} tài khoản ${user.email}?`)) {
      return;
    }

    try {
      const res = await apiClient.patch<ApiResponse<User>>(`/admin/users/${user.id}/status`, {
        status: nextStatus,
      });

      if (res.data.success) {
        setNotification({ type: 'success', message: `Đã ${actionText} tài khoản ${user.email} thành công.` });
        fetchUsers();
      }
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || `Lỗi khi ${actionText} tài khoản.` });
    }
  };

  const handleCreateTrainer = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await apiClient.post<ApiResponse<any>>('/admin/users/trainers', trainerForm);
      if (res.data.success) {
        setNotification({ type: 'success', message: `Tạo tài khoản HLV ${trainerForm.fullName} thành công.` });
        setShowAddTrainerModal(false);
        setTrainerForm({ email: '', password: '', fullName: '', phone: '', specialization: '', bio: '' });
        fetchUsers();
      }
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || 'Lỗi khi tạo tài khoản HLV.' });
    }
  };

  return (
    <div className="main-content">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', marginBottom: '0.25rem' }}>Quản lý tài khoản người dùng</h1>
          <p>Xem danh sách tài khoản, tạo Huấn luyện viên, khóa/mở khóa tài khoản</p>
        </div>

        <button onClick={() => setShowAddTrainerModal(true)} className="btn btn-primary">
          <UserPlus size={18} />
          <span>Thêm Huấn luyện viên</span>
        </button>
      </div>

      {notification && (
        <div className={`alert-banner alert-${notification.type}`}>
          {notification.type === 'success' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
          <span>{notification.message}</span>
          <button onClick={() => setNotification(null)} style={{ marginLeft: 'auto', background: 'none', border: 'none', cursor: 'pointer' }}>
            <X size={16} />
          </button>
        </div>
      )}

      {/* Bộ lọc tìm kiếm */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1.25rem 1.5rem' }}>
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
          <div style={{ flex: '1', minWidth: '220px' }}>
            <label className="form-label">Tìm kiếm Email</label>
            <div style={{ position: 'relative' }}>
              <input
                type="text"
                className="form-input"
                placeholder="Nhập email..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                style={{ paddingLeft: '2.25rem' }}
              />
              <Search size={16} style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-subtle)' }} />
            </div>
          </div>

          <div style={{ width: '180px' }}>
            <label className="form-label">Vai trò</label>
            <select className="form-select" value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)}>
              <option value="">Tất cả vai trò</option>
              <option value="ADMIN">ADMIN</option>
              <option value="TRAINER">TRAINER</option>
              <option value="MEMBER">MEMBER</option>
            </select>
          </div>

          <div style={{ width: '180px' }}>
            <label className="form-label">Trạng thái</label>
            <select className="form-select" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="">Tất cả trạng thái</option>
              <option value="ACTIVE">ACTIVE (Hoạt động)</option>
              <option value="LOCKED">LOCKED (Bị khóa)</option>
            </select>
          </div>

          <button type="submit" className="btn btn-secondary">
            <Filter size={16} />
            <span>Lọc</span>
          </button>
        </form>
      </div>

      {/* Bảng danh sách người dùng */}
      <div className="table-container">
        <table className="table">
          <thead>
            <tr>
              <th>Họ tên & Email</th>
              <th>Số điện thoại</th>
              <th>Vai trò</th>
              <th>Trạng thái</th>
              <th>Ngày tạo</th>
              <th style={{ textAlign: 'right' }}>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={6} style={{ textAlign: 'center', padding: '2rem' }}>
                  Đang tải dữ liệu...
                </td>
              </tr>
            ) : users.length === 0 ? (
              <tr>
                <td colSpan={6} style={{ textAlign: 'center', padding: '2rem' }}>
                  Không tìm thấy người dùng nào phù hợp.
                </td>
              </tr>
            ) : (
              users.map((u) => (
                <tr key={u.id}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{u.fullName || 'Chưa cập nhật'}</div>
                    <div style={{ fontSize: '0.825rem', color: 'var(--text-muted)' }}>{u.email}</div>
                  </td>
                  <td>{u.phone || '—'}</td>
                  <td>
                    <span className={`badge badge-${u.role.toLowerCase()}`}>
                      {u.role}
                    </span>
                  </td>
                  <td>
                    <span className={`badge badge-${u.status.toLowerCase()}`}>
                      {u.status === 'ACTIVE' ? 'Hoạt động' : 'Bị khóa'}
                    </span>
                  </td>
                  <td>{u.createdAt ? new Date(u.createdAt).toLocaleDateString('vi-VN') : '—'}</td>
                  <td style={{ textAlign: 'right' }}>
                    {u.id !== currentAdmin?.id && (
                      <button
                        onClick={() => handleToggleStatus(u)}
                        className={`btn btn-sm ${u.status === 'ACTIVE' ? 'btn-danger' : 'btn-secondary'}`}
                        title={u.status === 'ACTIVE' ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}
                      >
                        {u.status === 'ACTIVE' ? (
                          <>
                            <Lock size={14} /> Khóa
                          </>
                        ) : (
                          <>
                            <Unlock size={14} /> Mở khóa
                          </>
                        )}
                      </button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Phân trang */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
        <button
          className="btn btn-secondary btn-sm"
          disabled={page === 0}
          onClick={() => setPage(page - 1)}
        >
          Trang trước
        </button>
        <span style={{ display: 'flex', alignItems: 'center', padding: '0 0.5rem', fontSize: '0.9rem' }}>
          Trang {page + 1} / {totalPages || 1}
        </span>
        <button
          className="btn btn-secondary btn-sm"
          disabled={page + 1 >= totalPages}
          onClick={() => setPage(page + 1)}
        >
          Trang sau
        </button>
      </div>

      {/* Modal Thêm Huấn luyện viên */}
      {showAddTrainerModal && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '540px' }}>
            <div className="card-header">
              <h3 className="card-title">Thêm Huấn luyện viên mới</h3>
              <button onClick={() => setShowAddTrainerModal(false)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleCreateTrainer}>
              <div className="grid grid-cols-2">
                <div className="form-group">
                  <label className="form-label">Họ và tên *</label>
                  <input
                    type="text"
                    required
                    className="form-input"
                    value={trainerForm.fullName}
                    onChange={(e) => setTrainerForm({ ...trainerForm, fullName: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Số điện thoại *</label>
                  <input
                    type="tel"
                    required
                    className="form-input"
                    value={trainerForm.phone}
                    onChange={(e) => setTrainerForm({ ...trainerForm, phone: e.target.value })}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2">
                <div className="form-group">
                  <label className="form-label">Email tài khoản *</label>
                  <input
                    type="email"
                    required
                    className="form-input"
                    value={trainerForm.email}
                    onChange={(e) => setTrainerForm({ ...trainerForm, email: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Mật khẩu ban đầu *</label>
                  <input
                    type="password"
                    required
                    minLength={6}
                    className="form-input"
                    value={trainerForm.password}
                    onChange={(e) => setTrainerForm({ ...trainerForm, password: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Chuyên môn / Chứng chỉ</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="Ví dụ: Mat Pilates, Reformer..."
                  value={trainerForm.specialization}
                  onChange={(e) => setTrainerForm({ ...trainerForm, specialization: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Tiểu sử kinh nghiệm</label>
                <textarea
                  rows={2}
                  className="form-textarea"
                  value={trainerForm.bio}
                  onChange={(e) => setTrainerForm({ ...trainerForm, bio: e.target.value })}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button type="button" onClick={() => setShowAddTrainerModal(false)} className="btn btn-secondary">
                  Hủy
                </button>
                <button type="submit" className="btn btn-primary">
                  Tạo tài khoản HLV
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

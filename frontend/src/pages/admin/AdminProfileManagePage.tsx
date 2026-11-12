import React, { useState, useEffect } from 'react';
import { apiClient } from '../../lib/apiClient';
import { MemberProfile, TrainerProfile, PageResponse, ApiResponse, Gender } from '../../types/auth';
import { Users, Search, Edit3, X, CheckCircle2, AlertCircle } from 'lucide-react';
import { normalizeVietnameseText } from '../../lib/formatters';

export const AdminProfileManagePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'MEMBERS' | 'TRAINERS'>('MEMBERS');
  const [search, setSearch] = useState('');
  const [members, setMembers] = useState<MemberProfile[]>([]);
  const [trainers, setTrainers] = useState<TrainerProfile[]>([]);
  const [loading, setLoading] = useState(false);
  const [notification, setNotification] = useState<{ type: 'success' | 'danger'; message: string } | null>(null);

  // Edit Modal
  const [editingItem, setEditingItem] = useState<{ type: 'MEMBER' | 'TRAINER'; data: any } | null>(null);
  const [editForm, setEditForm] = useState({
    fullName: '',
    phone: '',
    gender: 'NAM' as Gender,
    dateOfBirth: '',
    address: '',
    specialization: '',
    bio: '',
  });

  const fetchData = async () => {
    setLoading(true);
    try {
      if (activeTab === 'MEMBERS') {
        const res = await apiClient.get<ApiResponse<PageResponse<MemberProfile>>>(
          `/admin/members?search=${encodeURIComponent(search)}`
        );
        if (res.data.success) {
          setMembers(res.data.data.items);
        }
      } else {
        const res = await apiClient.get<ApiResponse<PageResponse<TrainerProfile>>>(
          `/admin/trainers?search=${encodeURIComponent(search)}`
        );
        if (res.data.success) {
          setTrainers(res.data.data.items);
        }
      }
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || 'Lỗi khi tải dữ liệu hồ sơ' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchData();
  };

  const handleOpenEdit = (type: 'MEMBER' | 'TRAINER', item: any) => {
    setEditingItem({ type, data: item });
    setEditForm({
      fullName: item.fullName || '',
      phone: item.phone || '',
      gender: item.gender || 'NAM',
      dateOfBirth: item.dateOfBirth || '',
      address: item.address || '',
      specialization: item.specialization || '',
      bio: item.bio || '',
    });
  };

  const handleSaveEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingItem) return;

    try {
      const endpoint = editingItem.type === 'MEMBER'
        ? `/admin/members/${editingItem.data.userId}`
        : `/admin/trainers/${editingItem.data.userId}`;

      const res = await apiClient.put<ApiResponse<any>>(endpoint, editForm);
      if (res.data.success) {
        setNotification({ type: 'success', message: 'Cập nhật hồ sơ thành công.' });
        setEditingItem(null);
        fetchData();
      }
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || 'Lỗi khi cập nhật hồ sơ.' });
    }
  };

  return (
    <div className="main-content">
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', marginBottom: '0.25rem' }}>Quản lý hồ sơ Học viên & Huấn luyện viên</h1>
        <p>Tra cứu, xem thông tin và cập nhật chuyên môn</p>
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

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-light)' }}>
        <button
          onClick={() => setActiveTab('MEMBERS')}
          className={`btn ${activeTab === 'MEMBERS' ? 'btn-primary' : 'btn-secondary'}`}
          style={{ borderRadius: 'var(--radius-md) var(--radius-md) 0 0', borderBottom: 'none' }}
        >
          Hồ sơ Học viên ({members.length})
        </button>
        <button
          onClick={() => setActiveTab('TRAINERS')}
          className={`btn ${activeTab === 'TRAINERS' ? 'btn-primary' : 'btn-secondary'}`}
          style={{ borderRadius: 'var(--radius-md) var(--radius-md) 0 0', borderBottom: 'none' }}
        >
          Hồ sơ Huấn luyện viên ({trainers.length})
        </button>
      </div>

      {/* Tìm kiếm */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem 1.5rem' }}>
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '1rem' }}>
          <div style={{ flex: 1, position: 'relative' }}>
            <input
              type="text"
              className="form-input"
              placeholder={activeTab === 'MEMBERS' ? 'Tìm theo tên hoặc số điện thoại học viên...' : 'Tìm theo tên, SĐT hoặc chuyên môn HLV...'}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{ paddingLeft: '2.25rem' }}
            />
            <Search size={16} style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-subtle)' }} />
          </div>
          <button type="submit" className="btn btn-secondary">
            Tìm kiếm
          </button>
        </form>
      </div>

      {/* Bảng Học viên */}
      {activeTab === 'MEMBERS' && (
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Họ và tên</th>
                <th>Email</th>
                <th>Số điện thoại</th>
                <th>Giới tính</th>
                <th>Ngày sinh</th>
                <th>Địa chỉ</th>
                <th style={{ textAlign: 'right' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={7} style={{ textAlign: 'center', padding: '2rem' }}>Đang tải...</td></tr>
              ) : members.length === 0 ? (
                <tr><td colSpan={7} style={{ textAlign: 'center', padding: '2rem' }}>Chưa có hồ sơ học viên nào.</td></tr>
              ) : (
                members.map((m) => (
                  <tr key={m.userId}>
                    <td style={{ fontWeight: 600 }}>{m.fullName}</td>
                    <td>{m.email}</td>
                    <td>{m.phone}</td>
                    <td>{m.gender || '—'}</td>
                    <td>{m.dateOfBirth ? new Date(m.dateOfBirth).toLocaleDateString('vi-VN') : '—'}</td>
                    <td>{normalizeVietnameseText(m.address) || '—'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <button onClick={() => handleOpenEdit('MEMBER', m)} className="btn btn-secondary btn-sm">
                        <Edit3 size={14} /> Sửa
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Bảng HLV */}
      {activeTab === 'TRAINERS' && (
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Họ và tên</th>
                <th>Email</th>
                <th>Số điện thoại</th>
                <th>Chuyên môn Pilates</th>
                <th>Tiểu sử kinh nghiệm</th>
                <th style={{ textAlign: 'right' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={6} style={{ textAlign: 'center', padding: '2rem' }}>Đang tải...</td></tr>
              ) : trainers.length === 0 ? (
                <tr><td colSpan={6} style={{ textAlign: 'center', padding: '2rem' }}>Chưa có hồ sơ huấn luyện viên nào.</td></tr>
              ) : (
                trainers.map((t) => (
                  <tr key={t.userId}>
                    <td style={{ fontWeight: 600 }}>{t.fullName}</td>
                    <td>{t.email}</td>
                    <td>{t.phone}</td>
                    <td>
                      <span className="badge badge-trainer">{normalizeVietnameseText(t.specialization) || 'Pilates tổng quát'}</span>
                    </td>
                    <td style={{ maxWidth: '280px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {normalizeVietnameseText(t.bio) || '—'}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <button onClick={() => handleOpenEdit('TRAINER', t)} className="btn btn-secondary btn-sm">
                        <Edit3 size={14} /> Sửa
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal Chỉnh sửa hồ sơ */}
      {editingItem && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="card-header">
              <h3 className="card-title">
                Chỉnh sửa hồ sơ {editingItem.type === 'MEMBER' ? 'Học viên' : 'Huấn luyện viên'}
              </h3>
              <button onClick={() => setEditingItem(null)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleSaveEdit}>
              <div className="form-group">
                <label className="form-label">Họ và tên *</label>
                <input
                  type="text"
                  required
                  className="form-input"
                  value={editForm.fullName}
                  onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Số điện thoại *</label>
                <input
                  type="tel"
                  required
                  className="form-input"
                  value={editForm.phone}
                  onChange={(e) => setEditForm({ ...editForm, phone: e.target.value })}
                />
              </div>

              {editingItem.type === 'MEMBER' ? (
                <>
                  <div className="grid grid-cols-2">
                    <div className="form-group">
                      <label className="form-label">Giới tính</label>
                      <select
                        className="form-select"
                        value={editForm.gender}
                        onChange={(e) => setEditForm({ ...editForm, gender: e.target.value as Gender })}
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
                        value={editForm.dateOfBirth}
                        onChange={(e) => setEditForm({ ...editForm, dateOfBirth: e.target.value })}
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Địa chỉ</label>
                    <input
                      type="text"
                      className="form-input"
                      value={editForm.address}
                      onChange={(e) => setEditForm({ ...editForm, address: e.target.value })}
                    />
                  </div>
                </>
              ) : (
                <>
                  <div className="form-group">
                    <label className="form-label">Chuyên môn Pilates</label>
                    <input
                      type="text"
                      className="form-input"
                      value={editForm.specialization}
                      onChange={(e) => setEditForm({ ...editForm, specialization: e.target.value })}
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Tiểu sử kinh nghiệm</label>
                    <textarea
                      rows={3}
                      className="form-textarea"
                      value={editForm.bio}
                      onChange={(e) => setEditForm({ ...editForm, bio: e.target.value })}
                    />
                  </div>
                </>
              )}

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button type="button" onClick={() => setEditingItem(null)} className="btn btn-secondary">
                  Hủy
                </button>
                <button type="submit" className="btn btn-primary">
                  Lưu thay đổi
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

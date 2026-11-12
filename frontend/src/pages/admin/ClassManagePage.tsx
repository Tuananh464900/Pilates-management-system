import React, { useState, useEffect } from 'react';
import { apiClient } from '../../lib/apiClient';
import { ApiResponse, PageResponse } from '../../types/auth';
import { PilatesClass, ClassLevel, ClassStatus } from '../../types/pilates';
import { Plus, Edit3, X, CheckCircle2, AlertCircle, ToggleLeft, ToggleRight, Search } from 'lucide-react';
import { normalizeVietnameseText } from '../../lib/formatters';

export const ClassManagePage: React.FC = () => {
  const [classes, setClasses] = useState<PilatesClass[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [notification, setNotification] = useState<{ type: 'success' | 'danger'; message: string } | null>(null);

  // Modal State
  const [showModal, setShowModal] = useState(false);
  const [editingClass, setEditingClass] = useState<PilatesClass | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    defaultCapacity: 10,
    level: 'BEGINNER' as ClassLevel,
  });

  const fetchClasses = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (search) params.append('search', search);
      if (statusFilter) params.append('status', statusFilter);

      const res = await apiClient.get<ApiResponse<PageResponse<PilatesClass>>>(`/admin/classes?${params.toString()}`);
      if (res.data.success && res.data.data) {
        setClasses(res.data.data.items);
      }
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || 'Lỗi khi tải danh sách lớp Pilates' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchClasses();
  }, [statusFilter]);

  const handleOpenCreate = () => {
    setEditingClass(null);
    setFormData({ name: '', description: '', defaultCapacity: 10, level: 'BEGINNER' });
    setShowModal(true);
  };

  const handleOpenEdit = (c: PilatesClass) => {
    setEditingClass(c);
    setFormData({
      name: c.name,
      description: normalizeVietnameseText(c.description),
      defaultCapacity: c.defaultCapacity,
      level: c.level,
    });
    setShowModal(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingClass) {
        const res = await apiClient.put<ApiResponse<PilatesClass>>(`/admin/classes/${editingClass.id}`, {
          ...formData,
          status: editingClass.status,
        });
        if (res.data.success) {
          setNotification({ type: 'success', message: 'Cập nhật thông tin lớp Pilates thành công.' });
        }
      } else {
        const res = await apiClient.post<ApiResponse<PilatesClass>>('/admin/classes', formData);
        if (res.data.success) {
          setNotification({ type: 'success', message: 'Tạo lớp Pilates mới thành công.' });
        }
      }
      setShowModal(false);
      fetchClasses();
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || 'Lỗi khi lưu thông tin lớp.' });
    }
  };

  const handleToggleStatus = async (c: PilatesClass) => {
    const nextStatus: ClassStatus = c.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    const actionText = nextStatus === 'INACTIVE' ? 'ngừng hoạt động' : 'kích hoạt lại';

    if (!window.confirm(`Bạn có chắc chắn muốn ${actionText} lớp "${c.name}"?`)) {
      return;
    }

    try {
      const res = await apiClient.patch<ApiResponse<PilatesClass>>(`/admin/classes/${c.id}/status?status=${nextStatus}`);
      if (res.data.success) {
        setNotification({ type: 'success', message: `Đã ${actionText} lớp "${c.name}" thành công.` });
        fetchClasses();
      }
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || 'Lỗi khi cập nhật trạng thái lớp.' });
    }
  };

  return (
    <div className="main-content">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', marginBottom: '0.25rem' }}>Quản lý lớp Pilates</h1>
          <p>Danh mục các mẫu lớp, cấp độ và sức chứa mặc định</p>
        </div>

        <button onClick={handleOpenCreate} className="btn btn-primary">
          <Plus size={18} />
          <span>Thêm lớp Pilates mới</span>
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

      {/* Bộ lọc */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem 1.5rem' }}>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <div style={{ flex: 1, minWidth: '220px', position: 'relative' }}>
            <input
              type="text"
              className="form-input"
              placeholder="Tìm theo tên lớp Pilates..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchClasses()}
              style={{ paddingLeft: '2.25rem' }}
            />
            <Search size={16} style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-subtle)' }} />
          </div>

          <div style={{ width: '200px' }}>
            <select className="form-select" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="">Tất cả trạng thái</option>
              <option value="ACTIVE">ACTIVE (Đang hoạt động)</option>
              <option value="INACTIVE">INACTIVE (Tạm dừng)</option>
            </select>
          </div>
        </div>
      </div>

      {/* Bảng lớp học */}
      <div className="table-container">
        <table className="table">
          <thead>
            <tr>
              <th>Tên lớp Pilates</th>
              <th>Mô tả nội dung</th>
              <th>Cấp độ</th>
              <th>Sức chứa mặc định</th>
              <th>Trạng thái</th>
              <th style={{ textAlign: 'right' }}>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={6} style={{ textAlign: 'center', padding: '2rem' }}>Đang tải danh sách...</td></tr>
            ) : classes.length === 0 ? (
              <tr><td colSpan={6} style={{ textAlign: 'center', padding: '2rem' }}>Chưa có lớp Pilates nào.</td></tr>
            ) : (
              classes.map((c) => (
                <tr key={c.id}>
                  <td style={{ fontWeight: 600 }}>{c.name}</td>
                  <td style={{ maxWidth: '280px', color: 'var(--text-muted)' }}>{normalizeVietnameseText(c.description) || '—'}</td>
                  <td>
                    <span className="badge badge-trainer">{c.level}</span>
                  </td>
                  <td>{c.defaultCapacity} học viên</td>
                  <td>
                    <span className={`badge ${c.status === 'ACTIVE' ? 'badge-active' : 'badge-locked'}`}>
                      {c.status === 'ACTIVE' ? 'Đang hoạt động' : 'Tạm dừng'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                      <button onClick={() => handleOpenEdit(c)} className="btn btn-secondary btn-sm" title="Chỉnh sửa">
                        <Edit3 size={14} /> Sửa
                      </button>
                      <button
                        onClick={() => handleToggleStatus(c)}
                        className={`btn btn-sm ${c.status === 'ACTIVE' ? 'btn-danger' : 'btn-secondary'}`}
                        title={c.status === 'ACTIVE' ? 'Ngừng hoạt động' : 'Kích hoạt lại'}
                      >
                        {c.status === 'ACTIVE' ? 'Ngừng' : 'Mở lại'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Modal Thêm / Sửa lớp */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="card-header">
              <h3 className="card-title">
                {editingClass ? 'Chỉnh sửa lớp Pilates' : 'Thêm lớp Pilates mới'}
              </h3>
              <button onClick={() => setShowModal(false)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleSave}>
              <div className="form-group">
                <label className="form-label">Tên lớp Pilates *</label>
                <input
                  type="text"
                  required
                  className="form-input"
                  placeholder="Ví dụ: Reformer Cơ Bản, Mat Pilates Giãn Cơ..."
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                />
              </div>

              <div className="grid grid-cols-2">
                <div className="form-group">
                  <label className="form-label">Cấp độ lớp *</label>
                  <select
                    className="form-select"
                    value={formData.level}
                    onChange={(e) => setFormData({ ...formData, level: e.target.value as ClassLevel })}
                  >
                    <option value="BEGINNER">Cơ bản (BEGINNER)</option>
                    <option value="INTERMEDIATE">Trung cấp (INTERMEDIATE)</option>
                    <option value="ADVANCED">Nâng cao (ADVANCED)</option>
                    <option value="ALL_LEVELS">Mọi cấp độ (ALL_LEVELS)</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Sức chứa mặc định *</label>
                  <input
                    type="number"
                    required
                    min={1}
                    className="form-input"
                    value={formData.defaultCapacity}
                    onChange={(e) => setFormData({ ...formData, defaultCapacity: parseInt(e.target.value) || 1 })}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Mô tả chi tiết</label>
                <textarea
                  rows={3}
                  className="form-textarea"
                  placeholder="Nội dung luyện tập, lưu ý chuẩn bị..."
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button type="button" onClick={() => setShowModal(false)} className="btn btn-secondary">
                  Hủy
                </button>
                <button type="submit" className="btn btn-primary">
                  {editingClass ? 'Lưu thay đổi' : 'Tạo lớp học'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

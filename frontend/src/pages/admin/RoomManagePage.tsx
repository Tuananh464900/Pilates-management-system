import React, { useState, useEffect } from 'react';
import { apiClient } from '../../lib/apiClient';
import { ApiResponse, PageResponse } from '../../types/auth';
import { Room, RoomStatus } from '../../types/pilates';
import { Plus, Edit3, X, CheckCircle2, AlertCircle, Search } from 'lucide-react';
import { normalizeVietnameseText } from '../../lib/formatters';

export const RoomManagePage: React.FC = () => {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [notification, setNotification] = useState<{ type: 'success' | 'danger'; message: string } | null>(null);

  // Modal State
  const [showModal, setShowModal] = useState(false);
  const [editingRoom, setEditingRoom] = useState<Room | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    capacity: 10,
    description: '',
  });

  const fetchRooms = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (search) params.append('search', search);
      if (statusFilter) params.append('status', statusFilter);

      const res = await apiClient.get<ApiResponse<PageResponse<Room>>>(`/admin/rooms?${params.toString()}`);
      if (res.data.success && res.data.data) {
        setRooms(res.data.data.items);
      }
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || 'Lỗi khi tải danh sách phòng tập' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRooms();
  }, [statusFilter]);

  const handleOpenCreate = () => {
    setEditingRoom(null);
    setFormData({ name: '', capacity: 10, description: '' });
    setShowModal(true);
  };

  const handleOpenEdit = (r: Room) => {
    setEditingRoom(r);
    setFormData({
      name: r.name,
      capacity: r.capacity,
      description: normalizeVietnameseText(r.description),
    });
    setShowModal(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingRoom) {
        const res = await apiClient.put<ApiResponse<Room>>(`/admin/rooms/${editingRoom.id}`, {
          ...formData,
          status: editingRoom.status,
        });
        if (res.data.success) {
          setNotification({ type: 'success', message: 'Cập nhật phòng tập thành công.' });
        }
      } else {
        const res = await apiClient.post<ApiResponse<Room>>('/admin/rooms', formData);
        if (res.data.success) {
          setNotification({ type: 'success', message: 'Tạo phòng tập mới thành công.' });
        }
      }
      setShowModal(false);
      fetchRooms();
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || 'Lỗi khi lưu phòng tập.' });
    }
  };

  const handleToggleStatus = async (r: Room) => {
    const nextStatus: RoomStatus = r.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    const actionText = nextStatus === 'INACTIVE' ? 'ngừng hoạt động' : 'kích hoạt lại';

    if (!window.confirm(`Bạn có chắc chắn muốn ${actionText} phòng "${r.name}"?`)) {
      return;
    }

    try {
      const res = await apiClient.patch<ApiResponse<Room>>(`/admin/rooms/${r.id}/status?status=${nextStatus}`);
      if (res.data.success) {
        setNotification({ type: 'success', message: `Đã ${actionText} phòng "${r.name}" thành công.` });
        fetchRooms();
      }
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || 'Lỗi khi cập nhật trạng thái phòng.' });
    }
  };

  return (
    <div className="main-content">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', marginBottom: '0.25rem' }}>Quản lý phòng tập luyện</h1>
          <p>Danh sách các phòng máy Reformer, phòng Mat và sức chứa tối đa</p>
        </div>

        <button onClick={handleOpenCreate} className="btn btn-primary">
          <Plus size={18} />
          <span>Thêm phòng tập mới</span>
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
              placeholder="Tìm theo tên phòng tập..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchRooms()}
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

      {/* Bảng phòng tập */}
      <div className="table-container">
        <table className="table">
          <thead>
            <tr>
              <th>Tên phòng tập</th>
              <th>Sức chứa tối đa</th>
              <th>Mô tả / Thiết bị</th>
              <th>Trạng thái</th>
              <th style={{ textAlign: 'right' }}>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={5} style={{ textAlign: 'center', padding: '2rem' }}>Đang tải danh sách...</td></tr>
            ) : rooms.length === 0 ? (
              <tr><td colSpan={5} style={{ textAlign: 'center', padding: '2rem' }}>Chưa có phòng tập nào.</td></tr>
            ) : (
              rooms.map((r) => (
                <tr key={r.id}>
                  <td style={{ fontWeight: 600 }}>{r.name}</td>
                  <td>{r.capacity} máy / học viên</td>
                  <td style={{ maxWidth: '320px', color: 'var(--text-muted)' }}>{normalizeVietnameseText(r.description) || '—'}</td>
                  <td>
                    <span className={`badge ${r.status === 'ACTIVE' ? 'badge-active' : 'badge-locked'}`}>
                      {r.status === 'ACTIVE' ? 'Đang hoạt động' : 'Tạm dừng'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                      <button onClick={() => handleOpenEdit(r)} className="btn btn-secondary btn-sm" title="Chỉnh sửa">
                        <Edit3 size={14} /> Sửa
                      </button>
                      <button
                        onClick={() => handleToggleStatus(r)}
                        className={`btn btn-sm ${r.status === 'ACTIVE' ? 'btn-danger' : 'btn-secondary'}`}
                        title={r.status === 'ACTIVE' ? 'Ngừng hoạt động' : 'Kích hoạt lại'}
                      >
                        {r.status === 'ACTIVE' ? 'Ngừng' : 'Mở lại'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Modal Thêm / Sửa phòng */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="card-header">
              <h3 className="card-title">
                {editingRoom ? 'Chỉnh sửa phòng tập' : 'Thêm phòng tập mới'}
              </h3>
              <button onClick={() => setShowModal(false)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleSave}>
              <div className="form-group">
                <label className="form-label">Tên phòng tập *</label>
                <input
                  type="text"
                  required
                  className="form-input"
                  placeholder="Ví dụ: Phòng Reformer Studio 1..."
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Sức chứa tối đa (máy / học viên) *</label>
                <input
                  type="number"
                  required
                  min={1}
                  className="form-input"
                  value={formData.capacity}
                  onChange={(e) => setFormData({ ...formData, capacity: parseInt(e.target.value) || 1 })}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Mô tả thiết bị trong phòng</label>
                <textarea
                  rows={3}
                  className="form-textarea"
                  placeholder="Số lượng máy Reformer, gương, thảm..."
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button type="button" onClick={() => setShowModal(false)} className="btn btn-secondary">
                  Hủy
                </button>
                <button type="submit" className="btn btn-primary">
                  {editingRoom ? 'Lưu thay đổi' : 'Tạo phòng'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

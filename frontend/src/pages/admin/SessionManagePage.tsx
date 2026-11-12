import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../../lib/apiClient';
import { ApiResponse, PageResponse } from '../../types/auth';
import { Session, SessionStatus, PilatesClass, Room } from '../../types/pilates';
import { Plus, Edit3, X, CheckCircle2, AlertCircle, Calendar, Ban, User, AlertTriangle, CheckSquare } from 'lucide-react';

export const SessionManagePage: React.FC = () => {
  const navigate = useNavigate();
  const [sessions, setSessions] = useState<Session[]>([]);
  const [classes, setClasses] = useState<PilatesClass[]>([]);
  const [rooms, setRooms] = useState<Room[]>([]);
  const [trainers, setTrainers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [notification, setNotification] = useState<{ type: 'success' | 'danger'; message: string } | null>(null);

  // Modal Xếp lịch mới
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createForm, setCreateForm] = useState({
    classId: '',
    roomId: '',
    trainerId: '',
    date: '',
    startTime: '08:00',
    endTime: '09:00',
    capacity: 10,
  });

  // Modal Hủy buổi tập
  const [cancelingSession, setCancelingSession] = useState<Session | null>(null);
  const [cancelReason, setCancelReason] = useState('');

  const fetchDependencies = async () => {
    try {
      const [classRes, roomRes, trainerRes] = await Promise.all([
        apiClient.get<ApiResponse<PilatesClass[]>>('/classes'),
        apiClient.get<ApiResponse<Room[]>>('/rooms'),
        apiClient.get<ApiResponse<PageResponse<any>>>('/admin/trainers'),
      ]);

      if (classRes.data.success) setClasses(classRes.data.data);
      if (roomRes.data.success) setRooms(roomRes.data.data);
      if (trainerRes.data.success) setTrainers(trainerRes.data.data.items);
    } catch (e) {
      console.error('Lỗi khi tải danh mục lớp/phòng/HLV', e);
    }
  };

  const fetchSessions = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (statusFilter) params.append('status', statusFilter);
      params.append('size', '20');

      const res = await apiClient.get<ApiResponse<PageResponse<Session>>>(`/admin/sessions?${params.toString()}`);
      if (res.data.success && res.data.data) {
        setSessions(res.data.data.items);
      }
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || 'Lỗi khi tải danh sách buổi tập' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDependencies();
  }, []);

  useEffect(() => {
    fetchSessions();
  }, [statusFilter]);

  const handleOpenCreate = () => {
    // Ngày mặc định là ngày mai
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dateStr = tomorrow.toISOString().split('T')[0];

    setCreateForm({
      classId: classes[0]?.id.toString() || '',
      roomId: rooms[0]?.id.toString() || '',
      trainerId: trainers[0]?.userId || '',
      date: dateStr,
      startTime: '08:00',
      endTime: '09:00',
      capacity: classes[0]?.defaultCapacity || 10,
    });
    setShowCreateModal(true);
  };

  const handleClassChange = (classIdStr: string) => {
    const selected = classes.find(c => c.id.toString() === classIdStr);
    setCreateForm({
      ...createForm,
      classId: classIdStr,
      capacity: selected ? selected.defaultCapacity : createForm.capacity,
    });
  };

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const startIso = new Date(`${createForm.date}T${createForm.startTime}:00`).toISOString();
      const endIso = new Date(`${createForm.date}T${createForm.endTime}:00`).toISOString();

      const payload = {
        classId: parseInt(createForm.classId),
        roomId: parseInt(createForm.roomId),
        trainerId: createForm.trainerId,
        startTime: startIso,
        endTime: endIso,
        capacity: createForm.capacity,
      };

      const res = await apiClient.post<ApiResponse<Session>>('/admin/sessions', payload);
      if (res.data.success) {
        setNotification({ type: 'success', message: 'Tạo buổi tập mới và đưa vào lịch thành công!' });
        setShowCreateModal(false);
        fetchSessions();
      }
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || 'Lỗi khi tạo buổi tập' });
    }
  };

  const handleEditClick = (session: Session) => {
    if (session.bookedCount > 0) {
      alert(`QUY TẮC NGHIỆP VỤ:\nBuổi tập này đã có ${session.bookedCount} học viên đăng ký.\nKhông thể sửa trực tiếp thời gian, phòng hoặc Huấn luyện viên!\nNếu có thay đổi, vui lòng Hủy buổi tập và tạo buổi tập mới theo quy định.`);
      return;
    }
    alert('Buổi tập chưa có người đăng ký, bạn có thể chỉnh sửa.');
  };

  const handleConfirmCancel = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!cancelingSession) return;

    try {
      const res = await apiClient.post<ApiResponse<Session>>(`/admin/sessions/${cancelingSession.id}/cancel`, {
        reason: cancelReason,
      });

      if (res.data.success) {
        setNotification({
          type: 'success',
          message: `Đã hủy buổi tập #${cancelingSession.id}. Toàn bộ booking liên quan đã được hủy và hoàn trả lượt!`,
        });
        setCancelingSession(null);
        setCancelReason('');
        fetchSessions();
      }
    } catch (err: any) {
      setNotification({ type: 'danger', message: err.response?.data?.message || 'Lỗi khi hủy buổi tập' });
    }
  };

  return (
    <div className="main-content">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', marginBottom: '0.25rem' }}>Quản lý buổi tập & Xếp lịch</h1>
          <p>Tạo buổi tập cụ thể, phân công HLV/phòng, chống trùng lịch và hủy buổi</p>
        </div>

        <button onClick={handleOpenCreate} className="btn btn-primary">
          <Plus size={18} />
          <span>Tạo buổi tập mới</span>
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
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <label style={{ fontSize: '0.9rem', fontWeight: 600 }}>Lọc theo trạng thái:</label>
          <select className="form-select" style={{ maxWidth: '240px' }} value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="">Tất cả trạng thái</option>
            <option value="SCHEDULED">SCHEDULED (Đã lên lịch)</option>
            <option value="COMPLETED">COMPLETED (Đã kết thúc)</option>
            <option value="CANCELED">CANCELED (Đã hủy)</option>
          </select>
        </div>
      </div>

      {/* Bảng danh sách buổi tập */}
      <div className="table-container">
        <table className="table">
          <thead>
            <tr>
              <th>Lớp Pilates & Cấp độ</th>
              <th>Thời gian buổi tập</th>
              <th>Phòng tập</th>
              <th>Huấn luyện viên</th>
              <th>Đăng ký / Chỗ</th>
              <th>Trạng thái</th>
              <th style={{ textAlign: 'right' }}>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={7} style={{ textAlign: 'center', padding: '2rem' }}>Đang tải danh sách buổi tập...</td></tr>
            ) : sessions.length === 0 ? (
              <tr><td colSpan={7} style={{ textAlign: 'center', padding: '2rem' }}>Chưa có buổi tập nào được xếp lịch.</td></tr>
            ) : (
              sessions.map((s) => (
                <tr key={s.id}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{s.className}</div>
                    <span className="badge badge-trainer" style={{ fontSize: '0.7rem' }}>{s.classLevel}</span>
                  </td>
                  <td>
                    <div style={{ fontWeight: 600 }}>{new Date(s.startTime).toLocaleDateString('vi-VN')}</div>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                      {new Date(s.startTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} - {' '}
                      {new Date(s.endTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}
                    </div>
                  </td>
                  <td>{s.roomName}</td>
                  <td>
                    <div style={{ fontWeight: 500 }}>{s.trainerName}</div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{s.trainerPhone}</div>
                  </td>
                  <td>
                    <span style={{ fontWeight: 700, color: s.bookedCount >= s.capacity ? 'var(--danger)' : 'inherit' }}>
                      {s.bookedCount} / {s.capacity}
                    </span>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginLeft: 4 }}>
                      ({s.availableCount} chỗ trống)
                    </span>
                  </td>
                  <td>
                    <span className={`badge ${
                      s.status === 'SCHEDULED' ? 'badge-active' :
                      s.status === 'COMPLETED' ? 'badge-paid' : 'badge-canceled'
                    }`}>
                      {s.status === 'SCHEDULED' ? 'Đã lên lịch' :
                       s.status === 'COMPLETED' ? 'Hoàn thành' : 'Đã hủy'}
                    </span>
                    {s.cancelReason && (
                      <div style={{ fontSize: '0.75rem', color: 'var(--danger)', marginTop: 2 }}>
                        Lý do: {s.cancelReason}
                      </div>
                    )}
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div style={{ display: 'inline-flex', gap: '0.4rem' }}>
                      {s.status !== 'CANCELED' && (
                        <button
                          onClick={() => navigate(`/trainer/attendance/${s.id}`)}
                          className="btn btn-secondary btn-sm"
                          title="Điểm danh buổi tập"
                          style={{ color: 'var(--primary)', borderColor: 'var(--primary)' }}
                        >
                          <CheckSquare size={14} /> Điểm danh
                        </button>
                      )}
                      {s.status === 'SCHEDULED' && (
                        <>
                          <button onClick={() => handleEditClick(s)} className="btn btn-secondary btn-sm" title="Sửa buổi tập">
                            <Edit3 size={14} /> Sửa
                          </button>
                          <button onClick={() => setCancelingSession(s)} className="btn btn-danger btn-sm" title="Hủy buổi tập">
                            <Ban size={14} /> Hủy buổi
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Modal Xếp lịch buổi tập mới */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '580px' }}>
            <div className="card-header">
              <h3 className="card-title">Tạo buổi tập mới (Xếp lịch)</h3>
              <button onClick={() => setShowCreateModal(false)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleCreateSubmit}>
              <div className="form-group">
                <label className="form-label">Chọn lớp Pilates (ACTIVE) *</label>
                <select
                  required
                  className="form-select"
                  value={createForm.classId}
                  onChange={(e) => handleClassChange(e.target.value)}
                >
                  {classes.map(c => (
                    <option key={c.id} value={c.id}>
                      {c.name} ({c.level} - Sức chứa: {c.defaultCapacity} người)
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2">
                <div className="form-group">
                  <label className="form-label">Chọn phòng tập (ACTIVE) *</label>
                  <select
                    required
                    className="form-select"
                    value={createForm.roomId}
                    onChange={(e) => setCreateForm({ ...createForm, roomId: e.target.value })}
                  >
                    {rooms.map(r => (
                      <option key={r.id} value={r.id}>
                        {r.name} (Sức chứa tối đa: {r.capacity})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Phân công Huấn luyện viên *</label>
                  <select
                    required
                    className="form-select"
                    value={createForm.trainerId}
                    onChange={(e) => setCreateForm({ ...createForm, trainerId: e.target.value })}
                  >
                    {trainers.map(t => (
                      <option key={t.userId} value={t.userId}>
                        {t.fullName} ({t.specialization || 'HLV'})
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Ngày diễn ra buổi tập *</label>
                <input
                  type="date"
                  required
                  className="form-input"
                  value={createForm.date}
                  onChange={(e) => setCreateForm({ ...createForm, date: e.target.value })}
                />
              </div>

              <div className="grid grid-cols-3">
                <div className="form-group">
                  <label className="form-label">Giờ bắt đầu *</label>
                  <input
                    type="time"
                    required
                    className="form-input"
                    value={createForm.startTime}
                    onChange={(e) => setCreateForm({ ...createForm, startTime: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Giờ kết thúc *</label>
                  <input
                    type="time"
                    required
                    className="form-input"
                    value={createForm.endTime}
                    onChange={(e) => setCreateForm({ ...createForm, endTime: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Sức chứa buổi *</label>
                  <input
                    type="number"
                    required
                    min={1}
                    className="form-input"
                    value={createForm.capacity}
                    onChange={(e) => setCreateForm({ ...createForm, capacity: parseInt(e.target.value) || 1 })}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button type="button" onClick={() => setShowCreateModal(false)} className="btn btn-secondary">
                  Hủy
                </button>
                <button type="submit" className="btn btn-primary">
                  Lưu & Đưa vào lịch
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Xác nhận Hủy buổi tập */}
      {cancelingSession && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '500px' }}>
            <div className="card-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--danger)' }}>
                <AlertTriangle size={24} />
                <h3 className="card-title" style={{ color: 'var(--danger)' }}>Xác nhận Hủy buổi tập</h3>
              </div>
              <button onClick={() => setCancelingSession(null)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            <div style={{ marginBottom: '1.25rem', fontSize: '0.9rem', lineHeight: 1.6 }}>
              <p>Bạn đang thực hiện hủy buổi tập <strong>{cancelingSession.className}</strong> diễn ra lúc <strong>{new Date(cancelingSession.startTime).toLocaleString('vi-VN')}</strong>.</p>
              <div className="alert-banner alert-warning" style={{ marginTop: '0.75rem' }}>
                <strong>Lưu ý nghiệp vụ quan trọng:</strong> Khi buổi tập bị hủy, toàn bộ các lượt đăng ký (booking) còn hiệu lực của học viên sẽ tự động bị hủy và <strong>hoàn trả 1 lượt về đúng gói tập</strong> của từng học viên.
              </div>
            </div>

            <form onSubmit={handleConfirmCancel}>
              <div className="form-group">
                <label className="form-label">Lý do hủy buổi tập *</label>
                <textarea
                  required
                  rows={3}
                  className="form-textarea"
                  placeholder="Nhập lý do hủy (HLV bận đột xuất, bảo trì phòng máy...)"
                  value={cancelReason}
                  onChange={(e) => setCancelReason(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button type="button" onClick={() => setCancelingSession(null)} className="btn btn-secondary">
                  Đóng
                </button>
                <button type="submit" className="btn btn-danger">
                  Xác nhận Hủy buổi tập
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

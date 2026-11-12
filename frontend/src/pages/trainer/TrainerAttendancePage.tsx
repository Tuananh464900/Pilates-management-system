import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { apiClient } from '../../lib/apiClient';
import { ApiResponse } from '../../types/auth';
import { AttendanceRecordResponse } from '../../types/attendance';
import { SessionResponse } from '../../types/pilates';
import { Check, X, ArrowLeft } from 'lucide-react';

export const TrainerAttendancePage: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [session, setSession] = useState<SessionResponse | null>(null);
  const [attendances, setAttendances] = useState<AttendanceRecordResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Lưu trạng thái điểm danh cục bộ: Map<bookingId, 'ATTENDED' | 'ABSENT'>
  const [markedStatus, setMarkedStatus] = useState<Record<number, 'ATTENDED' | 'ABSENT'>>({});

  useEffect(() => {
    if (!sessionId) return;
    fetchSessionAndAttendance();
  }, [sessionId]);

  const fetchSessionAndAttendance = async () => {
    setLoading(true);
    setError(null);
    try {
      // 1. Lấy thông tin chi tiết buổi tập
      const sessionRes = await apiClient.get<ApiResponse<SessionResponse>>(`/sessions/${sessionId}`);
      if (!sessionRes.data.success || !sessionRes.data.data) {
        throw new Error(sessionRes.data.message || 'Không thể tải thông tin buổi tập');
      }
      setSession(sessionRes.data.data);

      // 2. Lấy danh sách điểm danh
      const attendRes = await apiClient.get<ApiResponse<AttendanceRecordResponse[]>>(`/attendance/sessions/${sessionId}`);
      if (!attendRes.data.success || !attendRes.data.data) {
        throw new Error(attendRes.data.message || 'Không thể tải danh sách điểm danh');
      }

      const records: AttendanceRecordResponse[] = attendRes.data.data;
      setAttendances(records);

      // Khởi tạo trạng thái ban đầu: nếu đã điểm danh trước đó thì giữ nguyên, mặc định là ATTENDED
      const initialMap: Record<number, 'ATTENDED' | 'ABSENT'> = {};
      records.forEach(r => {
        if (r.status === 'ABSENT') {
          initialMap[r.bookingId] = 'ABSENT';
        } else {
          initialMap[r.bookingId] = 'ATTENDED';
        }
      });
      setMarkedStatus(initialMap);

    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Có lỗi xảy ra khi tải dữ liệu');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = (bookingId: number, status: 'ATTENDED' | 'ABSENT') => {
    setMarkedStatus(prev => ({
      ...prev,
      [bookingId]: status
    }));
  };

  const handleSubmitAttendance = async (completeSession: boolean) => {
    if (!sessionId) return;

    const now = new Date();
    if (session && new Date(session.startTime) > now) {
      setError('Chưa đến giờ diễn ra buổi tập, không thể gửi điểm danh trước!');
      return;
    }

    setSubmitting(true);
    setError(null);
    setSuccessMsg(null);

    try {
      const items = Object.entries(markedStatus).map(([bId, status]) => ({
        bookingId: Number(bId),
        status
      }));

      const res = await apiClient.post<ApiResponse<AttendanceRecordResponse[]>>(`/attendance/sessions/${sessionId}`, {
        items,
        completeSession
      });

      if (!res.data.success || !res.data.data) {
        throw new Error(res.data.message || 'Không thể lưu kết quả điểm danh');
      }

      setAttendances(res.data.data);
      setSuccessMsg(
        completeSession
          ? 'Đã điểm danh và hoàn tất buổi tập thành công!'
          : 'Lưu kết quả điểm danh thành công!'
      );

      // Cập nhật lại session nếu hoàn tất
      if (completeSession && session) {
        setSession({ ...session, status: 'COMPLETED' });
      }
    } catch (err: any) {
      setError(err.message || 'Lỗi khi lưu điểm danh');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="main-content">
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <p>Đang tải thông tin điểm danh buổi tập...</p>
        </div>
      </div>
    );
  }

  if (error && !session) {
    return (
      <div className="main-content">
        <div className="alert alert-danger" style={{ marginBottom: '1.5rem' }}>
          {error}
        </div>
        <button className="btn btn-secondary" onClick={() => navigate('/schedule')}>
          ← Quay lại Lịch tập
        </button>
      </div>
    );
  }

  const isSessionStarted = session ? new Date(session.startTime) <= new Date() : false;
  const isTrainerAssigned = session && user ? session.trainerId === user.id || user.role === 'ADMIN' : false;

  return (
    <div className="main-content">
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <button
            onClick={() => navigate('/schedule')}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--primary)',
              cursor: 'pointer',
              fontSize: '0.95rem',
              fontWeight: 600,
              padding: 0,
              marginBottom: '0.5rem',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.25rem'
            }}
          >
            <ArrowLeft size={16} />
            <span>Quay lại Lịch tập</span>
          </button>
          <h1>Điểm danh Buổi tập Pilates</h1>
          <p style={{ marginTop: '0.25rem' }}>
            Ghi nhận học viên tham gia hoặc vắng mặt trong buổi học do bạn phụ trách.
          </p>
        </div>

        {session && (
          <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
            <span className={`badge ${session.status === 'COMPLETED' ? 'badge-completed' : session.status === 'SCHEDULED' ? 'badge-scheduled' : 'badge-canceled'}`}>
              {session.status === 'COMPLETED' ? 'Đã hoàn thành' : session.status === 'SCHEDULED' ? 'Đã lên lịch' : 'Đã hủy'}
            </span>
          </div>
        )}
      </div>

      {/* Thông tin buổi tập Card */}
      {session && (
        <div className="card" style={{ marginBottom: '2rem', background: 'var(--bg-surface-soft)' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem' }}>
            <div>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Lớp học:</span>
              <p style={{ fontWeight: 700, fontSize: '1.1rem', color: 'var(--primary)' }}>{session.className}</p>
            </div>
            <div>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Phòng tập:</span>
              <p style={{ fontWeight: 600 }}>{session.roomName}</p>
            </div>
            <div>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>HLV phụ trách:</span>
              <p style={{ fontWeight: 600 }}>{session.trainerName}</p>
            </div>
            <div>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Thời gian:</span>
              <p style={{ fontWeight: 600 }}>
                {new Date(session.startTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} - {new Date(session.endTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} ({new Date(session.startTime).toLocaleDateString('vi-VN')})
              </p>
            </div>
            <div>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Sĩ số đăng ký:</span>
              <p style={{ fontWeight: 600 }}>
                {session.bookedCount} / {session.capacity} học viên
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Thông báo nghiệp vụ */}
      {error && <div className="alert alert-danger" style={{ marginBottom: '1.5rem' }}>{error}</div>}
      {successMsg && <div className="alert alert-success" style={{ marginBottom: '1.5rem' }}>{successMsg}</div>}

      {!isSessionStarted && (
        <div className="alert alert-warning" style={{ marginBottom: '1.5rem' }}>
          <strong>Lưu ý nghiệp vụ:</strong> Buổi tập chưa đến giờ bắt đầu (bắt đầu lúc{' '}
          {session ? new Date(session.startTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : ''}
          ). Theo quy định hệ thống, bạn chỉ có thể điểm danh từ lúc buổi tập bắt đầu trở đi!
        </div>
      )}

      {!isTrainerAssigned && (
        <div className="alert alert-danger" style={{ marginBottom: '1.5rem' }}>
          <strong>Cảnh báo phân quyền:</strong> Bạn không phải Huấn luyện viên phụ trách buổi tập này, không có quyền thực hiện điểm danh.
        </div>
      )}

      <div className="alert alert-info" style={{ marginBottom: '1.5rem', background: 'var(--primary-subtle)', borderColor: 'var(--primary)' }}>
        <p style={{ color: 'var(--primary-hover)', margin: 0, fontSize: '0.9rem' }}>
          <strong>Quy tắc điểm danh:</strong> Khi học viên bị đánh dấu <strong>Vắng mặt (ABSENT)</strong>, hệ thống <strong>tuyệt đối không hoàn trả lại lượt tập</strong>. Hãy kiểm tra chính xác sự có mặt của học viên trước khi lưu!
        </p>
      </div>

      {/* Danh sách học viên đăng ký */}
      <div className="card">
        <h3 style={{ marginBottom: '1.25rem' }}>Danh sách học viên tham gia ({attendances.length})</h3>

        {attendances.length === 0 ? (
          <p style={{ textAlign: 'center', padding: '2rem 0', color: 'var(--text-muted)' }}>
            Chưa có học viên nào đăng ký tham gia buổi tập này.
          </p>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table className="table" style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid var(--border-light)', textAlign: 'left' }}>
                  <th style={{ padding: '0.75rem' }}>STT</th>
                  <th style={{ padding: '0.75rem' }}>Học viên</th>
                  <th style={{ padding: '0.75rem' }}>Liên hệ</th>
                  <th style={{ padding: '0.75rem' }}>Thời điểm đặt</th>
                  <th style={{ padding: '0.75rem' }}>Trạng thái hiện tại</th>
                  <th style={{ padding: '0.75rem', textAlign: 'center' }}>Điểm danh</th>
                </tr>
              </thead>
              <tbody>
                {attendances.map((item, index) => {
                  const currentSelection = markedStatus[item.bookingId] || 'ATTENDED';
                  return (
                    <tr key={item.bookingId} style={{ borderBottom: '1px solid var(--border-light)' }}>
                      <td style={{ padding: '0.75rem' }}>{index + 1}</td>
                      <td style={{ padding: '0.75rem' }}>
                        <div style={{ fontWeight: 600 }}>{item.memberName || 'Chưa cập nhật tên'}</div>
                        <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>{item.memberEmail}</div>
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        {item.memberPhone || <span style={{ color: 'var(--text-subtle)' }}>N/A</span>}
                      </td>
                      <td style={{ padding: '0.75rem', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                        {new Date(item.bookedAt).toLocaleString('vi-VN')}
                      </td>
                      <td style={{ padding: '0.75rem' }}>
                        <span className={`badge ${item.status === 'ATTENDED' ? 'badge-active' : item.status === 'ABSENT' ? 'badge-locked' : 'badge-scheduled'}`}>
                          {item.status === 'ATTENDED' ? 'Có mặt' : item.status === 'ABSENT' ? 'Vắng mặt' : 'Đã đăng ký'}
                        </span>
                      </td>
                      <td style={{ padding: '0.75rem', textAlign: 'center' }}>
                        <div style={{ display: 'inline-flex', gap: '0.5rem', background: 'var(--bg-surface-soft)', padding: '0.25rem', borderRadius: 'var(--radius-sm)' }}>
                          <button
                            type="button"
                            onClick={() => handleStatusChange(item.bookingId, 'ATTENDED')}
                            disabled={!isSessionStarted || !isTrainerAssigned || submitting}
                            style={{
                              padding: '0.4rem 0.85rem',
                              border: 'none',
                              borderRadius: 'var(--radius-sm)',
                              cursor: !isSessionStarted || !isTrainerAssigned ? 'not-allowed' : 'pointer',
                              fontWeight: 600,
                              fontSize: '0.85rem',
                              background: currentSelection === 'ATTENDED' ? 'var(--success)' : 'transparent',
                              color: currentSelection === 'ATTENDED' ? '#fff' : 'var(--text-main)',
                              transition: 'var(--transition)',
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: 4
                            }}
                          >
                            <Check size={14} />
                            <span>Có mặt</span>
                          </button>
                          <button
                            type="button"
                            onClick={() => handleStatusChange(item.bookingId, 'ABSENT')}
                            disabled={!isSessionStarted || !isTrainerAssigned || submitting}
                            style={{
                              padding: '0.4rem 0.85rem',
                              border: 'none',
                              borderRadius: 'var(--radius-sm)',
                              cursor: !isSessionStarted || !isTrainerAssigned ? 'not-allowed' : 'pointer',
                              fontWeight: 600,
                              fontSize: '0.85rem',
                              background: currentSelection === 'ABSENT' ? 'var(--danger)' : 'transparent',
                              color: currentSelection === 'ABSENT' ? '#fff' : 'var(--text-main)',
                              transition: 'var(--transition)',
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: 4
                            }}
                          >
                            <X size={14} />
                            <span>Vắng mặt</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {/* Nút thao tác gửi điểm danh */}
        {attendances.length > 0 && isTrainerAssigned && (
          <div style={{ marginTop: '2rem', display: 'flex', justifyContent: 'flex-end', gap: '1rem', flexWrap: 'wrap' }}>
            <button
              type="button"
              className="btn btn-secondary"
              disabled={!isSessionStarted || submitting}
              onClick={() => handleSubmitAttendance(false)}
            >
              {submitting ? 'Đang lưu...' : 'Lưu kết quả điểm danh'}
            </button>
            <button
              type="button"
              className="btn btn-primary"
              disabled={!isSessionStarted || submitting}
              onClick={() => handleSubmitAttendance(true)}
            >
              {submitting ? 'Đang xử lý...' : 'Lưu & Hoàn tất buổi tập'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

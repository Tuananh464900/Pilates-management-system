import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../lib/apiClient';
import { useAuth } from '../context/AuthContext';
import { Session, PilatesClass } from '../types/pilates';
import {
  Calendar,
  ChevronLeft,
  ChevronRight,
  Clock,
  MapPin,
  User,
  Users,
  Filter,
  CheckCircle2,
  AlertCircle,
  XCircle,
  Info
} from 'lucide-react';

interface TrainerOption {
  userId?: string;
  id?: string;
  fullName: string;
  specialization?: string;
}

export const SchedulePage: React.FC = () => {
  const navigate = useNavigate();
  const { role } = useAuth();

  // Tuần hiện tại: tính ngày bắt đầu tuần (Thứ 2)
  const [currentWeekStart, setCurrentWeekStart] = useState<Date>(() => {
    const now = new Date();
    const day = now.getDay();
    const diff = now.getDate() - day + (day === 0 ? -6 : 1); // Thứ 2 là ngày bắt đầu
    const monday = new Date(now.setDate(diff));
    monday.setHours(0, 0, 0, 0);
    return monday;
  });

  const [sessions, setSessions] = useState<Session[]>([]);
  const [classes, setClasses] = useState<PilatesClass[]>([]);
  const [trainers, setTrainers] = useState<TrainerOption[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMsg, setErrorMsg] = useState<string>('');

  // Bộ lọc
  const [selectedClassId, setSelectedClassId] = useState<string>('');
  const [selectedTrainerId, setSelectedTrainerId] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<string>('');

  // Modal chi tiết
  const [selectedSession, setSelectedSession] = useState<Session | null>(null);

  // State đặt chỗ
  const [bookingLoading, setBookingLoading] = useState<boolean>(false);
  const [bookingSuccessMsg, setBookingSuccessMsg] = useState<string>('');
  const [bookingErrorMsg, setBookingErrorMsg] = useState<string>('');

  // State danh sách học viên cho Trainer/Admin
  const [sessionAttendees, setSessionAttendees] = useState<any[]>([]);
  const [attendeesLoading, setAttendeesLoading] = useState<boolean>(false);

  // Tải danh sách học viên đã đặt chỗ
  const fetchAttendees = async (sessionId: number) => {
    setAttendeesLoading(true);
    try {
      const endpoint = role === 'ADMIN'
        ? `/api/v1/admin/sessions/${sessionId}/bookings`
        : `/api/v1/trainer/sessions/${sessionId}/bookings`;
      const res = await apiClient.get(endpoint);
      if (res.data.success) {
        setSessionAttendees(res.data.data);
      }
    } catch (err) {
      console.error('Không thể tải học viên', err);
    } finally {
      setAttendeesLoading(false);
    }
  };

  useEffect(() => {
    if (selectedSession && (role === 'TRAINER' || role === 'ADMIN')) {
      fetchAttendees(selectedSession.id);
    } else {
      setSessionAttendees([]);
    }
    setBookingSuccessMsg('');
    setBookingErrorMsg('');
  }, [selectedSession, role]);

  // Đặt chỗ buổi tập
  const handleBookSession = async (sessionId: number) => {
    setBookingLoading(true);
    setBookingSuccessMsg('');
    setBookingErrorMsg('');
    try {
      const res = await apiClient.post('/api/v1/bookings', { sessionId });
      if (res.data.success) {
        setBookingSuccessMsg('Đặt chỗ thành công! Đã trừ 1 lượt vào gói tập của bạn.');
        fetchSchedule(); // Cập nhật lại số chỗ còn lại
      }
    } catch (err: any) {
      setBookingErrorMsg(err.response?.data?.message || 'Không thể đặt chỗ buổi tập này.');
    } finally {
      setBookingLoading(false);
    }
  };

  // Tính ngày kết thúc tuần (Chủ nhật 23:59:59)
  const currentWeekEnd = useMemo(() => {
    const end = new Date(currentWeekStart);
    end.setDate(end.getDate() + 6);
    end.setHours(23, 59, 59, 999);
    return end;
  }, [currentWeekStart]);

  // Nạp danh sách bộ lọc: Lớp tập & Huấn luyện viên
  useEffect(() => {
    const loadFilterOptions = async () => {
      try {
        const [classesRes, trainersRes] = await Promise.all([
          apiClient.get('/api/v1/classes'),
          apiClient.get('/api/v1/auth/trainers').catch(() => ({ data: { data: [] } }))
        ]);
        if (classesRes.data.success) {
          setClasses(classesRes.data.data.filter((c: PilatesClass) => c.status === 'ACTIVE'));
        }
        if (trainersRes.data.success) {
          setTrainers(trainersRes.data.data);
        }
      } catch (err) {
        console.error('Không thể nạp dữ liệu bộ lọc', err);
      }
    };
    loadFilterOptions();
  }, []);

  // Nạp danh sách thời khóa biểu theo khoảng ngày
  const fetchSchedule = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const params = new URLSearchParams();
      params.append('fromDate', currentWeekStart.toISOString());
      params.append('toDate', currentWeekEnd.toISOString());
      if (selectedTrainerId && selectedTrainerId.trim() !== '') {
        params.append('trainerId', selectedTrainerId.trim());
      }
      if (selectedClassId && selectedClassId.trim() !== '') {
        params.append('classId', selectedClassId.trim());
      }

      const res = await apiClient.get(`/api/v1/sessions/schedule?${params.toString()}`);
      if (res.data.success) {
        setSessions(res.data.data);
      }
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Không thể tải lịch tập');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSchedule();
  }, [currentWeekStart, selectedClassId, selectedTrainerId]);

  // Điều hướng tuần
  const handlePreviousWeek = () => {
    const prev = new Date(currentWeekStart);
    prev.setDate(prev.getDate() - 7);
    setCurrentWeekStart(prev);
  };

  const handleNextWeek = () => {
    const next = new Date(currentWeekStart);
    next.setDate(next.getDate() + 7);
    setCurrentWeekStart(next);
  };

  const handleTodayWeek = () => {
    const now = new Date();
    const day = now.getDay();
    const diff = now.getDate() - day + (day === 0 ? -6 : 1);
    const monday = new Date(now.setDate(diff));
    monday.setHours(0, 0, 0, 0);
    setCurrentWeekStart(monday);
  };

  // Lọc thêm theo trạng thái ở client nếu người dùng chọn
  const filteredSessions = useMemo(() => {
    if (!selectedStatus) return sessions;
    return sessions.filter((s) => s.status === selectedStatus);
  }, [sessions, selectedStatus]);

  // Tạo danh sách 7 ngày trong tuần
  const weekDays = useMemo(() => {
    const days = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(currentWeekStart);
      d.setDate(d.getDate() + i);
      days.push(d);
    }
    return days;
  }, [currentWeekStart]);

  const getDayName = (d: Date) => {
    const names = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
    return names[d.getDay()];
  };

  const isToday = (d: Date) => {
    const today = new Date();
    return (
      d.getDate() === today.getDate() &&
      d.getMonth() === today.getMonth() &&
      d.getFullYear() === today.getFullYear()
    );
  };

  // Định dạng ngày theo múi giờ địa phương YYYY-MM-DD để gom nhóm chính xác
  const formatDateKey = (d: Date) => {
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  // Gom nhóm sessions theo từng ngày
  const sessionsByDay = useMemo(() => {
    const map = new Map<string, Session[]>();
    weekDays.forEach((d) => {
      const dateStr = formatDateKey(d);
      map.set(dateStr, []);
    });

    filteredSessions.forEach((s) => {
      const sDateStr = formatDateKey(new Date(s.startTime));
      if (map.has(sDateStr)) {
        map.get(sDateStr)?.push(s);
      }
    });

    // Sắp xếp các buổi theo giờ bắt đầu
    map.forEach((list) => {
      list.sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());
    });

    return map;
  }, [weekDays, filteredSessions]);

  return (
    <main className="main-content">
      {/* Tiêu đề & Giới thiệu */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '2rem', flexWrap: 'wrap', gap: '1.25rem' }}>
        <div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.45rem', padding: '0.3rem 0.85rem', background: 'var(--primary-light)', borderRadius: 'var(--radius-full)', color: 'var(--primary)', fontSize: '0.78rem', fontWeight: 700, letterSpacing: '0.04em', textTransform: 'uppercase', marginBottom: '0.6rem' }}>
            <Calendar size={13} />
            <span>Thời khóa biểu tuần</span>
          </div>
          <h1 style={{ margin: 0, fontSize: '2.2rem', fontFamily: 'var(--font-serif)', fontWeight: 700, color: 'var(--text-main)', letterSpacing: '-0.02em' }}>
            Lịch tập Pilates
          </h1>
          <p style={{ margin: '0.35rem 0 0 0', color: 'var(--text-muted)', fontSize: '0.96rem' }}>
            Khám phá lịch các lớp tập Reformer, Mat và đặt chỗ tiện lợi theo tuần.
          </p>
        </div>

        {/* Nút điều hướng tuần */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', background: '#FFFFFF', padding: '0.45rem 0.75rem', borderRadius: 'var(--radius-full)', border: '1.5px solid var(--border-light)', boxShadow: 'var(--shadow-sm)' }}>
          <button
            onClick={handlePreviousWeek}
            className="btn btn-secondary btn-sm"
            style={{ borderRadius: 'var(--radius-full)', padding: '0.4rem 0.6rem' }}
            title="Tuần trước"
          >
            <ChevronLeft size={16} />
          </button>
          <button
            onClick={handleTodayWeek}
            className="btn btn-secondary btn-sm"
            style={{ borderRadius: 'var(--radius-full)', fontWeight: 600, fontSize: '0.82rem', padding: '0.4rem 0.85rem' }}
          >
            Hôm nay
          </button>
          <button
            onClick={handleNextWeek}
            className="btn btn-secondary btn-sm"
            style={{ borderRadius: 'var(--radius-full)', padding: '0.4rem 0.6rem' }}
            title="Tuần sau"
          >
            <ChevronRight size={16} />
          </button>
          <div style={{ width: 1, height: 18, background: 'var(--border-light)', margin: '0 0.25rem' }} />
          <span style={{ fontWeight: 700, fontSize: '0.88rem', padding: '0.2rem 0.6rem', color: 'var(--primary)', background: 'var(--primary-light)', borderRadius: 'var(--radius-full)' }}>
            {currentWeekStart.toLocaleDateString('vi-VN')} – {currentWeekEnd.toLocaleDateString('vi-VN')}
          </span>
        </div>
      </div>

      {/* Thanh bộ lọc */}
      <div className="card" style={{ marginBottom: '2rem', padding: '1.5rem', borderRadius: 'var(--radius-lg)', background: '#FFFFFF', border: '1.5px solid var(--border-light)', boxShadow: 'var(--shadow-sm)' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-light)', paddingBottom: '0.85rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <div style={{ width: 34, height: 34, borderRadius: '50%', background: 'var(--primary-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--primary)' }}>
              <Filter size={17} />
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: '1rem', color: 'var(--text-main)' }}>Bộ lọc lịch tập</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Lọc nhanh theo lớp tập, huấn luyện viên hoặc trạng thái</div>
            </div>
          </div>
          <span style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-muted)', background: 'var(--bg-surface-soft)', padding: '0.3rem 0.75rem', borderRadius: 'var(--radius-full)' }}>
            {filteredSessions.length} buổi tập tìm thấy
          </span>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem' }}>
          <div>
            <label className="form-label">Lớp tập Pilates</label>
            <select
              className="form-select"
              value={selectedClassId}
              onChange={(e) => setSelectedClassId(e.target.value)}
            >
              <option value="">-- Tất cả lớp tập --</option>
              {classes.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name} ({c.level})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="form-label">Huấn luyện viên phụ trách</label>
            <select
              className="form-select"
              value={selectedTrainerId}
              onChange={(e) => setSelectedTrainerId(e.target.value)}
            >
              <option value="">-- Tất cả HLV --</option>
              {trainers.map((t) => {
                const val = t.userId || t.id || '';
                return (
                  <option key={val} value={val}>
                    {t.fullName} {t.specialization ? `• ${t.specialization}` : ''}
                  </option>
                );
              })}
            </select>
          </div>

          <div>
            <label className="form-label">Trạng thái buổi tập</label>
            <select
              className="form-select"
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
            >
              <option value="">-- Tất cả trạng thái --</option>
              <option value="SCHEDULED">Đã lên lịch (Sắp diễn ra)</option>
              <option value="COMPLETED">Đã hoàn thành</option>
              <option value="CANCELED">Đã hủy</option>
            </select>
          </div>
        </div>
      </div>

      {errorMsg && (
        <div className="alert alert-danger" style={{ marginBottom: '1.5rem' }}>
          <AlertCircle size={18} />
          <span>{errorMsg}</span>
        </div>
      )}

      {/* Lưới lịch theo 7 ngày trong tuần */}
      {loading ? (
        <div className="card" style={{ textAlign: 'center', padding: '4rem 2rem', color: 'var(--text-muted)' }}>
          <div style={{ display: 'inline-block', width: 36, height: 36, border: '3px solid var(--primary-light)', borderTopColor: 'var(--primary)', borderRadius: '50%', animation: 'spin 0.8s linear infinite', marginBottom: '1rem' }} />
          <div style={{ fontWeight: 600, fontSize: '1rem' }}>Đang tải lịch tập tuần này...</div>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1.25rem', alignItems: 'start' }}>
          {weekDays.map((date) => {
            const dateStr = formatDateKey(date);
            const daySessions = sessionsByDay.get(dateStr) || [];
            const isCurrentDate = isToday(date);

            return (
              <div
                key={dateStr}
                style={{
                  background: '#FFFFFF',
                  borderRadius: '18px',
                  border: isCurrentDate ? '2px solid var(--primary)' : '1.5px solid var(--border-light)',
                  boxShadow: isCurrentDate ? '0 8px 24px rgba(200, 90, 116, 0.12)' : 'var(--shadow-sm)',
                  overflow: 'hidden',
                  display: 'flex',
                  flexDirection: 'column',
                  minHeight: '400px',
                  transition: 'var(--transition)'
                }}
              >
                {/* Header ngày */}
                <div
                  style={{
                    padding: '1rem 1.15rem',
                    background: isCurrentDate
                      ? 'linear-gradient(135deg, #FFF5F6 0%, #FCEEEF 100%)'
                      : 'var(--bg-surface-soft)',
                    borderBottom: isCurrentDate ? '1.5px solid #F7D4DA' : '1px solid var(--border-light)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 700, fontSize: '1.02rem', color: isCurrentDate ? 'var(--primary)' : 'var(--text-main)' }}>
                      {getDayName(date)}
                    </div>
                    <div style={{ fontSize: '0.82rem', color: isCurrentDate ? 'var(--primary-dark)' : 'var(--text-muted)', fontWeight: 500, marginTop: 2 }}>
                      {date.toLocaleDateString('vi-VN')}
                    </div>
                  </div>
                  {isCurrentDate ? (
                    <span style={{ fontSize: '0.72rem', fontWeight: 700, textTransform: 'uppercase', padding: '0.25rem 0.65rem', borderRadius: 'var(--radius-full)', background: 'var(--primary)', color: '#FFFFFF', letterSpacing: '0.04em' }}>
                      Hôm nay
                    </span>
                  ) : (
                    <span style={{ fontSize: '0.76rem', fontWeight: 600, color: 'var(--text-subtle)' }}>
                      {daySessions.length} buổi
                    </span>
                  )}
                </div>

                {/* Danh sách buổi tập trong ngày */}
                <div style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '0.85rem', flex: 1 }}>
                  {daySessions.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '3.5rem 1rem', color: 'var(--text-subtle)', fontSize: '0.88rem', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.5rem' }}>
                      <Calendar size={28} style={{ opacity: 0.35, color: 'var(--primary)' }} />
                      <span>Chưa có buổi tập</span>
                    </div>
                  ) : (
                    daySessions.map((s) => {
                      const startTime = new Date(s.startTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
                      const endTime = new Date(s.endTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
                      const isFull = s.availableCount <= 0;

                      let statusBadge = <span className="badge badge-active" style={{ fontSize: '0.7rem' }}>Sắp diễn ra</span>;
                      if (s.status === 'COMPLETED') statusBadge = <span className="badge badge-paid" style={{ fontSize: '0.7rem' }}>Đã xong</span>;
                      if (s.status === 'CANCELED') statusBadge = <span className="badge badge-canceled" style={{ fontSize: '0.7rem' }}>Đã hủy</span>;

                      return (
                        <div
                          key={s.id}
                          onClick={() => setSelectedSession(s)}
                          style={{
                            background: s.status === 'CANCELED' ? 'var(--danger-bg)' : '#FFFDFD',
                            border: '1.5px solid var(--border-light)',
                            borderRadius: '14px',
                            padding: '0.95rem',
                            cursor: 'pointer',
                            transition: 'all 0.2s cubic-bezier(0.16, 1, 0.3, 1)',
                            boxShadow: '0 2px 6px rgba(180, 130, 140, 0.04)'
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.transform = 'translateY(-2px)';
                            e.currentTarget.style.boxShadow = '0 6px 16px rgba(200, 90, 116, 0.12)';
                            e.currentTarget.style.borderColor = 'var(--primary)';
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.transform = 'translateY(0)';
                            e.currentTarget.style.boxShadow = '0 2px 6px rgba(180, 130, 140, 0.04)';
                            e.currentTarget.style.borderColor = 'var(--border-light)';
                          }}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.55rem' }}>
                            <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--primary)', display: 'flex', alignItems: 'center', gap: '5px' }}>
                              <Clock size={13} />
                              {startTime} – {endTime}
                            </span>
                            {statusBadge}
                          </div>

                          <div style={{ fontWeight: 700, fontSize: '0.98rem', color: 'var(--text-main)', marginBottom: '0.5rem', lineHeight: 1.3 }}>
                            {s.className}
                          </div>

                          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem', fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                              <MapPin size={13} style={{ color: 'var(--primary)' }} />
                              <span>{s.roomName}</span>
                            </div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                              <User size={13} style={{ color: 'var(--primary)' }} />
                              <span>HLV: {s.trainerName}</span>
                            </div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '5px', marginTop: '0.25rem', fontWeight: 600, color: isFull ? 'var(--danger)' : 'var(--text-main)' }}>
                              <Users size={13} style={{ color: isFull ? 'var(--danger)' : 'var(--success)' }} />
                              <span>
                                Còn {s.availableCount}/{s.capacity} chỗ {isFull && '(Hết chỗ)'}
                              </span>
                            </div>
                          </div>
                        </div>
                      );
                    })
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Modal chi tiết buổi tập */}
      {selectedSession && (
        <div className="modal-backdrop" onClick={() => setSelectedSession(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '560px' }}>
            <div className="modal-header">
              <div>
                <span style={{ fontSize: '0.78rem', textTransform: 'uppercase', letterSpacing: '0.04em', fontWeight: 700, color: 'var(--primary)' }}>
                  Thông tin lịch tập
                </span>
                <h3 style={{ margin: '0.2rem 0 0 0', fontSize: '1.35rem', fontFamily: 'var(--font-serif)', fontWeight: 700, color: 'var(--text-main)' }}>
                  Chi tiết buổi tập #{selectedSession.id}
                </h3>
              </div>
              <button
                onClick={() => setSelectedSession(null)}
                className="btn btn-secondary btn-sm"
                style={{ borderRadius: '50%', width: 34, height: 34, padding: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
              >
                &times;
              </button>
            </div>

            <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'var(--bg-surface-soft)', padding: '0.9rem 1.25rem', borderRadius: 'var(--radius-md)' }}>
                <div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Lớp Pilates</div>
                  <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--primary)' }}>
                    {selectedSession.className}
                  </div>
                </div>
                <span className={`badge badge-${selectedSession.status.toLowerCase()}`}>
                  {selectedSession.status === 'SCHEDULED' ? 'Sắp diễn ra' : selectedSession.status === 'COMPLETED' ? 'Đã hoàn thành' : 'Đã hủy'}
                </span>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.85rem', background: '#FFFFFF', border: '1.5px solid var(--border-light)', padding: '1.15rem', borderRadius: 'var(--radius-md)' }}>
                <div>
                  <div style={{ fontSize: '0.76rem', color: 'var(--text-muted)', fontWeight: 600 }}>Thời gian</div>
                  <div style={{ fontWeight: 700, fontSize: '0.94rem', color: 'var(--text-main)', marginTop: 2 }}>
                    {new Date(selectedSession.startTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} – {new Date(selectedSession.endTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    {new Date(selectedSession.startTime).toLocaleDateString('vi-VN')}
                  </div>
                </div>

                <div>
                  <div style={{ fontSize: '0.76rem', color: 'var(--text-muted)', fontWeight: 600 }}>Phòng tập</div>
                  <div style={{ fontWeight: 700, fontSize: '0.94rem', color: 'var(--text-main)', marginTop: 2 }}>{selectedSession.roomName}</div>
                </div>

                <div>
                  <div style={{ fontSize: '0.76rem', color: 'var(--text-muted)', fontWeight: 600 }}>Huấn luyện viên</div>
                  <div style={{ fontWeight: 700, fontSize: '0.94rem', color: 'var(--text-main)', marginTop: 2 }}>{selectedSession.trainerName}</div>
                  {selectedSession.trainerPhone && (
                    <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>SĐT: {selectedSession.trainerPhone}</div>
                  )}
                </div>

                <div>
                  <div style={{ fontSize: '0.76rem', color: 'var(--text-muted)', fontWeight: 600 }}>Tình trạng chỗ</div>
                  <div style={{ fontWeight: 700, fontSize: '0.94rem', color: selectedSession.availableCount > 0 ? 'var(--success)' : 'var(--danger)', marginTop: 2 }}>
                    Còn {selectedSession.availableCount} / {selectedSession.capacity} chỗ
                  </div>
                </div>
              </div>

              {selectedSession.cancelReason && (
                <div className="alert alert-danger" style={{ margin: 0 }}>
                  <XCircle size={16} />
                  <span><strong>Lý do hủy buổi:</strong> {selectedSession.cancelReason}</span>
                </div>
              )}

              {bookingSuccessMsg && (
                <div className="alert alert-success" style={{ margin: 0 }}>
                  <CheckCircle2 size={16} />
                  <span>{bookingSuccessMsg}</span>
                </div>
              )}

              {bookingErrorMsg && (
                <div className="alert alert-danger" style={{ margin: 0 }}>
                  <AlertCircle size={16} />
                  <span>{bookingErrorMsg}</span>
                </div>
              )}

              {/* Danh sách học viên cho TRAINER & ADMIN */}
              {(role === 'TRAINER' || role === 'ADMIN') && (
                <div style={{ borderTop: '1px solid var(--border-light)', paddingTop: '1rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                    <span style={{ fontWeight: 700, fontSize: '0.92rem', color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: 6 }}>
                      <Users size={15} style={{ color: 'var(--primary)' }} />
                      <span>Học viên đã đăng ký ({sessionAttendees.length})</span>
                    </span>
                    <button
                      onClick={() => fetchAttendees(selectedSession.id)}
                      className="btn btn-secondary btn-sm"
                      style={{ fontSize: '0.75rem', padding: '0.25rem 0.65rem', borderRadius: 'var(--radius-full)' }}
                    >
                      Tải lại
                    </button>
                  </div>

                  {attendeesLoading ? (
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', textAlign: 'center', padding: '1rem' }}>Đang tải danh sách...</div>
                  ) : sessionAttendees.length === 0 ? (
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-subtle)', textAlign: 'center', padding: '1rem', background: 'var(--bg-surface-soft)', borderRadius: 'var(--radius-md)' }}>
                      Chưa có học viên nào đặt chỗ buổi này.
                    </div>
                  ) : (
                    <div style={{ maxHeight: '180px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '0.45rem' }}>
                      {sessionAttendees.map((att, idx) => (
                        <div key={att.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'var(--bg-surface-soft)', padding: '0.55rem 0.85rem', borderRadius: 'var(--radius-sm)', fontSize: '0.85rem' }}>
                          <div>
                            <strong>{idx + 1}. {att.memberName || 'Học viên'}</strong>
                            <span style={{ color: 'var(--text-muted)', marginLeft: 6 }}>({att.memberEmail})</span>
                            {att.memberPhone && <span style={{ color: 'var(--text-muted)', marginLeft: 6 }}>- SĐT: {att.memberPhone}</span>}
                          </div>
                          <span className={`badge badge-${att.status.toLowerCase()}`} style={{ fontSize: '0.7rem' }}>
                            {att.status}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>

            <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button
                onClick={() => setSelectedSession(null)}
                className="btn btn-secondary"
                style={{ borderRadius: 'var(--radius-full)' }}
              >
                Đóng
              </button>
              {role === 'MEMBER' && selectedSession.status === 'SCHEDULED' && selectedSession.availableCount > 0 && (
                <button
                  onClick={() => handleBookSession(selectedSession.id)}
                  className="btn btn-primary"
                  style={{ borderRadius: 'var(--radius-full)' }}
                  disabled={bookingLoading}
                >
                  {bookingLoading ? 'Đang đặt chỗ...' : 'Đặt chỗ ngay (-1 lượt)'}
                </button>
              )}
              {(role === 'TRAINER' || role === 'ADMIN') && selectedSession.status !== 'CANCELED' && (
                <button
                  onClick={() => navigate(`/trainer/attendance/${selectedSession.id}`)}
                  className="btn btn-primary"
                  style={{ borderRadius: 'var(--radius-full)' }}
                >
                  Điểm danh học viên
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </main>
  );
};

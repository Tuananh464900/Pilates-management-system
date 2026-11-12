import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../../lib/apiClient';
import { Booking } from '../../types/booking';
import {
  Calendar,
  Clock,
  MapPin,
  User,
  AlertCircle,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  RotateCcw,
  Sparkles
} from 'lucide-react';

export const MyBookingsPage: React.FC = () => {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMsg, setErrorMsg] = useState<string>('');
  const [successMsg, setSuccessMsg] = useState<string>('');
  const [filterStatus, setFilterStatus] = useState<string>('CONFIRMED');

  // Modal hủy booking
  const [cancelingBooking, setCancelingBooking] = useState<Booking | null>(null);
  const [cancelReason, setCancelReason] = useState<string>('Có việc đột xuất');
  const [submitting, setSubmitting] = useState<boolean>(false);

  const fetchMyBookings = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const res = await apiClient.get('/api/v1/bookings/my-history?size=50');
      if (res.data.success) {
        setBookings(res.data.data.items);
      }
    } catch (err: any) {
      setErrorMsg('Không thể tải lịch tập của bạn');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyBookings();
  }, []);

  const handleConfirmCancel = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!cancelingBooking) return;

    setSubmitting(true);
    setErrorMsg('');
    try {
      const res = await apiClient.post(`/api/v1/bookings/${cancelingBooking.id}/cancel`, {
        reason: cancelReason
      });

      if (res.data.success) {
        setSuccessMsg(res.data.message || 'Hủy đăng ký buổi tập thành công!');
        setCancelingBooking(null);
        fetchMyBookings();
        setTimeout(() => setSuccessMsg(''), 5000);
      }
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Không thể hủy đăng ký buổi tập');
    } finally {
      setSubmitting(false);
    }
  };

  const filteredBookings = bookings.filter((b) => {
    if (!filterStatus) return true;
    return b.status === filterStatus;
  });

  return (
    <main className="main-content">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ margin: 0, fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-color)' }}>
            Lịch tập đã đặt của tôi
          </h1>
          <p style={{ margin: '0.25rem 0 0 0', color: 'var(--text-muted)' }}>
            Xem các buổi tập Pilates đã đăng ký, quản lý hoặc hủy đặt chỗ theo quy tắc hoàn lượt
          </p>
        </div>

        <Link to="/schedule" className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <Sparkles size={16} />
          <span>Đặt thêm buổi tập mới</span>
        </Link>
      </div>

      {successMsg && (
        <div className="alert alert-success" style={{ marginBottom: '1.5rem' }}>
          <CheckCircle2 size={18} />
          <span>{successMsg}</span>
        </div>
      )}

      {errorMsg && (
        <div className="alert alert-danger" style={{ marginBottom: '1.5rem' }}>
          <AlertCircle size={18} />
          <span>{errorMsg}</span>
        </div>
      )}

      {/* Bộ lọc trạng thái */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem' }}>
        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          <button
            onClick={() => setFilterStatus('CONFIRMED')}
            className={`btn btn-sm ${filterStatus === 'CONFIRMED' ? 'btn-primary' : 'btn-secondary'}`}
          >
            Sắp diễn ra
          </button>
          <button
            onClick={() => setFilterStatus('ATTENDED')}
            className={`btn btn-sm ${filterStatus === 'ATTENDED' ? 'btn-primary' : 'btn-secondary'}`}
          >
            Đã tham gia
          </button>
          <button
            onClick={() => setFilterStatus('CANCELED')}
            className={`btn btn-sm ${filterStatus === 'CANCELED' ? 'btn-primary' : 'btn-secondary'}`}
          >
            Đã hủy
          </button>
          <button
            onClick={() => setFilterStatus('')}
            className={`btn btn-sm ${filterStatus === '' ? 'btn-primary' : 'btn-secondary'}`}
          >
            Tất cả
          </button>
        </div>
      </div>

      {/* Danh sách lịch tập */}
      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3.5rem 1.5rem', color: 'var(--text-muted)' }}>
            Đang tải lịch tập của bạn...
          </div>
        ) : filteredBookings.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '3.5rem 1.5rem', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
            <div style={{ width: 64, height: 64, borderRadius: '50%', background: 'rgba(216, 75, 119, 0.08)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--primary)' }}>
              <Calendar size={32} />
            </div>
            <div>
              <h3 style={{ margin: '0 0 0.5rem 0', fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-color)' }}>
                {bookings.length === 0 ? 'Bạn chưa đăng ký buổi tập nào' : 'Không có buổi tập nào phù hợp với bộ lọc'}
              </h3>
              <p style={{ margin: 0, color: 'var(--text-muted)', maxWidth: 520, fontSize: '0.95rem', lineHeight: 1.6 }}>
                {bookings.length === 0 
                  ? 'Gói tập đã cấp lượt tập cho bạn. Hãy vào Thời khóa biểu của phòng tập để chọn ca tập, phòng và HLV mong muốn rồi bấm Đặt chỗ nhé!'
                  : 'Hãy thử chọn mục "Tất cả" hoặc thay đổi bộ lọc trạng thái để xem các buổi tập khác.'}
              </p>
            </div>
            {bookings.length === 0 && (
              <Link to="/schedule" className="btn btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: 8, marginTop: '0.5rem' }}>
                <Sparkles size={16} />
                <span>Xem Thời khóa biểu & Đặt chỗ ngay</span>
              </Link>
            )}
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>Lớp tập</th>
                  <th>Thời gian</th>
                  <th>Phòng tập</th>
                  <th>Huấn luyện viên</th>
                  <th>Gói tập đã dùng</th>
                  <th>Trạng thái</th>
                  <th style={{ textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {filteredBookings.map((b) => {
                  let statusBadge = <span className="badge badge-success">Sắp diễn ra</span>;
                  if (b.status === 'ATTENDED') statusBadge = <span className="badge badge-primary">Đã tham gia</span>;
                  if (b.status === 'ABSENT') statusBadge = <span className="badge badge-warning">Vắng mặt</span>;
                  if (b.status === 'CANCELED') {
                    statusBadge = (
                      <span className="badge badge-danger">
                        Đã hủy {b.refunded ? '(Đã hoàn 1 lượt)' : '(Không hoàn lượt)'}
                      </span>
                    );
                  }

                  return (
                    <tr key={b.id}>
                      <td>
                        <div style={{ fontWeight: 700, color: 'var(--primary-color)' }}>{b.className}</div>
                        <span className="badge badge-secondary" style={{ fontSize: '0.7rem' }}>{b.classLevel}</span>
                      </td>
                      <td>
                        <div style={{ fontWeight: 600 }}>
                          {new Date(b.startTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} - {new Date(b.endTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}
                        </div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                          {new Date(b.startTime).toLocaleDateString('vi-VN')}
                        </div>
                      </td>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                          <MapPin size={14} color="var(--text-muted)" />
                          <span>{b.roomName}</span>
                        </div>
                      </td>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                          <User size={14} color="var(--text-muted)" />
                          <span>{b.trainerName}</span>
                        </div>
                      </td>
                      <td>
                        <span style={{ fontSize: '0.85rem' }}>{b.packageName}</span>
                      </td>
                      <td>{statusBadge}</td>
                      <td style={{ textAlign: 'right' }}>
                        {b.canCancel && (
                          <button
                            onClick={() => {
                              setCancelingBooking(b);
                              setCancelReason('Có việc đột xuất');
                            }}
                            className="btn btn-secondary btn-sm"
                            style={{ color: 'var(--danger-color)' }}
                            title="Hủy đặt chỗ buổi tập này"
                          >
                            Hủy buổi
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal Hủy đặt chỗ (Áp dụng quy tắc biên 2 giờ) */}
      {cancelingBooking && (
        <div className="modal-backdrop" onClick={() => setCancelingBooking(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '480px' }}>
            <div className="modal-header">
              <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700, color: 'var(--danger-color)' }}>
                Hủy đăng ký buổi tập
              </h3>
              <button onClick={() => setCancelingBooking(null)} className="btn btn-secondary btn-sm">
                &times;
              </button>
            </div>

            <form onSubmit={handleConfirmCancel}>
              <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div style={{ background: 'var(--bg-muted, #f8fafc)', padding: '0.75rem', borderRadius: 'var(--radius)' }}>
                  <div><strong>Lớp:</strong> {cancelingBooking.className}</div>
                  <div><strong>Thời gian:</strong> {new Date(cancelingBooking.startTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} ngày {new Date(cancelingBooking.startTime).toLocaleDateString('vi-VN')}</div>
                  <div><strong>Phòng:</strong> {cancelingBooking.roomName} • <strong>HLV:</strong> {cancelingBooking.trainerName}</div>
                </div>

                {/* THÔNG BÁO QUY TẮC BIÊN 2 GIỜ */}
                {cancelingBooking.isRefundable ? (
                  <div className="alert alert-success" style={{ margin: 0, fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: 6 }}>
                    <CheckCircle2 size={18} />
                    <span>
                      Bạn đang hủy trước giờ tập <strong>ít nhất 2 giờ</strong>. Hệ thống sẽ <strong>hoàn trả 1 lượt tập</strong> về đúng gói tập của bạn!
                    </span>
                  </div>
                ) : (
                  <div className="alert alert-warning" style={{ margin: 0, fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: 6, borderLeft: '4px solid #f59e0b' }}>
                    <AlertTriangle size={18} />
                    <span>
                      <strong>Cảnh báo hủy sát giờ:</strong> Thời gian đến buổi tập <strong>ít hơn 2 giờ</strong>. Theo quy định phòng tập, bạn <strong>sẽ KHÔNG được hoàn lại lượt tập</strong>.
                    </span>
                  </div>
                )}

                <div>
                  <label className="form-label">Lý do hủy buổi tập (Tùy chọn)</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Nhập lý do hủy..."
                    value={cancelReason}
                    onChange={(e) => setCancelReason(e.target.value)}
                  />
                </div>
              </div>

              <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
                <button
                  type="button"
                  onClick={() => setCancelingBooking(null)}
                  className="btn btn-secondary"
                  disabled={submitting}
                >
                  Quay lại
                </button>
                <button
                  type="submit"
                  className="btn btn-danger"
                  disabled={submitting}
                >
                  {submitting ? 'Đang hủy...' : 'Xác nhận HỦY BUỔI'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </main>
  );
};

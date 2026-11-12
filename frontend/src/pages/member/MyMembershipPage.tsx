import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../../lib/apiClient';
import { MemberMembership } from '../../types/membership';
import {
  CreditCard,
  AlertTriangle,
  Clock,
  Calendar,
  CheckCircle2,
  RefreshCw,
  History,
  Sparkles,
  ArrowRight
} from 'lucide-react';

export const MyMembershipPage: React.FC = () => {
  const [activeMembership, setActiveMembership] = useState<MemberMembership | null>(null);
  const [historyMemberships, setHistoryMemberships] = useState<MemberMembership[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMsg, setErrorMsg] = useState<string>('');

  const fetchMembershipData = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const [activeRes, historyRes] = await Promise.all([
        apiClient.get('/api/v1/memberships/my-active'),
        apiClient.get('/api/v1/memberships/my-history?size=10')
      ]);

      if (activeRes.data.success) {
        setActiveMembership(activeRes.data.data);
      }
      if (historyRes.data.success) {
        setHistoryMemberships(historyRes.data.data.items);
      }
    } catch (err: any) {
      setErrorMsg('Không thể tải thông tin gói tập');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMembershipData();
  }, []);

  return (
    <main className="main-content">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ margin: 0, fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-color)' }}>
            Gói tập của tôi
          </h1>
          <p style={{ margin: '0.25rem 0 0 0', color: 'var(--text-muted)' }}>
            Theo dõi số lượt tập còn lại, thời hạn sử dụng và lịch sử các gói đã tham gia
          </p>
        </div>

        <Link to="/buy-package" className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <Sparkles size={16} />
          <span>Gia hạn / Mua gói tập</span>
        </Link>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
          Đang tải dữ liệu gói tập...
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* 1. KHỐI CẢNH BÁO HẾT HẠN HOẶC HẾT LƯỢT */}
          {activeMembership && (activeMembership.warningDays || activeMembership.warningSessions) && (
            <div className="alert alert-warning" style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', borderLeft: '4px solid #f59e0b' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 700, fontSize: '1rem', color: '#b45309' }}>
                <AlertTriangle size={20} />
                <span>CẢNH BÁO GIA HẠN GÓI TẬP:</span>
              </div>
              <ul style={{ margin: '0 0 0 1.5rem', padding: 0, color: '#92400e', fontSize: '0.95rem' }}>
                {activeMembership.warningSessions && (
                  <li>
                    Bạn chỉ còn <strong>{activeMembership.remainingSessions} lượt tập</strong>. Hãy gia hạn cùng loại gói để không gián đoạn lịch luyện tập!
                  </li>
                )}
                {activeMembership.warningDays && (
                  <li>
                    Gói tập của bạn sẽ hết hạn trong <strong>{activeMembership.daysRemaining} ngày</strong> tới (Hạn đến {new Date(activeMembership.endDate).toLocaleDateString('vi-VN')}).
                  </li>
                )}
              </ul>
              <div style={{ marginTop: '0.25rem' }}>
                <Link to="/buy-package" className="btn btn-sm btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                  <span>Gia hạn gói '{activeMembership.packageName}' ngay</span>
                  <ArrowRight size={14} />
                </Link>
              </div>
            </div>
          )}

          {/* 2. THẺ GÓI TẬP ĐANG HOẠT ĐỘNG */}
          <div className="card" style={{ background: activeMembership ? 'linear-gradient(135deg, rgba(79, 70, 229, 0.05) 0%, rgba(255, 255, 255, 1) 100%)' : 'var(--surface-color)', border: activeMembership ? '2px solid var(--primary-color)' : '1px solid var(--border-color)' }}>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: '0 0 1rem 0', display: 'flex', alignItems: 'center', gap: 8 }}>
              <CreditCard size={20} color="var(--primary-color)" />
              <span>Gói tập đang hoạt động (ACTIVE)</span>
            </h2>

            {activeMembership ? (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '2rem', alignItems: 'flex-start' }}>
                <div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.35rem' }}>Tên gói tập</div>
                  <div style={{ fontSize: '1.35rem', fontWeight: 800, color: 'var(--primary)', marginBottom: '0.4rem', lineHeight: 1.3 }}>
                    {activeMembership.packageName}
                  </div>
                  <span className="badge badge-success">Đang hoạt động</span>
                </div>

                <div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.35rem' }}>Số buổi tập còn lại</div>
                  <div style={{ fontSize: '1.6rem', fontWeight: 800, color: activeMembership.remainingSessions <= 2 ? 'var(--danger)' : 'var(--primary)', marginBottom: '0.35rem', lineHeight: 1 }}>
                    {activeMembership.remainingSessions} buổi
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    {activeMembership.remainingSessions > activeMembership.totalSessions ? (
                      <span style={{ color: 'var(--success)', fontWeight: 600 }}>✓ Đã cộng dồn gia hạn gói</span>
                    ) : (
                      <span>Đã sử dụng: {Math.max(0, activeMembership.totalSessions - activeMembership.remainingSessions)} buổi</span>
                    )}
                  </div>
                </div>

                <div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.35rem' }}>Thời hạn sử dụng</div>
                  <div style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.35rem', lineHeight: 1.2 }}>
                    Còn {activeMembership.daysRemaining} ngày
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    Từ {new Date(activeMembership.startDate).toLocaleDateString('vi-VN')} đến {new Date(activeMembership.endDate).toLocaleDateString('vi-VN')}
                  </div>
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', height: '100%', minHeight: '60px' }}>
                  <Link to="/schedule" className="btn btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: 6, whiteSpace: 'nowrap' }}>
                    <Calendar size={16} />
                    <span>Đặt lịch tập ngay</span>
                  </Link>
                </div>
              </div>
            ) : (
              <div style={{ textAlign: 'center', padding: '2rem 1rem' }}>
                <p style={{ color: 'var(--text-muted)', fontSize: '1.05rem', margin: '0 0 1rem 0' }}>
                  Bạn chưa có gói tập nào đang hoạt động. Vui lòng đăng ký gói để bắt đầu đặt lịch tập Pilates.
                </p>
                <Link to="/buy-package" className="btn btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                  <Sparkles size={16} />
                  <span>Xem bảng giá & Đăng ký gói tập</span>
                </Link>
              </div>
            )}
          </div>

          {/* 3. LỊCH SỬ CÁC GÓI TẬP ĐÃ TỪNG SỬ DỤNG */}
          <div className="card">
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: '0 0 1rem 0', display: 'flex', alignItems: 'center', gap: 8 }}>
              <History size={20} color="var(--primary-color)" />
              <span>Lịch sử các gói tập</span>
            </h2>

            {historyMemberships.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                Chưa có lịch sử gói tập nào.
              </div>
            ) : (
              <div className="table-responsive">
                <table className="table" style={{ width: '100%', tableLayout: 'fixed' }}>
                  <thead>
                    <tr>
                      <th style={{ width: '10%' }}>Mã thẻ</th>
                      <th style={{ width: '28%' }}>Gói tập</th>
                      <th style={{ width: '22%' }}>Số lượt</th>
                      <th style={{ width: '15%' }}>Thời gian bắt đầu</th>
                      <th style={{ width: '13%' }}>Ngày hết hạn</th>
                      <th style={{ width: '12%' }}>Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody>
                    {historyMemberships.map((m) => {
                      let badge = <span className="badge badge-success">ACTIVE</span>;
                      if (m.status === 'EXPIRED') badge = <span className="badge badge-secondary">Hết hạn (EXPIRED)</span>;
                      if (m.status === 'EXHAUSTED') badge = <span className="badge badge-warning">Hết buổi (EXHAUSTED)</span>;
                      if (m.status === 'CANCELED') badge = <span className="badge badge-danger">Đã hủy (CANCELED)</span>;

                      return (
                        <tr key={m.id}>
                          <td style={{ fontWeight: 600 }}>#{m.id}</td>
                          <td style={{ fontWeight: 600, color: 'var(--primary-color)' }}>{m.packageName}</td>
                          <td>
                            {m.remainingSessions > m.totalSessions ? (
                              <span><strong>{m.remainingSessions}</strong> lượt <span style={{ color: 'var(--success)', fontSize: '0.75rem', fontWeight: 600 }}>(Đã gia hạn)</span></span>
                            ) : (
                              <span>{m.remainingSessions} / {m.totalSessions} lượt</span>
                            )}
                          </td>
                          <td>{new Date(m.startDate).toLocaleDateString('vi-VN')}</td>
                          <td>{new Date(m.endDate).toLocaleDateString('vi-VN')}</td>
                          <td>{badge}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </main>
  );
};

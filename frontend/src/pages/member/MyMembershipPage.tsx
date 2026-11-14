import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../../lib/apiClient';
import { MemberMembership, Payment } from '../../types/membership';
import {
  CreditCard,
  AlertTriangle,
  Clock,
  Calendar,
  CheckCircle2,
  RefreshCw,
  History,
  Sparkles,
  ArrowRight,
  Layers,
  Receipt
} from 'lucide-react';

export const MyMembershipPage: React.FC = () => {
  const [activeMembership, setActiveMembership] = useState<MemberMembership | null>(null);
  const [historyMemberships, setHistoryMemberships] = useState<MemberMembership[]>([]);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMsg, setErrorMsg] = useState<string>('');

  const fetchMembershipData = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const [activeRes, historyRes, paymentsRes] = await Promise.all([
        apiClient.get('/api/v1/memberships/my-active'),
        apiClient.get('/api/v1/memberships/my-history?size=10'),
        apiClient.get('/api/v1/payments/my-history?size=50')
      ]);

      if (activeRes.data.success) {
        setActiveMembership(activeRes.data.data);
      }
      if (historyRes.data.success) {
        setHistoryMemberships(historyRes.data.data.items);
      }
      if (paymentsRes.data.success) {
        setPayments(paymentsRes.data.data.items);
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

  const getPackageStats = (m: MemberMembership | null) => {
    if (!m) {
      return {
        packageCount: 0,
        renewalCount: 0,
        totalAccumulated: 0,
        usedSessions: 0,
        remainingSessions: 0,
        paidPayments: [] as Payment[]
      };
    }

    const paidPayments = payments.filter(
      p => p.status === 'PAID' && (p.membershipId === m.id || p.packageId === m.packageId)
    );

    const baseSessions = m.totalSessions || 1;
    const countFromPayments = paidPayments.length;
    const estimatedCount = m.remainingSessions > baseSessions
      ? Math.ceil(m.remainingSessions / baseSessions)
      : 1;

    const packageCount = m.packageCount || Math.max(countFromPayments, estimatedCount, 1);
    const renewalCount = m.renewalCount !== undefined ? m.renewalCount : Math.max(0, packageCount - 1);
    const totalAccumulated = m.totalAccumulatedSessions || (packageCount * baseSessions);
    const usedSessions = m.usedSessions !== undefined ? m.usedSessions : Math.max(0, totalAccumulated - m.remainingSessions);

    return {
      packageCount,
      renewalCount,
      totalAccumulated,
      usedSessions,
      remainingSessions: m.remainingSessions,
      paidPayments
    };
  };

  const formatPrice = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

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

            {activeMembership ? (() => {
              const stats = getPackageStats(activeMembership);
              return (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '1.5rem', alignItems: 'flex-start' }}>
                  {/* Cột 1: Tên gói */}
                  <div>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.35rem' }}>Gói tập đang dùng</div>
                    <div style={{ fontSize: '1.35rem', fontWeight: 800, color: 'var(--primary)', marginBottom: '0.4rem', lineHeight: 1.3 }}>
                      {activeMembership.packageName}
                    </div>
                    <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', alignItems: 'center' }}>
                      <span className="badge badge-success">Đang hoạt động</span>
                      <span className="badge badge-secondary" style={{ fontSize: '0.75rem' }}>
                        {activeMembership.totalSessions} buổi / gói
                      </span>
                    </div>
                  </div>

                  {/* Cột 2: Số gói đã đăng ký (Theo yêu cầu hiển thị số gói và số lần đăng ký) */}
                  <div>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.35rem' }}>Số gói đã đăng ký</div>
                    <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-color)', marginBottom: '0.35rem', lineHeight: 1 }}>
                      {stats.packageCount} gói
                    </div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--success)', fontWeight: 600 }}>
                      {stats.renewalCount > 0 ? (
                        <span>✓ Đã đăng ký {stats.packageCount} lần (1 mua mới + {stats.renewalCount} gia hạn)</span>
                      ) : (
                        <span>✓ Đã đăng ký 1 lần (Gói mua mới)</span>
                      )}
                    </div>
                  </div>

                  {/* Cột 3: Số buổi tập còn lại & Tình trạng sử dụng */}
                  <div>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.35rem' }}>Số buổi tập còn lại</div>
                    <div style={{ fontSize: '1.6rem', fontWeight: 800, color: activeMembership.remainingSessions <= 2 ? 'var(--danger)' : 'var(--primary)', marginBottom: '0.35rem', lineHeight: 1 }}>
                      {activeMembership.remainingSessions} buổi
                    </div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      Tổng cấp: <strong>{stats.totalAccumulated} buổi</strong> • Đã dùng: {stats.usedSessions} buổi
                    </div>
                  </div>

                  {/* Cột 4: Thời hạn sử dụng */}
                  <div>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.35rem' }}>Thời hạn sử dụng</div>
                    <div style={{ fontSize: '1.35rem', fontWeight: 700, marginBottom: '0.35rem', lineHeight: 1.2 }}>
                      Còn {activeMembership.daysRemaining} ngày
                    </div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      Từ {new Date(activeMembership.startDate).toLocaleDateString('vi-VN')} đến {new Date(activeMembership.endDate).toLocaleDateString('vi-VN')}
                    </div>
                  </div>

                  {/* Cột 5: Đặt lịch tập ngay */}
                  <div style={{ display: 'flex', justifyContent: 'flex-start', alignItems: 'center', height: '100%', minHeight: '60px' }}>
                    <Link to="/schedule" className="btn btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: 6, whiteSpace: 'nowrap' }}>
                      <Calendar size={16} />
                      <span>Đặt lịch tập ngay</span>
                    </Link>
                  </div>
                </div>
              );
            })() : (
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
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.5rem' }}>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0, display: 'flex', alignItems: 'center', gap: 8 }}>
                <History size={20} color="var(--primary-color)" />
                <span>Lịch sử các gói tập</span>
              </h2>
              <Link to="/member/payments" style={{ fontSize: '0.85rem', color: 'var(--primary)', fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                <span>Xem lịch sử hóa đơn thanh toán</span>
                <ArrowRight size={14} />
              </Link>
            </div>

            {historyMemberships.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                Chưa có lịch sử gói tập nào.
              </div>
            ) : (
              <div className="table-responsive">
                <table className="table" style={{ width: '100%' }}>
                  <thead>
                    <tr>
                      <th style={{ width: '8%' }}>Mã thẻ</th>
                      <th style={{ width: '22%' }}>Gói tập</th>
                      <th style={{ width: '18%' }}>Số gói đã đăng ký</th>
                      <th style={{ width: '16%' }}>Tổng buổi cấp</th>
                      <th style={{ width: '18%' }}>Đã dùng / Còn lại</th>
                      <th style={{ width: '18%' }}>Thời hạn sử dụng</th>
                      <th style={{ width: '10%' }}>Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody>
                    {historyMemberships.map((m) => {
                      const mStats = getPackageStats(m);
                      let badge = <span className="badge badge-success">ACTIVE</span>;
                      if (m.status === 'EXPIRED') badge = <span className="badge badge-secondary">Hết hạn (EXPIRED)</span>;
                      if (m.status === 'EXHAUSTED') badge = <span className="badge badge-warning">Hết buổi (EXHAUSTED)</span>;
                      if (m.status === 'CANCELED') badge = <span className="badge badge-danger">Đã hủy (CANCELED)</span>;

                      return (
                        <tr key={m.id}>
                          <td style={{ fontWeight: 600 }}>#{m.id}</td>
                          <td>
                            <div style={{ fontWeight: 700, color: 'var(--primary-color)' }}>{m.packageName}</div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{m.totalSessions} buổi/gói</div>
                          </td>
                          <td>
                            <span className="badge badge-primary" style={{ fontSize: '0.8rem' }}>
                              {mStats.packageCount} gói
                            </span>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: 2 }}>
                              {mStats.renewalCount > 0 ? `(1 mua + ${mStats.renewalCount} gia hạn)` : '(1 mua mới)'}
                            </div>
                          </td>
                          <td>
                            <strong>{mStats.totalAccumulated}</strong> buổi
                          </td>
                          <td>
                            <div>Đã dùng: <strong>{mStats.usedSessions}</strong> buổi</div>
                            <div style={{ color: 'var(--primary)', fontWeight: 600 }}>Còn lại: {m.remainingSessions} buổi</div>
                          </td>
                          <td>
                            <div>{new Date(m.startDate).toLocaleDateString('vi-VN')}</div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>đến {new Date(m.endDate).toLocaleDateString('vi-VN')}</div>
                          </td>
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

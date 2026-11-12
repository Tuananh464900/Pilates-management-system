import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../../lib/apiClient';
import { ApiResponse } from '../../types/auth';
import { AdminDashboardResponse } from '../../types/stats';
import { RefreshCw, DollarSign, Users, Activity, Calendar, CreditCard, Package, Layers, DoorClosed, Filter } from 'lucide-react';

export const AdminDashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const now = new Date();
  const [selectedMonth, setSelectedMonth] = useState<number>(now.getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState<number>(now.getFullYear());
  const [data, setData] = useState<AdminDashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchDashboard(selectedMonth, selectedYear);
  }, []);

  const fetchDashboard = async (month?: number, year?: number) => {
    setLoading(true);
    setError(null);
    try {
      const m = month !== undefined ? month : selectedMonth;
      const y = year !== undefined ? year : selectedYear;
      const res = await apiClient.get<ApiResponse<AdminDashboardResponse>>('/statistics/admin/dashboard', {
        params: { month: m, year: y }
      });
      if (res.data.success && res.data.data) {
        setData(res.data.data);
      } else {
        throw new Error(res.data.message || 'Không thể tải dữ liệu Dashboard');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Đã có lỗi xảy ra khi tải báo cáo thống kê');
    } finally {
      setLoading(false);
    }
  };

  const handleFilterSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchDashboard(selectedMonth, selectedYear);
  };

  const formatCurrency = (amount?: number) => {
    if (amount === undefined || amount === null) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  if (loading && !data) {
    return (
      <div className="main-content">
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <p>Đang tải dữ liệu Dashboard Quản trị viên...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="main-content">
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1>Báo cáo Thống kê & Dashboard Quản trị</h1>
          <p style={{ marginTop: '0.25rem' }}>
            Tổng hợp các chỉ số hiệu quả kinh doanh, hội viên và vận hành lớp học của trung tâm Pilates.
          </p>
        </div>
        <button className="btn btn-secondary" onClick={() => fetchDashboard()} style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
          <RefreshCw size={15} />
          <span>Cập nhật số liệu</span>
        </button>
      </div>

      {error && <div className="alert alert-danger" style={{ marginBottom: '1.5rem' }}>{error}</div>}

      {/* UC5.4 & R5.5: Bộ lọc Doanh thu theo Tháng / Năm */}
      <div className="card" style={{ marginBottom: '2rem', padding: '1rem 1.25rem', background: 'var(--bg-surface-soft)' }}>
        <form onSubmit={handleFilterSubmit} style={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>
            <Filter size={18} />
            <span>Kỳ thống kê doanh thu:</span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <label style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>Tháng:</label>
            <select
              value={selectedMonth}
              onChange={(e) => setSelectedMonth(Number(e.target.value))}
              className="form-select"
              style={{ width: '130px', padding: '0.4rem 0.6rem' }}
            >
              {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                <option key={m} value={m}>
                  Tháng {m}
                </option>
              ))}
            </select>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <label style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>Năm:</label>
            <select
              value={selectedYear}
              onChange={(e) => setSelectedYear(Number(e.target.value))}
              className="form-select"
              style={{ width: '110px', padding: '0.4rem 0.6rem' }}
            >
              {[2024, 2025, 2026, 2027].map((y) => (
                <option key={y} value={y}>
                  {y}
                </option>
              ))}
            </select>
          </div>

          <button type="submit" className="btn btn-primary" style={{ padding: '0.4rem 1rem', fontSize: '0.9rem' }}>
            Áp dụng bộ lọc
          </button>
        </form>
      </div>

      {data && (
        <>
          {/* Nhóm 1: Tài chính & Doanh thu theo Tháng (R5.5) và Toàn kỳ */}
          <div style={{ marginBottom: '2.5rem' }}>
            <h3 style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <DollarSign size={20} style={{ color: 'var(--success)' }} />
              <span>Doanh thu Phòng tập (Kỳ Tháng {data.selectedMonth || selectedMonth}/{data.selectedYear || selectedYear})</span>
              <span style={{ fontSize: '0.8rem', fontWeight: 500, color: 'var(--text-muted)' }}>
                (Chỉ tính các đơn hàng đã thanh toán thành công - PAID)
              </span>
            </h3>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(230px, 1fr))', gap: '1.25rem' }}>
              {/* Doanh thu tháng được chọn */}
              <div className="card" style={{ borderTop: '4px solid var(--success)', background: 'var(--bg-surface)' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>
                  DOANH THU THÁNG {data.selectedMonth || selectedMonth}/{data.selectedYear || selectedYear}
                </span>
                <h2 style={{ fontSize: '2rem', color: 'var(--success)', marginTop: '0.5rem' }}>
                  {formatCurrency(data.monthlyPaidRevenue)}
                </h2>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                  CK: {formatCurrency(data.monthlyBankTransferRevenue)} | TM: {formatCurrency(data.monthlyCashRevenue)}
                </p>
              </div>

              {/* Tổng doanh thu toàn kỳ */}
              <div className="card" style={{ borderTop: '4px solid var(--primary)', background: 'var(--bg-surface)' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>TỔNG DOANH THU TOÀN KỲ</span>
                <h2 style={{ fontSize: '2rem', color: 'var(--primary)', marginTop: '0.5rem' }}>
                  {formatCurrency(data.totalPaidRevenue)}
                </h2>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                  Toàn bộ giao dịch PAID từ trước tới nay
                </p>
              </div>

              <div className="card" style={{ borderTop: '4px solid #2563EB', background: 'var(--bg-surface)' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>CHUYỂN KHOẢN TOÀN KỲ</span>
                <h2 style={{ fontSize: '1.8rem', color: '#2563EB', marginTop: '0.5rem' }}>
                  {formatCurrency(data.bankTransferRevenue)}
                </h2>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                  Giao dịch thanh toán qua QR / STK
                </p>
              </div>

              <div className="card" style={{ borderTop: '4px solid var(--warning)', background: 'var(--bg-surface)' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>ĐƠN CHỜ THANH TOÁN</span>
                <h2 style={{ fontSize: '1.8rem', color: 'var(--warning)', marginTop: '0.5rem' }}>
                  {data.unpaidPaymentsCount}
                </h2>
                <div style={{ marginTop: '0.5rem' }}>
                  <button
                    className="btn btn-secondary"
                    style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem' }}
                    onClick={() => navigate('/admin/payments')}
                  >
                    Xem đơn chờ xử lý →
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Nhóm 2: Hội viên & Gói tập */}
          <div style={{ marginBottom: '2.5rem' }}>
            <h3 style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Users size={20} style={{ color: 'var(--primary)' }} />
              <span>Hội viên & Gói dịch vụ</span>
            </h3>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(230px, 1fr))', gap: '1.25rem' }}>
              <div className="card">
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>TỔNG SỐ HỘI VIÊN</span>
                <h2 style={{ fontSize: '2rem', marginTop: '0.25rem' }}>{data.totalMembers}</h2>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Tài khoản Member trong hệ thống</p>
              </div>

              <div className="card" style={{ borderLeft: '4px solid var(--success)' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>ĐANG HOẠT ĐỘNG</span>
                <h2 style={{ fontSize: '2rem', color: 'var(--success)', marginTop: '0.25rem' }}>{data.activeMembers}</h2>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Hội viên trạng thái ACTIVE</p>
              </div>

              <div className="card" style={{ borderLeft: '4px solid var(--danger)' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>BỊ KHÓA TÀI KHOẢN</span>
                <h2 style={{ fontSize: '2rem', color: 'var(--danger)', marginTop: '0.25rem' }}>{data.lockedMembers}</h2>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Hội viên trạng thái LOCKED</p>
              </div>

              <div className="card" style={{ borderLeft: '4px solid var(--primary)' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>CÓ GÓI ĐANG HOẠT ĐỘNG</span>
                <h2 style={{ fontSize: '2rem', color: 'var(--primary)', marginTop: '0.25rem' }}>{data.membersWithActivePackage}</h2>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Còn hạn & còn số lượt tập</p>
              </div>
            </div>
          </div>

          {/* Nhóm 3: Buổi tập & Vận hành lớp */}
          <div style={{ marginBottom: '2.5rem' }}>
            <h3 style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Activity size={20} style={{ color: 'var(--accent-gold)' }} />
              <span>Buổi tập & Tình hình Đặt chỗ</span>
            </h3>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '1.25rem' }}>
              <div className="card">
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>TỔNG SỐ BUỔI TẬP</span>
                <h2 style={{ fontSize: '1.8rem', marginTop: '0.25rem' }}>{data.totalSessions}</h2>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Tổng số buổi được tổ chức</p>
              </div>

              <div className="card" style={{ borderLeft: '4px solid var(--terracotta)' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>BUỔI ĐÃ LÊN LỊCH</span>
                <h2 style={{ fontSize: '1.8rem', color: 'var(--terracotta)', marginTop: '0.25rem' }}>{data.scheduledSessions}</h2>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Sắp diễn ra (SCHEDULED)</p>
              </div>

              <div className="card" style={{ borderLeft: '4px solid var(--success)' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>BUỔI ĐÃ HOÀN TẤT</span>
                <h2 style={{ fontSize: '1.8rem', color: 'var(--success)', marginTop: '0.25rem' }}>{data.completedSessions}</h2>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Đã dạy xong (COMPLETED)</p>
              </div>

              <div className="card" style={{ borderLeft: '4px solid var(--danger)' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>BUỔI ĐÃ HỦY</span>
                <h2 style={{ fontSize: '1.8rem', color: 'var(--danger)', marginTop: '0.25rem' }}>{data.canceledSessions}</h2>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Đã hủy (đã hoàn lượt cho Member)</p>
              </div>

              <div className="card" style={{ borderLeft: '4px solid var(--primary)' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>LƯỢT ĐẶT CHỖ HIỆU LỰC</span>
                <h2 style={{ fontSize: '1.8rem', color: 'var(--primary)', marginTop: '0.25rem' }}>{data.totalConfirmedBookings}</h2>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Học viên đang giữ chỗ (CONFIRMED)</p>
              </div>
            </div>
          </div>

          {/* Shortcuts quản trị */}
          <div className="card" style={{ background: 'var(--bg-surface-soft)' }}>
            <h4 style={{ marginBottom: '1rem' }}>Thao tác Quản trị Nhanh</h4>
            <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
              <button className="btn btn-secondary" onClick={() => navigate('/admin/sessions')} style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                <Calendar size={15} />
                <span>Quản lý Buổi tập</span>
              </button>
              <button className="btn btn-secondary" onClick={() => navigate('/admin/payments')} style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                <CreditCard size={15} />
                <span>Quản lý & Duyệt Thanh toán</span>
              </button>
              <button className="btn btn-secondary" onClick={() => navigate('/admin/packages')} style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                <Package size={15} />
                <span>Quản lý Gói tập</span>
              </button>
              <button className="btn btn-secondary" onClick={() => navigate('/admin/users')} style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                <Users size={15} />
                <span>Quản lý Người dùng & HLV</span>
              </button>
              <button className="btn btn-secondary" onClick={() => navigate('/admin/classes')} style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                <Layers size={15} />
                <span>Quản lý Lớp Pilates</span>
              </button>
              <button className="btn btn-secondary" onClick={() => navigate('/admin/rooms')} style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                <DoorClosed size={15} />
                <span>Quản lý Phòng tập</span>
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

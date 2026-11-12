import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../../lib/apiClient';
import { useAuth } from '../../context/AuthContext';
import { MembershipPackage, Payment, PaymentMethod } from '../../types/membership';
import {
  Package,
  CheckCircle2,
  AlertCircle,
  Calendar,
  Clock,
  Sparkles,
  CreditCard,
  Building,
  ArrowRight
} from 'lucide-react';

export const BuyPackagePage: React.FC = () => {
  const { user, role } = useAuth();
  const navigate = useNavigate();

  const [packages, setPackages] = useState<MembershipPackage[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMsg, setErrorMsg] = useState<string>('');
  const [successMsg, setSuccessMsg] = useState<string>('');

  // Modal mua gói
  const [selectedPackage, setSelectedPackage] = useState<MembershipPackage | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('BANK_TRANSFER');
  const [note, setNote] = useState<string>('');
  const [submitting, setSubmitting] = useState<boolean>(false);

  // Kết quả sau khi tạo đơn thành công
  const [createdPayment, setCreatedPayment] = useState<Payment | null>(null);

  useEffect(() => {
    const fetchActivePackages = async () => {
      setLoading(true);
      setErrorMsg('');
      try {
        const res = await apiClient.get('/api/v1/packages');
        if (res.data.success) {
          setPackages(res.data.data);
        }
      } catch (err: any) {
        setErrorMsg('Không thể tải danh sách gói tập');
      } finally {
        setLoading(false);
      }
    };
    fetchActivePackages();
  }, []);

  const handleCreateOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) {
      navigate('/login');
      return;
    }
    if (!selectedPackage) return;

    setSubmitting(true);
    setErrorMsg('');
    try {
      const res = await apiClient.post('/api/v1/payments', {
        packageId: selectedPackage.id,
        paymentMethod,
        note
      });

      if (res.data.success) {
        setCreatedPayment(res.data.data);
        setSelectedPackage(null);
      }
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Không thể tạo đơn hàng mua gói tập');
    } finally {
      setSubmitting(false);
    }
  };

  const formatPrice = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  return (
    <main className="main-content">
      <div style={{ textAlign: 'center', marginBottom: '2.5rem' }}>
        <h1 style={{ fontSize: '2.25rem', fontWeight: 800, color: 'var(--text-color)', marginBottom: '0.5rem' }}>
          Gói tập Pilates Chuyên Nghiệp
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1.05rem', maxWidth: '600px', margin: '0 auto' }}>
          Đầu tư cho sức khỏe và vóc dáng với các gói tập linh hoạt, trang thiết bị Reformer hiện đại và huấn luyện viên tận tâm
        </p>
      </div>

      {errorMsg && (
        <div className="alert alert-danger" style={{ marginBottom: '1.5rem', maxWidth: '750px', margin: '0 auto 1.5rem auto' }}>
          <AlertCircle size={18} />
          <span>{errorMsg}</span>
        </div>
      )}

      {/* Thông báo thanh toán thành công (Mở đơn UNPAID và hướng dẫn chuyển khoản) */}
      {createdPayment && (
        <div className="card" style={{ maxWidth: '650px', margin: '0 auto 2.5rem auto', border: '2px solid var(--primary-color)' }}>
          <div style={{ textAlign: 'center', padding: '1rem 0' }}>
            <div style={{ display: 'inline-flex', padding: '0.75rem', background: 'rgba(79, 70, 229, 0.1)', color: 'var(--primary-color)', borderRadius: '50%', marginBottom: '1rem' }}>
              <CheckCircle2 size={36} />
            </div>
            <h2 style={{ margin: '0 0 0.5rem 0', fontSize: '1.5rem', fontWeight: 700 }}>
              Đã tạo đơn mua gói tập thành công!
            </h2>
            <p style={{ color: 'var(--text-muted)', margin: 0 }}>
              Mã giao dịch của bạn: <strong style={{ color: 'var(--primary-color)', fontSize: '1.1rem' }}>{createdPayment.transactionCode}</strong>
            </p>
          </div>

          <div style={{ background: 'var(--bg-muted, #f8fafc)', padding: '1.25rem', borderRadius: 'var(--radius)', margin: '1rem 0', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Gói đăng ký:</span>
              <span style={{ fontWeight: 600 }}>{createdPayment.packageName}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Tổng thanh toán:</span>
              <span style={{ fontWeight: 700, fontSize: '1.2rem', color: 'var(--success-color)' }}>
                {formatPrice(createdPayment.amount)}
              </span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Hình thức:</span>
              <span style={{ fontWeight: 600 }}>
                {createdPayment.paymentMethod === 'BANK_TRANSFER' ? 'Chuyển khoản ngân hàng' : 'Tiền mặt tại quầy'}
              </span>
            </div>

            {createdPayment.paymentMethod === 'BANK_TRANSFER' && (
              <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '0.75rem', marginTop: '0.25rem' }}>
                <div style={{ fontWeight: 600, color: 'var(--text-color)', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: 6 }}>
                  <Building size={16} />
                  <span>Thông tin chuyển khoản:</span>
                </div>
                <div style={{ fontSize: '0.9rem', color: 'var(--text-color)' }}>
                  <div>Ngân hàng: <strong>Vietcombank (VCB)</strong></div>
                  <div>Số tài khoản: <strong>0123456789999</strong></div>
                  <div>Chủ tài khoản: <strong>PHONG TAP PILATES N06-12</strong></div>
                  <div>Nội dung CK: <strong style={{ color: 'var(--primary-color)' }}>{createdPayment.transactionCode}</strong></div>
                </div>
              </div>
            )}
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.25rem' }}>
            <button onClick={() => setCreatedPayment(null)} className="btn btn-secondary">
              Xem gói khác
            </button>
            <button onClick={() => navigate('/my-membership')} className="btn btn-primary">
              Xem gói tập của tôi
            </button>
          </div>
        </div>
      )}

      {/* Lưới các gói tập */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
          Đang nạp bảng giá gói tập...
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem', maxWidth: '1200px', margin: '0 auto' }}>
          {packages.map((pkg, idx) => {
            const isHighlight = idx === 1 || pkg.totalSessions === 20;

            return (
              <div
                key={pkg.id}
                className="card"
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  border: isHighlight ? '2px solid var(--primary-color)' : '1px solid var(--border-color)',
                  position: 'relative',
                  padding: '1.75rem',
                  transform: isHighlight ? 'scale(1.02)' : 'none',
                  boxShadow: isHighlight ? '0 8px 24px rgba(79, 70, 229, 0.12)' : 'var(--shadow)'
                }}
              >
                {isHighlight && (
                  <div style={{ position: 'absolute', top: '-12px', left: '50%', transform: 'translateX(-50%)', background: 'var(--primary-color)', color: '#fff', fontSize: '0.75rem', fontWeight: 700, padding: '0.2rem 0.75rem', borderRadius: '12px', display: 'flex', alignItems: 'center', gap: 4 }}>
                    <Sparkles size={12} />
                    <span>PHỔ BIẾN NHẤT</span>
                  </div>
                )}

                <div>
                  <h3 style={{ margin: '0 0 0.5rem 0', fontSize: '1.35rem', fontWeight: 700, color: 'var(--text-color)' }}>
                    {pkg.name}
                  </h3>
                  <div style={{ fontSize: '1.85rem', fontWeight: 800, color: 'var(--primary-color)', marginBottom: '1rem' }}>
                    {formatPrice(pkg.price)}
                  </div>

                  <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', minHeight: '40px', marginBottom: '1.5rem' }}>
                    {pkg.description || 'Gói tập Pilates chuẩn quốc tế với đầy đủ dụng cụ và trang thiết bị.'}
                  </p>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', borderTop: '1px solid var(--border-color)', paddingTop: '1.25rem', marginBottom: '1.5rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.95rem' }}>
                      <Calendar size={16} color="var(--primary-color)" />
                      <span><strong>{pkg.totalSessions} buổi tập</strong> Pilates</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.95rem' }}>
                      <Clock size={16} color="var(--primary-color)" />
                      <span>Thời hạn sử dụng: <strong>{pkg.durationDays} ngày</strong></span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.95rem' }}>
                      <CreditCard size={16} color="var(--primary-color)" />
                      <span>Hỗ trợ chuyển khoản & tiền mặt</span>
                    </div>
                  </div>
                </div>

                <button
                  onClick={() => {
                    setSelectedPackage(pkg);
                    setErrorMsg('');
                  }}
                  className={`btn ${isHighlight ? 'btn-primary' : 'btn-secondary'}`}
                  style={{ width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 6, fontWeight: 600 }}
                >
                  <span>Đăng ký gói này</span>
                  <ArrowRight size={16} />
                </button>
              </div>
            );
          })}
        </div>
      )}

      {/* Modal xác nhận đặt mua gói */}
      {selectedPackage && (
        <div className="modal-backdrop" onClick={() => setSelectedPackage(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '480px' }}>
            <div className="modal-header">
              <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700 }}>
                Đăng ký mua gói tập
              </h3>
              <button onClick={() => setSelectedPackage(null)} className="btn btn-secondary btn-sm">
                &times;
              </button>
            </div>

            <form onSubmit={handleCreateOrder}>
              <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div style={{ background: 'var(--bg-muted, #f8fafc)', padding: '1rem', borderRadius: 'var(--radius)' }}>
                  <div style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--primary-color)' }}>
                    {selectedPackage.name}
                  </div>
                  <div style={{ fontSize: '1.3rem', fontWeight: 800, margin: '0.25rem 0' }}>
                    {formatPrice(selectedPackage.price)}
                  </div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    {selectedPackage.totalSessions} buổi tập • Hạn dùng {selectedPackage.durationDays} ngày
                  </div>
                </div>

                <div>
                  <label className="form-label">Phương thức thanh toán *</label>
                  <select
                    className="form-control"
                    value={paymentMethod}
                    onChange={(e) => setPaymentMethod(e.target.value as PaymentMethod)}
                  >
                    <option value="BANK_TRANSFER">Chuyển khoản ngân hàng (QR / STK)</option>
                    <option value="CASH">Tiền mặt tại quầy lễ tân</option>
                  </select>
                </div>

                <div>
                  <label className="form-label">Ghi chú (Tùy chọn)</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Ví dụ: Đăng ký tập buổi sáng..."
                    value={note}
                    onChange={(e) => setNote(e.target.value)}
                  />
                </div>
              </div>

              <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
                <button
                  type="button"
                  onClick={() => setSelectedPackage(null)}
                  className="btn btn-secondary"
                  disabled={submitting}
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting}
                >
                  {submitting ? 'Đang tạo đơn...' : 'Tạo đơn mua gói'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </main>
  );
};

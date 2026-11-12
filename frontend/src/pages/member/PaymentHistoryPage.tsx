import React, { useEffect, useState } from 'react';
import { apiClient } from '../../lib/apiClient';
import { Payment } from '../../types/membership';
import {
  CreditCard,
  CheckCircle2,
  Clock,
  XCircle,
  Building,
  Info
} from 'lucide-react';

export const PaymentHistoryPage: React.FC = () => {
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMsg, setErrorMsg] = useState<string>('');
  const [selectedPayment, setSelectedPayment] = useState<Payment | null>(null);

  const fetchMyPayments = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const res = await apiClient.get('/api/v1/payments/my-history?size=50');
      if (res.data.success) {
        setPayments(res.data.data.items);
      }
    } catch (err: any) {
      setErrorMsg('Không thể tải lịch sử thanh toán');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyPayments();
  }, []);

  const formatPrice = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  return (
    <main className="main-content">
      <div style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ margin: 0, fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-color)' }}>
          Lịch sử giao dịch & Thanh toán
        </h1>
        <p style={{ margin: '0.25rem 0 0 0', color: 'var(--text-muted)' }}>
          Theo dõi các hóa đơn mua gói tập Pilates và trạng thái thanh toán của bạn
        </p>
      </div>

      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
            Đang tải dữ liệu giao dịch...
          </div>
        ) : payments.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
            Bạn chưa có giao dịch thanh toán nào.
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>Mã đơn (TXN)</th>
                  <th>Gói tập</th>
                  <th>Số tiền</th>
                  <th>Hình thức</th>
                  <th>Trạng thái</th>
                  <th>Ngày tạo</th>
                  <th style={{ textAlign: 'right' }}>Chi tiết</th>
                </tr>
              </thead>
              <tbody>
                {payments.map((p) => {
                  let badge = <span className="badge badge-warning">Chờ thanh toán</span>;
                  if (p.status === 'PAID') badge = <span className="badge badge-success">Đã thanh toán</span>;
                  if (p.status === 'CANCELED') badge = <span className="badge badge-danger">Đã hủy</span>;

                  return (
                    <tr key={p.id}>
                      <td style={{ fontWeight: 700, color: 'var(--primary-color)' }}>
                        {p.transactionCode}
                      </td>
                      <td style={{ fontWeight: 600 }}>{p.packageName}</td>
                      <td style={{ fontWeight: 700 }}>{formatPrice(p.amount)}</td>
                      <td>
                        <span className="badge badge-secondary">
                          {p.paymentMethod === 'BANK_TRANSFER' ? 'Chuyển khoản' : 'Tiền mặt'}
                        </span>
                      </td>
                      <td>{badge}</td>
                      <td>
                        {new Date(p.createdAt).toLocaleDateString('vi-VN')} {new Date(p.createdAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <button
                          onClick={() => setSelectedPayment(p)}
                          className="btn btn-secondary btn-sm"
                        >
                          Xem chi tiết
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal chi tiết hóa đơn */}
      {selectedPayment && (
        <div className="modal-backdrop" onClick={() => setSelectedPayment(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '480px' }}>
            <div className="modal-header">
              <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700 }}>
                Hóa đơn #{selectedPayment.transactionCode}
              </h3>
              <button onClick={() => setSelectedPayment(null)} className="btn btn-secondary btn-sm">
                &times;
              </button>
            </div>

            <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div style={{ background: 'var(--bg-muted, #f8fafc)', padding: '1rem', borderRadius: 'var(--radius)', display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Gói tập:</span>
                  <span style={{ fontWeight: 600 }}>{selectedPayment.packageName}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Số tiền:</span>
                  <span style={{ fontWeight: 700, fontSize: '1.15rem', color: 'var(--success-color)' }}>
                    {formatPrice(selectedPayment.amount)}
                  </span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Trạng thái:</span>
                  <span>
                    {selectedPayment.status === 'PAID' && <span className="badge badge-success">ĐÃ THANH TOÁN</span>}
                    {selectedPayment.status === 'UNPAID' && <span className="badge badge-warning">CHỜ THANH TOÁN</span>}
                    {selectedPayment.status === 'CANCELED' && <span className="badge badge-danger">ĐÃ HỦY</span>}
                  </span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Hình thức:</span>
                  <span>{selectedPayment.paymentMethod === 'BANK_TRANSFER' ? 'Chuyển khoản ngân hàng' : 'Tiền mặt'}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Ngày tạo:</span>
                  <span>{new Date(selectedPayment.createdAt).toLocaleString('vi-VN')}</span>
                </div>
                {selectedPayment.paidAt && (
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-muted)' }}>Ngày duyệt:</span>
                    <span style={{ color: 'var(--success-color)', fontWeight: 600 }}>
                      {new Date(selectedPayment.paidAt).toLocaleString('vi-VN')}
                    </span>
                  </div>
                )}
              </div>

              {selectedPayment.status === 'UNPAID' && selectedPayment.paymentMethod === 'BANK_TRANSFER' && (
                <div style={{ border: '1px solid var(--border-color)', padding: '0.9rem', borderRadius: 'var(--radius)' }}>
                  <div style={{ fontWeight: 600, color: 'var(--primary-color)', marginBottom: '0.35rem', display: 'flex', alignItems: 'center', gap: 6 }}>
                    <Building size={16} />
                    <span>Hướng dẫn thanh toán chuyển khoản:</span>
                  </div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-color)' }}>
                    <div>Ngân hàng: <strong>Vietcombank (VCB)</strong></div>
                    <div>Số tài khoản: <strong>0123456789999</strong></div>
                    <div>Chủ tài khoản: <strong>PHONG TAP PILATES N06-12</strong></div>
                    <div>Nội dung CK: <strong style={{ color: 'var(--primary-color)' }}>{selectedPayment.transactionCode}</strong></div>
                  </div>
                </div>
              )}

              {selectedPayment.note && (
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  <strong>Ghi chú:</strong> {selectedPayment.note}
                </div>
              )}
            </div>

            <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <button onClick={() => setSelectedPayment(null)} className="btn btn-secondary">
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
};

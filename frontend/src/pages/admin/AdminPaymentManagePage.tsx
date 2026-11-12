import React, { useEffect, useState } from 'react';
import { apiClient } from '../../lib/apiClient';
import { Payment, PaymentStatus, PaymentMethod } from '../../types/membership';
import {
  CreditCard,
  CheckCircle2,
  XCircle,
  AlertCircle,
  DollarSign,
  Filter,
  Calendar,
  User,
  Clock,
  Check
} from 'lucide-react';

export const AdminPaymentManagePage: React.FC = () => {
  const [payments, setPayments] = useState<Payment[]>([]);
  const [totalRevenue, setTotalRevenue] = useState<number>(0);
  const [selectedStatus, setSelectedStatus] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMsg, setErrorMsg] = useState<string>('');
  const [successMsg, setSuccessMsg] = useState<string>('');

  // Modal Xác nhận thanh toán (PAID)
  const [confirmModalPayment, setConfirmModalPayment] = useState<Payment | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('BANK_TRANSFER');
  const [confirmNote, setConfirmNote] = useState<string>('Đã nhận đủ tiền từ hội viên');
  const [submitting, setSubmitting] = useState<boolean>(false);

  // Modal Hủy giao dịch (CANCELED)
  const [cancelModalPayment, setCancelModalPayment] = useState<Payment | null>(null);
  const [cancelReason, setCancelReason] = useState<string>('Hội viên yêu cầu hủy đơn');

  const fetchPayments = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const params = new URLSearchParams();
      params.append('size', '50');
      if (selectedStatus) params.append('status', selectedStatus);

      const [paymentsRes, revenueRes] = await Promise.all([
        apiClient.get(`/api/v1/admin/payments?${params.toString()}`),
        apiClient.get('/api/v1/admin/payments/revenue').catch(() => ({ data: { data: { totalRevenue: 0 } } }))
      ]);

      if (paymentsRes.data.success) {
        setPayments(paymentsRes.data.data.items);
      }
      if (revenueRes.data.success) {
        setTotalRevenue(revenueRes.data.data.totalRevenue || 0);
      }
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Không thể tải danh sách thanh toán');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPayments();
  }, [selectedStatus]);

  const handleConfirmPayment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!confirmModalPayment) return;

    setSubmitting(true);
    setErrorMsg('');
    try {
      const res = await apiClient.post(`/api/v1/admin/payments/${confirmModalPayment.id}/confirm`, {
        paymentMethod,
        note: confirmNote
      });
      if (res.data.success) {
        setSuccessMsg(`Đã xác nhận thanh toán mã ${confirmModalPayment.transactionCode} & kích hoạt gói thành công!`);
        setConfirmModalPayment(null);
        fetchPayments();
        setTimeout(() => setSuccessMsg(''), 4000);
      }
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Lỗi xác nhận thanh toán');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancelPayment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!cancelModalPayment) return;

    setSubmitting(true);
    setErrorMsg('');
    try {
      const res = await apiClient.post(`/api/v1/admin/payments/${cancelModalPayment.id}/cancel?reason=${encodeURIComponent(cancelReason)}`);
      if (res.data.success) {
        setSuccessMsg(`Đã hủy giao dịch ${cancelModalPayment.transactionCode}`);
        setCancelModalPayment(null);
        fetchPayments();
        setTimeout(() => setSuccessMsg(''), 4000);
      }
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Không thể hủy giao dịch');
    } finally {
      setSubmitting(false);
    }
  };

  const formatPrice = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  return (
    <main className="main-content">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ margin: 0, fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-color)' }}>
            Quản lý Giao dịch & Doanh thu
          </h1>
          <p style={{ margin: '0.25rem 0 0 0', color: 'var(--text-muted)' }}>
            Xác nhận thanh toán để kích hoạt gói tập hội viên và theo dõi doanh thu thực tế
          </p>
        </div>

        {/* Thẻ doanh thu tổng */}
        <div style={{ background: 'var(--surface-color)', padding: '0.75rem 1.25rem', borderRadius: 'var(--radius)', border: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ background: 'rgba(16, 185, 129, 0.1)', color: 'var(--success-color)', padding: '0.5rem', borderRadius: '50%' }}>
            <DollarSign size={24} />
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>TỔNG DOANH THU ĐÃ THU (PAID)</div>
            <div style={{ fontSize: '1.4rem', fontWeight: 700, color: 'var(--success-color)' }}>
              {formatPrice(totalRevenue)}
            </div>
          </div>
        </div>
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
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600 }}>
            <Filter size={16} color="var(--primary-color)" />
            <span>Lọc theo trạng thái:</span>
          </div>

          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button
              onClick={() => setSelectedStatus('')}
              className={`btn btn-sm ${selectedStatus === '' ? 'btn-primary' : 'btn-secondary'}`}
            >
              Tất cả
            </button>
            <button
              onClick={() => setSelectedStatus('UNPAID')}
              className={`btn btn-sm ${selectedStatus === 'UNPAID' ? 'btn-primary' : 'btn-secondary'}`}
            >
              Chờ thanh toán (UNPAID)
            </button>
            <button
              onClick={() => setSelectedStatus('PAID')}
              className={`btn btn-sm ${selectedStatus === 'PAID' ? 'btn-primary' : 'btn-secondary'}`}
            >
              Đã thanh toán (PAID)
            </button>
            <button
              onClick={() => setSelectedStatus('CANCELED')}
              className={`btn btn-sm ${selectedStatus === 'CANCELED' ? 'btn-primary' : 'btn-secondary'}`}
            >
              Đã hủy (CANCELED)
            </button>
          </div>
        </div>
      </div>

      {/* Bảng danh sách thanh toán */}
      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
            Đang tải dữ liệu giao dịch...
          </div>
        ) : payments.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
            Không có giao dịch nào phù hợp.
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>Mã đơn (TXN)</th>
                  <th>Hội viên</th>
                  <th>Gói tập đăng ký</th>
                  <th>Số tiền</th>
                  <th>Phương thức</th>
                  <th>Trạng thái</th>
                  <th>Ngày tạo / Duyệt</th>
                  <th style={{ textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {payments.map((p) => {
                  let statusBadge = <span className="badge badge-warning">Chờ thanh toán</span>;
                  if (p.status === 'PAID') statusBadge = <span className="badge badge-success">Đã thanh toán</span>;
                  if (p.status === 'CANCELED') statusBadge = <span className="badge badge-danger">Đã hủy</span>;

                  return (
                    <tr key={p.id}>
                      <td style={{ fontWeight: 700, color: 'var(--primary-color)' }}>
                        {p.transactionCode}
                      </td>
                      <td>
                        <div style={{ fontWeight: 600 }}>{p.memberName || 'Hội viên'}</div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{p.memberEmail}</div>
                      </td>
                      <td>
                        <div style={{ fontWeight: 600 }}>{p.packageName}</div>
                        {p.membershipId && (
                          <div style={{ fontSize: '0.75rem', color: 'var(--success-color)' }}>
                            Đã kích hoạt thẻ #{p.membershipId}
                          </div>
                        )}
                      </td>
                      <td style={{ fontWeight: 700, color: 'var(--text-color)' }}>
                        {formatPrice(p.amount)}
                      </td>
                      <td>
                        <span className="badge badge-secondary">
                          {p.paymentMethod === 'BANK_TRANSFER' ? 'Chuyển khoản' : 'Tiền mặt'}
                        </span>
                      </td>
                      <td>{statusBadge}</td>
                      <td>
                        <div style={{ fontSize: '0.85rem' }}>
                          {new Date(p.createdAt).toLocaleDateString('vi-VN')} {new Date(p.createdAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}
                        </div>
                        {p.paidAt && (
                          <div style={{ fontSize: '0.75rem', color: 'var(--success-color)' }}>
                            Duyệt: {new Date(p.paidAt).toLocaleDateString('vi-VN')}
                          </div>
                        )}
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        {p.status === 'UNPAID' && (
                          <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                            <button
                              onClick={() => {
                                setConfirmModalPayment(p);
                                setPaymentMethod(p.paymentMethod);
                                setConfirmNote('Đã nhận đủ tiền từ hội viên');
                              }}
                              className="btn btn-primary btn-sm"
                              title="Xác nhận đã thanh toán (PAID) và kích hoạt gói"
                              style={{ display: 'flex', alignItems: 'center', gap: 4 }}
                            >
                              <Check size={14} />
                              <span>Duyệt</span>
                            </button>
                            <button
                              onClick={() => {
                                setCancelModalPayment(p);
                                setCancelReason('Hội viên yêu cầu hủy đơn');
                              }}
                              className="btn btn-secondary btn-sm"
                              title="Hủy đơn giao dịch"
                            >
                              <XCircle size={14} />
                            </button>
                          </div>
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

      {/* Modal Xác nhận đã nhận tiền (PAID) */}
      {confirmModalPayment && (
        <div className="modal-backdrop" onClick={() => setConfirmModalPayment(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '480px' }}>
            <div className="modal-header">
              <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700 }}>
                Xác nhận thanh toán & Kích hoạt gói
              </h3>
              <button onClick={() => setConfirmModalPayment(null)} className="btn btn-secondary btn-sm">
                &times;
              </button>
            </div>

            <form onSubmit={handleConfirmPayment}>
              <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div style={{ background: 'var(--bg-muted, #f8fafc)', padding: '0.75rem', borderRadius: 'var(--radius)' }}>
                  <div><strong>Mã giao dịch:</strong> {confirmModalPayment.transactionCode}</div>
                  <div><strong>Hội viên:</strong> {confirmModalPayment.memberName} ({confirmModalPayment.memberEmail})</div>
                  <div><strong>Gói tập:</strong> {confirmModalPayment.packageName}</div>
                  <div><strong>Số tiền thực thu:</strong> <span style={{ color: 'var(--success-color)', fontWeight: 700 }}>{formatPrice(confirmModalPayment.amount)}</span></div>
                </div>

                <div>
                  <label className="form-label">Hình thức thực tế nhận tiền</label>
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
                  <label className="form-label">Ghi chú xác nhận</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Ví dụ: Đã nhận đủ tiền từ ngân hàng Vietcombank"
                    value={confirmNote}
                    onChange={(e) => setConfirmNote(e.target.value)}
                  />
                </div>
              </div>

              <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
                <button
                  type="button"
                  onClick={() => setConfirmModalPayment(null)}
                  className="btn btn-secondary"
                  disabled={submitting}
                >
                  Đóng
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting}
                >
                  {submitting ? 'Đang xử lý...' : 'Xác nhận ĐÃ THANH TOÁN'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Hủy giao dịch (CANCELED) */}
      {cancelModalPayment && (
        <div className="modal-backdrop" onClick={() => setCancelModalPayment(null)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '440px' }}>
            <div className="modal-header">
              <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700, color: 'var(--danger-color)' }}>
                Hủy giao dịch #{cancelModalPayment.transactionCode}
              </h3>
              <button onClick={() => setCancelModalPayment(null)} className="btn btn-secondary btn-sm">
                &times;
              </button>
            </div>

            <form onSubmit={handleCancelPayment}>
              <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <p style={{ margin: 0, color: 'var(--text-muted)' }}>
                  Bạn có chắc chắn muốn hủy đơn hàng này không? Giao dịch bị hủy sẽ không thể hoàn tác.
                </p>

                <div>
                  <label className="form-label">Lý do hủy đơn *</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Nhập lý do hủy giao dịch..."
                    value={cancelReason}
                    onChange={(e) => setCancelReason(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
                <button
                  type="button"
                  onClick={() => setCancelModalPayment(null)}
                  className="btn btn-secondary"
                  disabled={submitting}
                >
                  Không
                </button>
                <button
                  type="submit"
                  className="btn btn-danger"
                  disabled={submitting}
                >
                  {submitting ? 'Đang hủy...' : 'Xác nhận HỦY ĐƠN'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </main>
  );
};

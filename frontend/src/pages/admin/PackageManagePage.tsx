import React, { useEffect, useState } from 'react';
import { apiClient } from '../../lib/apiClient';
import { MembershipPackage, PackageStatus } from '../../types/membership';
import {
  Package,
  Plus,
  Edit2,
  ToggleLeft,
  ToggleRight,
  AlertCircle,
  CheckCircle2,
  Calendar,
  CreditCard,
  Clock
} from 'lucide-react';

export const PackageManagePage: React.FC = () => {
  const [packages, setPackages] = useState<MembershipPackage[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [errorMsg, setErrorMsg] = useState<string>('');
  const [successMsg, setSuccessMsg] = useState<string>('');

  // Modal Thêm / Sửa
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingPackage, setEditingPackage] = useState<MembershipPackage | null>(null);

  // Form State
  const [name, setName] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  const [price, setPrice] = useState<number>(1500000);
  const [totalSessions, setTotalSessions] = useState<number>(10);
  const [durationDays, setDurationDays] = useState<number>(30);
  const [status, setStatus] = useState<PackageStatus>('ACTIVE');
  const [submitting, setSubmitting] = useState<boolean>(false);

  const fetchPackages = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const res = await apiClient.get('/api/v1/admin/packages?size=50');
      if (res.data.success) {
        setPackages(res.data.data.items);
      }
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Không thể tải danh sách gói tập');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPackages();
  }, []);

  const openCreateModal = () => {
    setEditingPackage(null);
    setName('');
    setDescription('');
    setPrice(1500000);
    setTotalSessions(10);
    setDurationDays(30);
    setStatus('ACTIVE');
    setErrorMsg('');
    setIsModalOpen(true);
  };

  const openEditModal = (pkg: MembershipPackage) => {
    setEditingPackage(pkg);
    setName(pkg.name);
    setDescription(pkg.description || '');
    setPrice(pkg.price);
    setTotalSessions(pkg.totalSessions);
    setDurationDays(pkg.durationDays);
    setStatus(pkg.status);
    setErrorMsg('');
    setIsModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setErrorMsg('Tên gói tập không được để trống');
      return;
    }
    if (price < 0) {
      setErrorMsg('Giá gói tập không thể âm');
      return;
    }
    if (totalSessions <= 0) {
      setErrorMsg('Tổng số buổi phải lớn hơn 0');
      return;
    }
    if (durationDays <= 0) {
      setErrorMsg('Thời hạn sử dụng phải lớn hơn 0');
      return;
    }

    setSubmitting(true);
    setErrorMsg('');
    try {
      const payload = {
        name: name.trim(),
        description: description.trim(),
        price,
        totalSessions,
        durationDays,
        status
      };

      if (editingPackage) {
        await apiClient.put(`/api/v1/admin/packages/${editingPackage.id}`, payload);
        setSuccessMsg(`Cập nhật gói '${name}' thành công!`);
      } else {
        await apiClient.post('/api/v1/admin/packages', payload);
        setSuccessMsg(`Tạo mới gói '${name}' thành công!`);
      }

      setIsModalOpen(false);
      fetchPackages();
      setTimeout(() => setSuccessMsg(''), 4000);
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Lỗi lưu thông tin gói tập');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = async (pkg: MembershipPackage) => {
    try {
      await apiClient.patch(`/api/v1/admin/packages/${pkg.id}/toggle-status`);
      setSuccessMsg(`Đã đổi trạng thái gói '${pkg.name}'`);
      fetchPackages();
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Không thể đổi trạng thái');
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
            Quản lý Gói tập Pilates
          </h1>
          <p style={{ margin: '0.25rem 0 0 0', color: 'var(--text-muted)' }}>
            Cấu hình các gói tập, số buổi, thời hạn sử dụng và mức giá cho hội viên
          </p>
        </div>

        <button onClick={openCreateModal} className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <Plus size={18} />
          <span>Thêm gói tập mới</span>
        </button>
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

      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
            Đang tải danh sách gói tập...
          </div>
        ) : packages.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
            Chưa có gói tập nào trong hệ thống.
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Tên gói tập</th>
                  <th>Đơn giá</th>
                  <th>Số buổi</th>
                  <th>Hạn dùng</th>
                  <th>Trạng thái</th>
                  <th style={{ textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {packages.map((pkg) => (
                  <tr key={pkg.id}>
                    <td style={{ fontWeight: 600 }}>#{pkg.id}</td>
                    <td>
                      <div style={{ fontWeight: 600, color: 'var(--primary-color)' }}>{pkg.name}</div>
                      {pkg.description && (
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', maxWidth: '300px' }}>
                          {pkg.description}
                        </div>
                      )}
                    </td>
                    <td style={{ fontWeight: 700, color: 'var(--text-color)' }}>
                      {formatPrice(pkg.price)}
                    </td>
                    <td>
                      <span className="badge badge-secondary" style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                        <Calendar size={12} />
                        {pkg.totalSessions} buổi
                      </span>
                    </td>
                    <td>
                      <span className="badge badge-secondary" style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                        <Clock size={12} />
                        {pkg.durationDays} ngày
                      </span>
                    </td>
                    <td>
                      {pkg.status === 'ACTIVE' ? (
                        <span className="badge badge-success">Đang mở bán</span>
                      ) : (
                        <span className="badge badge-danger">Tạm ngừng</span>
                      )}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                        <button
                          onClick={() => handleToggleStatus(pkg)}
                          className="btn btn-secondary btn-sm"
                          title={pkg.status === 'ACTIVE' ? 'Tạm ngừng mở bán' : 'Kích hoạt mở bán'}
                        >
                          {pkg.status === 'ACTIVE' ? <ToggleRight size={16} color="var(--success-color)" /> : <ToggleLeft size={16} />}
                        </button>
                        <button
                          onClick={() => openEditModal(pkg)}
                          className="btn btn-secondary btn-sm"
                          title="Chỉnh sửa"
                        >
                          <Edit2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal Thêm / Chỉnh sửa gói tập */}
      {isModalOpen && (
        <div className="modal-backdrop" onClick={() => setIsModalOpen(false)}>
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '520px' }}>
            <div className="modal-header">
              <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700 }}>
                {editingPackage ? 'Cập nhật gói tập' : 'Tạo mới gói tập'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="btn btn-secondary btn-sm">
                &times;
              </button>
            </div>

            <form onSubmit={handleSave}>
              <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div>
                  <label className="form-label">Tên gói tập *</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Ví dụ: Gói Tiêu Chuẩn 20 buổi"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                  />
                </div>

                <div>
                  <label className="form-label">Mô tả gói tập</label>
                  <textarea
                    className="form-control"
                    rows={2}
                    placeholder="Mô tả nội dung, quyền lợi gói tập..."
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                  />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div>
                    <label className="form-label">Giá niêm yết (VNĐ) *</label>
                    <input
                      type="number"
                      className="form-control"
                      value={price}
                      onChange={(e) => setPrice(Number(e.target.value))}
                      min={0}
                      step={50000}
                      required
                    />
                  </div>

                  <div>
                    <label className="form-label">Tổng số buổi tập *</label>
                    <input
                      type="number"
                      className="form-control"
                      value={totalSessions}
                      onChange={(e) => setTotalSessions(Number(e.target.value))}
                      min={1}
                      required
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div>
                    <label className="form-label">Thời hạn (Ngày) *</label>
                    <input
                      type="number"
                      className="form-control"
                      value={durationDays}
                      onChange={(e) => setDurationDays(Number(e.target.value))}
                      min={1}
                      required
                    />
                  </div>

                  <div>
                    <label className="form-label">Trạng thái</label>
                    <select
                      className="form-control"
                      value={status}
                      onChange={(e) => setStatus(e.target.value as PackageStatus)}
                    >
                      <option value="ACTIVE">Hoạt động (ACTIVE)</option>
                      <option value="INACTIVE">Tạm ngưng (INACTIVE)</option>
                    </select>
                  </div>
                </div>
              </div>

              <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="btn btn-secondary"
                  disabled={submitting}
                >
                  Hủy bỏ
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting}
                >
                  {submitting ? 'Đang lưu...' : editingPackage ? 'Lưu thay đổi' : 'Tạo gói tập'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </main>
  );
};

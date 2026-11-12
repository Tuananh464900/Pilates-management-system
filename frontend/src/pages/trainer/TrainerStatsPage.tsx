import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../../lib/apiClient';
import { ApiResponse } from '../../types/auth';
import { TrainerStatsResponse } from '../../types/stats';
import { Calendar } from 'lucide-react';

export const TrainerStatsPage: React.FC = () => {
  const navigate = useNavigate();
  const [stats, setStats] = useState<TrainerStatsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchTrainerStats();
  }, []);

  const fetchTrainerStats = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiClient.get<ApiResponse<TrainerStatsResponse>>('/statistics/trainer/my-stats');
      if (res.data.success && res.data.data) {
        setStats(res.data.data);
      } else {
        throw new Error(res.data.message || 'Không thể tải dữ liệu thống kê');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Đã có lỗi xảy ra khi tải thống kê');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="main-content">
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <p>Đang tải dữ liệu thống kê giảng dạy...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="main-content">
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1>Thống kê Giảng dạy HLV</h1>
          <p style={{ marginTop: '0.25rem' }}>
            Tổng hợp kết quả công tác giảng dạy, số buổi đứng lớp và chỉ số chuyên cần của học viên.
          </p>
        </div>
        <button className="btn btn-secondary" onClick={() => navigate('/schedule')} style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
          <Calendar size={15} />
          <span>Xem Lịch giảng dạy</span>
        </button>
      </div>

      {error && <div className="alert alert-danger" style={{ marginBottom: '1.5rem' }}>{error}</div>}

      {stats && (
        <>
          {/* KPI Cards Grid */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(230px, 1fr))', gap: '1.25rem', marginBottom: '2rem' }}>
            <div className="card" style={{ borderLeft: '4px solid var(--primary)' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>BUỔI ĐÃ DẠY</span>
              <h2 style={{ fontSize: '2.25rem', color: 'var(--primary)', marginTop: '0.25rem' }}>
                {stats.totalSessionsTaught}
              </h2>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                Các buổi tập đã hoàn tất (COMPLETED)
              </p>
            </div>

            <div className="card" style={{ borderLeft: '4px solid var(--terracotta)' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>BUỔI SẮP TỚI</span>
              <h2 style={{ fontSize: '2.25rem', color: 'var(--terracotta)', marginTop: '0.25rem' }}>
                {stats.totalUpcomingSessions}
              </h2>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                Các buổi đã lên lịch (SCHEDULED)
              </p>
            </div>

            <div className="card" style={{ borderLeft: '4px solid var(--success)' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>HỌC VIÊN CÓ MẶT</span>
              <h2 style={{ fontSize: '2.25rem', color: 'var(--success)', marginTop: '0.25rem' }}>
                {stats.totalStudentsAttended}
              </h2>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                Lượt học viên tham gia học đầy đủ
              </p>
            </div>

            <div className="card" style={{ borderLeft: '4px solid var(--danger)' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>HỌC VIÊN VẮNG MẶT</span>
              <h2 style={{ fontSize: '2.25rem', color: 'var(--danger)', marginTop: '0.25rem' }}>
                {stats.totalStudentsAbsent}
              </h2>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                Lượt vắng mặt không được hoàn buổi
              </p>
            </div>
          </div>

          {/* Tỷ lệ chuyên cần Card */}
          <div className="card" style={{ marginBottom: '2rem' }}>
            <h3 style={{ marginBottom: '1rem' }}>Tỷ lệ Chuyên cần Tổng thể</h3>
            <div style={{ display: 'flex', alignItems: 'center', gap: '2rem', flexWrap: 'wrap' }}>
              <div style={{ minWidth: '150px' }}>
                <span style={{ fontSize: '3rem', fontWeight: 800, color: 'var(--primary)' }}>
                  {stats.attendanceRate}%
                </span>
              </div>
              <div style={{ flex: 1, minWidth: '240px' }}>
                <p style={{ marginBottom: '0.5rem', fontWeight: 600 }}>
                  Công thức: [Số học viên có mặt / (Có mặt + Vắng mặt)] × 100%
                </p>
                <div style={{ width: '100%', height: '14px', background: 'var(--bg-surface-soft)', borderRadius: 'var(--radius-full)', overflow: 'hidden' }}>
                  <div
                    style={{
                      width: `${stats.attendanceRate}%`,
                      height: '100%',
                      background: 'var(--primary)',
                      borderRadius: 'var(--radius-full)',
                      transition: 'width 1s cubic-bezier(0.16, 1, 0.3, 1)'
                    }}
                  />
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>
                  <span>0%</span>
                  <span>Mục tiêu chuyên cần: ≥ 85%</span>
                  <span>100%</span>
                </div>
              </div>
            </div>
          </div>

          {/* Hướng dẫn nghiệp vụ giảng dạy */}
          <div className="card" style={{ background: 'var(--bg-surface-soft)' }}>
            <h4 style={{ marginBottom: '0.75rem', color: 'var(--text-main)' }}>Quy định nghiệp vụ HLV Pilates</h4>
            <ul style={{ paddingLeft: '1.25rem', color: 'var(--text-muted)', lineHeight: '1.8' }}>
              <li>HLV chỉ thực hiện điểm danh cho buổi học do chính mình phụ trách.</li>
              <li>Chức năng điểm danh chỉ mở từ lúc buổi tập bắt đầu trở đi (không điểm danh trước).</li>
              <li>Học viên vắng mặt không được hoàn lượt tập theo quy tắc phòng tập.</li>
              <li>Sau khi kết thúc buổi dạy, HLV nhấn "Lưu & Hoàn tất buổi tập" để cập nhật trạng thái lớp sang COMPLETED.</li>
            </ul>
          </div>
        </>
      )}
    </div>
  );
};

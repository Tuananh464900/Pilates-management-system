import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Sparkles, Calendar, CreditCard, CheckCircle2, Heart, ArrowRight, ShieldCheck, Award, Clock, BarChart3, Package, MapPin, Phone } from 'lucide-react';

export const HomePage: React.FC = () => {
  const { user, role } = useAuth();

  return (
    <div>
      {/* 1. Hero Section - Phong cách Glow Pilates */}
      <section style={{
        background: 'linear-gradient(180deg, #FDF7F8 0%, #FAF1F3 100%)',
        borderBottom: '1px solid var(--border-light)',
        padding: '4.5rem 1.5rem 5rem',
        position: 'relative',
        overflow: 'hidden'
      }}>
        <div style={{ maxWidth: '1200px', margin: '0 auto', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '3rem', alignItems: 'center' }}>
          <div>
            <div style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.5rem',
              padding: '0.4rem 1rem',
              background: '#FFFFFF',
              borderRadius: 'var(--radius-full)',
              border: '1px solid var(--border-light)',
              boxShadow: 'var(--shadow-sm)',
              marginBottom: '1.5rem'
            }}>
              <Sparkles size={16} color="var(--primary)" />
              <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--primary)', letterSpacing: '0.04em', textTransform: 'uppercase' }}>
                Boutique Pilates Studio · N06-12
              </span>
            </div>

            <h1 style={{
              fontSize: 'clamp(2.4rem, 5vw, 3.8rem)',
              lineHeight: 1.15,
              marginBottom: '1.5rem',
              color: 'var(--text-main)',
              fontFamily: 'var(--font-serif)',
              fontWeight: 600
            }}>
              Find Your Balance, Strength & <span style={{ fontStyle: 'italic', color: 'var(--primary)', fontWeight: 400 }}>Inner Glow</span>
            </h1>

            <p style={{
              fontSize: '1.1rem',
              color: 'var(--text-muted)',
              marginBottom: '2.25rem',
              lineHeight: 1.7,
              maxWidth: '540px'
            }}>
              Trải nghiệm các lớp học Pilates trên máy Reformer cao cấp và Mat Pilates cổ điển. Giúp phục hồi cột sống, kiến tạo vóc dáng thon gọn và nuôi dưỡng sự tĩnh tại trong tâm trí.
            </p>

            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
              <Link to="/schedule" className="btn btn-primary btn-lg">
                <Calendar size={18} />
                <span>Đặt lịch buổi tập ngay</span>
              </Link>
              <Link to="/buy-package" className="btn btn-secondary btn-lg">
                <span>Khám phá gói tập</span>
                <ArrowRight size={18} />
              </Link>
            </div>

            {/* Micro Trust Badges */}
            <div style={{ display: 'flex', gap: '2rem', marginTop: '2.5rem', borderTop: '1px solid var(--border-light)', paddingTop: '1.5rem', flexWrap: 'wrap' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                <Award size={20} color="var(--primary)" />
                <span style={{ fontSize: '0.88rem', fontWeight: 600 }}>HLV chứng nhận quốc tế</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                <ShieldCheck size={20} color="var(--primary)" />
                <span style={{ fontSize: '0.88rem', fontWeight: 600 }}>Thiết bị Reformer cao cấp</span>
              </div>
            </div>
          </div>

          {/* Hero Visual Card */}
          <div style={{ position: 'relative' }}>
            <div style={{
              position: 'relative',
              borderRadius: 'var(--radius-xl)',
              overflow: 'hidden',
              boxShadow: 'var(--shadow-xl)',
              border: '6px solid #FFFFFF',
              background: '#FFFFFF',
              height: '460px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              <img
                src="/pilates-studio-hero.jpg"
                alt="Phòng tập Pilates Reformer cao cấp"
                style={{
                  width: '100%',
                  height: '100%',
                  display: 'block',
                  objectFit: 'cover',
                  objectPosition: 'center center'
                }}
              />
              <div style={{
                position: 'absolute',
                bottom: '1.25rem',
                left: '1.25rem',
                right: '1.25rem',
                background: 'rgba(255, 255, 255, 0.94)',
                backdropFilter: 'blur(10px)',
                padding: '1rem 1.25rem',
                borderRadius: 'var(--radius-lg)',
                boxShadow: 'var(--shadow-md)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                border: '1px solid rgba(255, 255, 255, 0.6)'
              }}>
                <div>
                  <div style={{ fontSize: '0.78rem', textTransform: 'uppercase', color: 'var(--primary)', fontWeight: 700 }}>
                    Lớp học gợi ý
                  </div>
                  <div style={{ fontFamily: 'var(--font-serif)', fontSize: '1.15rem', fontWeight: 600 }}>
                    Reformer Full-Body Alignment
                  </div>
                </div>
                <Link to="/schedule" className="btn btn-sm btn-primary">
                  Đặt ngay
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 2. Banner chào đón nếu đã đăng nhập */}
      {user && (
        <section style={{ maxWidth: '1280px', margin: '2rem auto 0', padding: '0 1.5rem' }}>
          <div className="card" style={{
            background: 'linear-gradient(90deg, #FFFFFF 0%, var(--bg-surface-soft) 100%)',
            borderLeft: '5px solid var(--primary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: '1rem'
          }}>
            <div>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Cổng dịch vụ cá nhân</span>
              <h3 style={{ fontSize: '1.35rem', marginTop: '0.15rem' }}>
                Chào mừng bạn trở lại, <strong>{user.email}</strong>!
              </h3>
            </div>
            <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
              {role === 'ADMIN' && (
                <Link to="/admin/dashboard" className="btn btn-primary btn-sm" style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                  <BarChart3 size={15} />
                  <span>Mở Admin Dashboard</span>
                </Link>
              )}
              {role === 'TRAINER' && (
                <Link to="/schedule" className="btn btn-primary btn-sm" style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                  <Calendar size={15} />
                  <span>Lịch dạy & Điểm danh hôm nay</span>
                </Link>
              )}
              {role === 'MEMBER' && (
                <>
                  <Link to="/schedule" className="btn btn-primary btn-sm" style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                    <Calendar size={15} />
                    <span>Đặt lịch tập ngay</span>
                  </Link>
                  <Link to="/my-membership" className="btn btn-secondary btn-sm" style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                    <Package size={15} />
                    <span>Xem gói tập của tôi</span>
                  </Link>
                </>
              )}
            </div>
          </div>
        </section>
      )}

      {/* 3. Section Bộ môn Pilates (Classes Showcase) */}
      <section style={{ maxWidth: '1280px', margin: '4rem auto 0', padding: '0 1.5rem' }}>
        <div style={{ textAlign: 'center', maxWidth: '640px', margin: '0 auto 3rem' }}>
          <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--primary)', textTransform: 'uppercase', letterSpacing: '0.08em' }}>
            Our Pilates Programs
          </span>
          <h2 style={{ fontSize: '2.4rem', marginTop: '0.5rem', marginBottom: '0.75rem' }}>
            Hệ Thống Lớp Học Tại Glow Studio
          </h2>
          <p>
            Được thiết kế tỉ mỉ phù hợp cho mọi cấp độ từ người mới bắt đầu đến nâng cao, hướng đến sự chuẩn xác trong từng chuyển động.
          </p>
        </div>

        <div className="grid grid-cols-3">
          {/* Lớp 1 */}
          <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div>
              <div style={{ height: '180px', borderRadius: 'var(--radius-md)', overflow: 'hidden', marginBottom: '1.25rem', background: 'var(--bg-surface-pink)' }}>
                <img
                  src="/class-reformer.jpg"
                  alt="Reformer Pilates"
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />
              </div>
              <span className="badge badge-scheduled" style={{ marginBottom: '0.5rem' }}>Reformer Machine</span>
              <h3 style={{ fontSize: '1.4rem', marginBottom: '0.5rem' }}>Reformer Pilates</h3>
              <p style={{ fontSize: '0.9rem', marginBottom: '1rem' }}>
                Luyện tập với máy Reformer chuyên sâu, hệ thống lò xo kháng lực giúp kéo giãn cơ thể và định hình vóc dáng thon dài.
              </p>
            </div>
            <div style={{ borderTop: '1px solid var(--border-light)', paddingTop: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'inline-flex', alignItems: 'center', gap: 5 }}>
                <Clock size={14} />
                <span>50 phút · Max 6-8 học viên</span>
              </span>
              <Link to="/schedule" className="btn btn-secondary btn-sm">Xem lịch</Link>
            </div>
          </div>

          {/* Lớp 2 */}
          <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div>
              <div style={{ height: '180px', borderRadius: 'var(--radius-md)', overflow: 'hidden', marginBottom: '1.25rem', background: 'var(--bg-surface-pink)' }}>
                <img
                  src="/class-mat.jpg"
                  alt="Classical Mat Pilates"
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />
              </div>
              <span className="badge badge-active" style={{ marginBottom: '0.5rem' }}>Classical Mat</span>
              <h3 style={{ fontSize: '1.4rem', marginBottom: '0.5rem' }}>Mat Pilates & Core</h3>
              <p style={{ fontSize: '0.9rem', marginBottom: '1rem' }}>
                Các bài tập trên thảm truyền thống kết hợp dụng cụ bóng, vòng Pilates tập trung xây dựng cơ lõi và tăng cường độ dẻo dai.
              </p>
            </div>
            <div style={{ borderTop: '1px solid var(--border-light)', paddingTop: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'inline-flex', alignItems: 'center', gap: 5 }}>
                <Clock size={14} />
                <span>50 phút · Max 10 học viên</span>
              </span>
              <Link to="/schedule" className="btn btn-secondary btn-sm">Xem lịch</Link>
            </div>
          </div>

          {/* Lớp 3 */}
          <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div>
              <div style={{ height: '180px', borderRadius: 'var(--radius-md)', overflow: 'hidden', marginBottom: '1.25rem', background: 'var(--bg-surface-pink)' }}>
                <img
                  src="/class-spine.jpg"
                  alt="Tower & Spine Recovery"
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />
              </div>
              <span className="badge badge-scheduled" style={{ marginBottom: '0.5rem' }}>Recovery & Posture</span>
              <h3 style={{ fontSize: '1.4rem', marginBottom: '0.5rem' }}>Spine Alignment & Tower</h3>
              <p style={{ fontSize: '0.9rem', marginBottom: '1rem' }}>
                Liệu trình phục hồi dành cho dân văn phòng, hỗ trợ cải thiện tư thế gù lưng, đau mỏi thắt lưng và mở khớp vai.
              </p>
            </div>
            <div style={{ borderTop: '1px solid var(--border-light)', paddingTop: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'inline-flex', alignItems: 'center', gap: 5 }}>
                <Clock size={14} />
                <span>50 phút · Max 5 học viên</span>
              </span>
              <Link to="/schedule" className="btn btn-secondary btn-sm">Xem lịch</Link>
            </div>
          </div>
        </div>
      </section>

      {/* 4. Section Bảng giá Gói tập (Memberships) */}
      <section style={{
        background: 'var(--bg-surface-soft)',
        borderTop: '1px solid var(--border-light)',
        borderBottom: '1px solid var(--border-light)',
        margin: '5rem 0 0',
        padding: '4.5rem 1.5rem'
      }}>
        <div style={{ maxWidth: '1280px', margin: '0 auto' }}>
          <div style={{ textAlign: 'center', maxWidth: '640px', margin: '0 auto 3rem' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--primary)', textTransform: 'uppercase', letterSpacing: '0.08em' }}>
              Pricing & Memberships
            </span>
            <h2 style={{ fontSize: '2.4rem', marginTop: '0.5rem', marginBottom: '0.75rem' }}>
              Gói Tập Linh Hoạt Tại Glow Studio
            </h2>
            <p>
              Chọn gói tập phù hợp với mục tiêu cá nhân. Thanh toán an toàn, linh hoạt gia hạn và theo dõi số buổi minh bạch.
            </p>
          </div>

          <div className="grid grid-cols-3">
            {/* Gói Starter */}
            <div className="card" style={{ background: '#FFFFFF', textAlign: 'center', padding: '2.5rem 2rem' }}>
              <span style={{ fontSize: '0.85rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                Khởi đầu
              </span>
              <h3 style={{ fontSize: '1.75rem', marginTop: '0.5rem', marginBottom: '1rem' }}>Gói Trải Nghiệm</h3>
              <div style={{ fontSize: '2.5rem', fontWeight: 700, color: 'var(--text-main)', fontFamily: 'var(--font-serif)', marginBottom: '0.5rem' }}>
                950.000đ
              </div>
              <p style={{ fontSize: '0.88rem', marginBottom: '1.75rem' }}>Thích hợp cho học viên mới làm quen với bộ môn</p>
              
              <ul style={{ listStyle: 'none', padding: 0, textAlign: 'left', marginBottom: '2rem', display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.92rem' }}>
                <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <CheckCircle2 size={16} color="var(--primary)" /> <strong>05 buổi tập</strong> tự do chọn lịch
                </li>
                <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <CheckCircle2 size={16} color="var(--primary)" /> Hạn sử dụng trong <strong>30 ngày</strong>
                </li>
                <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <CheckCircle2 size={16} color="var(--primary)" /> Hủy buổi trước 2 giờ được hoàn lượt
                </li>
              </ul>

              <Link to="/buy-package" className="btn btn-secondary" style={{ width: '100%' }}>
                Đăng ký gói này
              </Link>
            </div>

            {/* Gói Standard - Nổi bật */}
            <div className="card" style={{
              background: '#FFFFFF',
              textAlign: 'center',
              padding: '2.5rem 2rem',
              border: '2px solid var(--primary)',
              position: 'relative',
              boxShadow: 'var(--shadow-lg)'
            }}>
              <div style={{
                position: 'absolute',
                top: '-12px',
                left: '50%',
                transform: 'translateX(-50%)',
                background: 'var(--primary)',
                color: '#FFFFFF',
                padding: '0.2rem 1rem',
                borderRadius: 'var(--radius-full)',
                fontSize: '0.75rem',
                fontWeight: 700,
                textTransform: 'uppercase',
                letterSpacing: '0.05em'
              }}>
                Được yêu thích nhất
              </div>

              <span style={{ fontSize: '0.85rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--primary)' }}>
                Phổ biến
              </span>
              <h3 style={{ fontSize: '1.75rem', marginTop: '0.5rem', marginBottom: '1rem' }}>Gói Glow Tiêu Chuẩn</h3>
              <div style={{ fontSize: '2.5rem', fontWeight: 700, color: 'var(--primary)', fontFamily: 'var(--font-serif)', marginBottom: '0.5rem' }}>
                1.750.000đ
              </div>
              <p style={{ fontSize: '0.88rem', marginBottom: '1.75rem' }}>Duy trì tập luyện đều đặn 2-3 buổi/tuần</p>
              
              <ul style={{ listStyle: 'none', padding: 0, textAlign: 'left', marginBottom: '2rem', display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.92rem' }}>
                <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <CheckCircle2 size={16} color="var(--primary)" /> <strong>10 buổi tập</strong> toàn quyền chọn lớp
                </li>
                <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <CheckCircle2 size={16} color="var(--primary)" /> Hạn sử dụng trong <strong>60 ngày</strong>
                </li>
                <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <CheckCircle2 size={16} color="var(--primary)" /> Tự do gia hạn cùng loại gói
                </li>
                <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <CheckCircle2 size={16} color="var(--primary)" /> Cảnh báo nhắc hạn trên giao diện
                </li>
              </ul>

              <Link to="/buy-package" className="btn btn-primary" style={{ width: '100%' }}>
                Mua ngay gói này
              </Link>
            </div>

            {/* Gói VIP / Unlimited */}
            <div className="card" style={{ background: '#FFFFFF', textAlign: 'center', padding: '2.5rem 2rem' }}>
              <span style={{ fontSize: '0.85rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)' }}>
                Chuyên sâu
              </span>
              <h3 style={{ fontSize: '1.75rem', marginTop: '0.5rem', marginBottom: '1rem' }}>Gói Glow VIP Member</h3>
              <div style={{ fontSize: '2.5rem', fontWeight: 700, color: 'var(--text-main)', fontFamily: 'var(--font-serif)', marginBottom: '0.5rem' }}>
                4.500.000đ
              </div>
              <p style={{ fontSize: '0.88rem', marginBottom: '1.75rem' }}>Dành cho hội viên cam kết chuyển hóa vóc dáng</p>
              
              <ul style={{ listStyle: 'none', padding: 0, textAlign: 'left', marginBottom: '2rem', display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.92rem' }}>
                <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <CheckCircle2 size={16} color="var(--primary)" /> <strong>30 buổi tập</strong> không giới hạn môn
                </li>
                <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <CheckCircle2 size={16} color="var(--primary)" /> Hạn sử dụng dài hạn <strong>120 ngày</strong>
                </li>
                <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <CheckCircle2 size={16} color="var(--primary)" /> Ưu tiên đặt lịch trước trong tuần
                </li>
              </ul>

              <Link to="/buy-package" className="btn btn-secondary" style={{ width: '100%' }}>
                Đăng ký gói này
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* 5. Section Quy trình 5 Nhóm Chức năng Nghiệp vụ */}
      <section style={{ maxWidth: '1280px', margin: '4rem auto 0', padding: '0 1.5rem' }}>
        <div style={{ textAlign: 'center', maxWidth: '640px', margin: '0 auto 2.5rem' }}>
          <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--primary)', textTransform: 'uppercase', letterSpacing: '0.08em' }}>
            Tính Năng Nổi Bật
          </span>
          <h2 style={{ fontSize: '2.2rem', marginTop: '0.5rem' }}>
            Trọn Bộ 5 Nhóm Chức Năng Phần Mềm
          </h2>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem' }}>
          <div className="card" style={{ borderTop: '4px solid var(--primary)' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--primary)' }}>01</span>
            <h4 style={{ fontSize: '1.15rem', margin: '0.25rem 0 0.5rem' }}>Tài khoản & Hồ sơ</h4>
            <p style={{ fontSize: '0.85rem' }}>Đăng ký hội viên, quản lý tài khoản Trainer, khóa/mở an toàn qua Supabase Auth.</p>
          </div>

          <div className="card" style={{ borderTop: '4px solid var(--accent-gold)' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--accent-gold)' }}>02</span>
            <h4 style={{ fontSize: '1.15rem', margin: '0.25rem 0 0.5rem' }}>Lớp & Lịch tập</h4>
            <p style={{ fontSize: '0.85rem' }}>Mẫu lớp Pilates, phòng tập, xếp lịch chống xung đột phòng/HLV, hủy buổi hoàn lượt.</p>
          </div>

          <div className="card" style={{ borderTop: '4px solid var(--success)' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--success)' }}>03</span>
            <h4 style={{ fontSize: '1.15rem', margin: '0.25rem 0 0.5rem' }}>Gói tập & Thanh toán</h4>
            <p style={{ fontSize: '0.85rem' }}>Giao dịch UNPAID/PAID, kích hoạt/gia hạn cùng loại gói, cảnh báo hết hạn/lượt tập.</p>
          </div>

          <div className="card" style={{ borderTop: '4px solid var(--primary-dark)' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--primary-dark)' }}>04</span>
            <h4 style={{ fontSize: '1.15rem', margin: '0.25rem 0 0.5rem' }}>Đăng ký & Hủy buổi</h4>
            <p style={{ fontSize: '0.85rem' }}>Khóa bi quan chống race condition, trừ 1 lượt nguyên tử, biên 2 giờ hoàn buổi.</p>
          </div>

          <div className="card" style={{ borderTop: '4px solid #2563EB' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: '#2563EB' }}>05</span>
            <h4 style={{ fontSize: '1.15rem', margin: '0.25rem 0 0.5rem' }}>Điểm danh & Thống kê</h4>
            <p style={{ fontSize: '0.85rem' }}>Điểm danh từ giờ tập (vắng không hoàn lượt), KPI HLV và Dashboard Admin doanh thu thực.</p>
          </div>
        </div>
      </section>

      {/* 6. Footer Glow Studio */}
      <footer style={{
        background: '#201C1D',
        color: '#FFFFFF',
        marginTop: '5rem',
        padding: '3.5rem 1.5rem 2rem'
      }}>
        <div style={{ maxWidth: '1280px', margin: '0 auto', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '2.5rem', marginBottom: '2.5rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontFamily: 'var(--font-serif)', fontSize: '1.4rem', fontWeight: 700, marginBottom: '0.75rem' }}>
              <Sparkles size={20} color="var(--primary)" />
              <span>Glow Pilates Studio</span>
            </div>
            <p style={{ color: '#A89E9F', fontSize: '0.9rem', lineHeight: 1.6 }}>
              Hệ thống quản lý phòng tập Pilates chuyên nghiệp dành cho sinh viên và ban quản trị nhóm N06-12.
            </p>
          </div>

          <div>
            <h5 style={{ color: '#FFFFFF', marginBottom: '0.75rem', fontSize: '1rem' }}>Liên kết nhanh</h5>
            <ul style={{ listStyle: 'none', padding: 0, display: 'flex', flexDirection: 'column', gap: '0.4rem', fontSize: '0.9rem' }}>
              <li><Link to="/schedule" style={{ color: '#A89E9F' }}>Thời khóa biểu các lớp</Link></li>
              <li><Link to="/buy-package" style={{ color: '#A89E9F' }}>Bảng giá gói tập</Link></li>
              <li><Link to="/login" style={{ color: '#A89E9F' }}>Đăng nhập hội viên</Link></li>
              <li><Link to="/register" style={{ color: '#A89E9F' }}>Đăng ký tài khoản</Link></li>
            </ul>
          </div>

          <div>
            <h5 style={{ color: '#FFFFFF', marginBottom: '0.75rem', fontSize: '1rem' }}>Thông tin phòng tập</h5>
            <div style={{ color: '#A89E9F', fontSize: '0.9rem', lineHeight: 1.8, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <MapPin size={15} style={{ color: 'var(--primary)', flexShrink: 0 }} />
                <span>Tầng 3, Tòa nhà Pilates Wellness, Hà Nội</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Phone size={15} style={{ color: 'var(--primary)', flexShrink: 0 }} />
                <span>Hotline: 0988.123.456</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Clock size={15} style={{ color: 'var(--primary)', flexShrink: 0 }} />
                <span>Giờ mở cửa: 06:30 - 21:30 hàng ngày</span>
              </div>
            </div>
          </div>
        </div>

        <div style={{ maxWidth: '1280px', margin: '0 auto', borderTop: '1px solid rgba(255, 255, 255, 0.1)', paddingTop: '1.5rem', textAlign: 'center', fontSize: '0.82rem', color: '#7E7475' }}>
          <span>© 2026 Glow Pilates Management (N06-12).</span>
        </div>
      </footer>
    </div>
  );
};

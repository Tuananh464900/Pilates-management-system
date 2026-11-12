import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LogOut, User as UserIcon, Calendar, Sparkles, BarChart3, ChevronDown, Layers, DoorClosed, CreditCard, Package, Users } from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, role, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        {/* Brand Logo phong cách Glow Pilates */}
        <Link to="/" className="brand-logo">
          <Sparkles size={22} style={{ color: 'var(--primary)' }} />
          <span>Glow Pilates</span>
        </Link>

        {/* Menu dịch vụ chung */}
        <nav style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          {user && (
            <Link to="/schedule" className="nav-item">
              <Calendar size={15} />
              <span>Thời khóa biểu</span>
            </Link>
          )}
          <Link to="/buy-package" className="nav-item">
            <span>Gói tập & Hội viên</span>
          </Link>
        </nav>

        {/* Khu vực Người dùng & Phân quyền */}
        {user ? (
          <nav className="nav-links">
            {role === 'ADMIN' && (
              <>
                <Link to="/admin/dashboard" className="nav-item" style={{ fontWeight: 600, color: 'var(--primary)', background: 'var(--primary-light)' }}>
                  <BarChart3 size={15} />
                  <span>Dashboard</span>
                </Link>

                {/* Dropdown Menu gom gọn các nghiệp vụ quản lý */}
                <div className="nav-dropdown">
                  <button type="button" className="nav-dropdown-btn">
                    <span>Quản lý hệ thống</span>
                    <ChevronDown size={14} />
                  </button>
                  <div className="nav-dropdown-menu">
                    <Link to="/admin/sessions" className="dropdown-item">
                      <Calendar size={15} />
                      <span>Quản lý Buổi tập</span>
                    </Link>
                    <Link to="/admin/payments" className="dropdown-item">
                      <CreditCard size={15} />
                      <span>Quản lý Doanh thu</span>
                    </Link>
                    <Link to="/admin/packages" className="dropdown-item">
                      <Package size={15} />
                      <span>Quản lý Gói tập</span>
                    </Link>
                    <Link to="/admin/classes" className="dropdown-item">
                      <Layers size={15} />
                      <span>Quản lý Lớp Pilates</span>
                    </Link>
                    <Link to="/admin/rooms" className="dropdown-item">
                      <DoorClosed size={15} />
                      <span>Quản lý Phòng tập</span>
                    </Link>
                    <Link to="/admin/users" className="dropdown-item">
                      <Users size={15} />
                      <span>Quản lý Tài khoản & HLV</span>
                    </Link>
                  </div>
                </div>
              </>
            )}

            {role === 'TRAINER' && (
              <>
                <Link to="/schedule" className="nav-item">
                  <Calendar size={15} />
                  <span>Lịch dạy & Điểm danh</span>
                </Link>
                <Link to="/trainer/stats" className="nav-item" style={{ fontWeight: 600, color: 'var(--primary)' }}>
                  <BarChart3 size={15} />
                  <span>Thống kê Giảng dạy</span>
                </Link>
              </>
            )}

            {role === 'MEMBER' && (
              <>
                <Link to="/my-bookings" className="nav-item">
                  <Calendar size={15} />
                  <span>Lịch đã đặt</span>
                </Link>
                <Link to="/my-membership" className="nav-item">
                  <span>Gói của tôi</span>
                </Link>
                <Link to="/my-payments" className="nav-item">
                  <span>Lịch sử thanh toán</span>
                </Link>
              </>
            )}

            {/* Profile Dropdown / Link */}
            <Link to="/profile" className="nav-item" style={{ border: '1px solid var(--border-light)' }}>
              <UserIcon size={15} />
              <span style={{ maxWidth: '140px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {user.email}
              </span>
              <span className={`badge badge-${role?.toLowerCase()}`}>
                {role}
              </span>
            </Link>

            <button onClick={handleLogout} className="btn btn-secondary btn-sm" title="Đăng xuất">
              <LogOut size={14} />
              <span>Thoát</span>
            </button>
          </nav>
        ) : (
          <div style={{ display: 'flex', gap: '0.65rem', alignItems: 'center' }}>
            <Link to="/login" className="btn btn-secondary btn-sm">
              Đăng nhập
            </Link>
            <Link to="/register" className="btn btn-primary btn-sm">
              Đăng ký ngay
            </Link>
          </div>
        )}
      </div>
    </header>
  );
};

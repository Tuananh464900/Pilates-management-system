import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';

import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { ForgotPasswordPage } from './pages/auth/ForgotPasswordPage';
import { ResetPasswordPage } from './pages/auth/ResetPasswordPage';
import { ProfilePage } from './pages/profile/ProfilePage';
import { SchedulePage } from './pages/SchedulePage';
import { UserManagePage } from './pages/admin/UserManagePage';
import { AdminProfileManagePage } from './pages/admin/AdminProfileManagePage';
import { ClassManagePage } from './pages/admin/ClassManagePage';
import { RoomManagePage } from './pages/admin/RoomManagePage';
import { SessionManagePage } from './pages/admin/SessionManagePage';
import { PackageManagePage } from './pages/admin/PackageManagePage';
import { AdminPaymentManagePage } from './pages/admin/AdminPaymentManagePage';
import { BuyPackagePage } from './pages/member/BuyPackagePage';
import { MyMembershipPage } from './pages/member/MyMembershipPage';
import { PaymentHistoryPage } from './pages/member/PaymentHistoryPage';
import { MyBookingsPage } from './pages/member/MyBookingsPage';
import { TrainerAttendancePage } from './pages/trainer/TrainerAttendancePage';
import { TrainerStatsPage } from './pages/trainer/TrainerStatsPage';
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <div className="app-container">
          <Navbar />
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route
              path="/schedule"
              element={
                <ProtectedRoute>
                  <SchedulePage />
                </ProtectedRoute>
              }
            />
            <Route path="/buy-package" element={<BuyPackagePage />} />

            {/* Tuyến đường yêu cầu đăng nhập */}
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <ProfilePage />
                </ProtectedRoute>
              }
            />

            {/* Tuyến đường dành riêng cho MEMBER */}
            <Route
              path="/my-bookings"
              element={
                <ProtectedRoute allowedRoles={['MEMBER']}>
                  <MyBookingsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-membership"
              element={
                <ProtectedRoute allowedRoles={['MEMBER']}>
                  <MyMembershipPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-payments"
              element={
                <ProtectedRoute allowedRoles={['MEMBER']}>
                  <PaymentHistoryPage />
                </ProtectedRoute>
              }
            />

            {/* Tuyến đường dành riêng cho ADMIN */}
            <Route
              path="/admin/dashboard"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminDashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/users"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <UserManagePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/profiles"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminProfileManagePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/classes"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <ClassManagePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/rooms"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <RoomManagePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/sessions"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <SessionManagePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/packages"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <PackageManagePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/payments"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminPaymentManagePage />
                </ProtectedRoute>
              }
            />

            {/* Tuyến đường dành cho TRAINER & ADMIN */}
            <Route
              path="/trainer/attendance/:sessionId"
              element={
                <ProtectedRoute allowedRoles={['TRAINER', 'ADMIN']}>
                  <TrainerAttendancePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/trainer/stats"
              element={
                <ProtectedRoute allowedRoles={['TRAINER']}>
                  <TrainerStatsPage />
                </ProtectedRoute>
              }
            />

            {/* Mặc định chuyển về trang chủ */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;

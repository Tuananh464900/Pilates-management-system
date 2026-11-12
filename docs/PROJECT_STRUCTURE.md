# Cấu trúc dự án Hệ thống Quản lý Phòng tập Pilates (Monorepo)

Dự án được tổ chức theo mô hình **Monorepo** với một Backend Spring Boot REST API và một Frontend React TypeScript.

```text
quan-ly-pilates/
├── .vscode/                               # Cấu hình IDE VS Code (Java compile settings)
├── docs/                                  # Tài liệu phân tích thiết kế phần mềm (SAD)
│   ├── N06_12_SAD_Quan_Ly_Phong_Tap_Pilates.docx # Báo cáo SAD hoàn chỉnh Tuần 12
│   ├── ACCOUNTS.md                        # Danh sách tài khoản kiểm thử chính thức (Admin, Trainer, Member)
│   ├── IMPLEMENTATION_PLAN.md             # Đặc tả 25 Use Case & Kế hoạch triển khai (Mục 1-14)
│   ├── PROJECT_STRUCTURE.md               # Sơ đồ cấu trúc dự án thực tế
│   └── README.md                          # Tài liệu tổng quan dự án
│
├── backend/                               # Spring Boot 3.3.4 (Java 17) REST API
│   ├── pom.xml                            # Quản lý thư viện Maven (JPA, Security, Flyway, JJWT...)
│   ├── .env.example                       # Biến môi trường mẫu
│   ├── src/main/java/vn/n06g12/pilates/
│   │   ├── PilatesApplication.java        # Entry point ứng dụng Spring Boot & RLS Runner
│   │   ├── common/                        # Exception handling, ApiResponse, PageResponse, Constants
│   │   ├── config/                        # Cấu hình WebMvc, OpenAPI / Swagger UI
│   │   ├── security/                      # Supabase JWT Filter, Validator, SecurityConfig
│   │   ├── user/                          # UC1: User, MemberProfile, TrainerProfile, Auth, Profile
│   │   ├── pilatesclass/                  # UC2: Quản lý mẫu lớp Pilates (PilatesClass)
│   │   ├── room/                          # UC2: Quản lý phòng tập luyện (Room)
│   │   ├── session/                       # UC2: Xếp lịch buổi tập chống trùng phòng/HLV (Session)
│   │   ├── membership/                    # UC3: Gói tập, Đơn mua UNPAID/PAID, Kích hoạt/Gia hạn gói
│   │   ├── booking/                       # UC4: Đặt chỗ bi quan (Pessimistic Locking), Hủy hoàn lượt
│   │   ├── attendance/                    # UC5: HLV điểm danh buổi tập (Có mặt / Vắng mặt)
│   │   └── statistics/                    # UC5: KPI HLV và Dashboard Doanh thu Admin
│   │
│   ├── src/main/resources/
│   │   ├── application.yml                # Cấu hình DataSource, Flyway, JWT Secret
│   │   ├── application-test.yml           # Cấu hình môi trường Test H2 in-memory
│   │   └── db/
│   │       ├── demo/demo_data.sql         # Dữ liệu lịch tập demo tách riêng độc lập
│   │       └── migration/                 # 9 Flyway SQL Migrations (V1 đến V9)
│   │
│   └── src/test/java/                     # 73 Unit & Integration Tests (11 test classes, 100% Passed)
│
├── frontend/                              # Single Page Application (React 18 + Vite 5 + TypeScript)
│   ├── package.json                       # Thư viện: React, Lucide Icons, Axios, React Router DOM
│   ├── vite.config.ts                     # Cấu hình Vite & API Proxy
│   ├── tsconfig.json                      # Cấu hình TypeScript
│   ├── public/                            # Ảnh phòng tập thật, ảnh 3 bộ môn Reformer, Mat, Tower
│   └── src/
│       ├── main.tsx                       # React DOM entry point
│       ├── App.tsx                        # Hệ thống Router và ProtectedRoute theo 3 vai trò
│       ├── index.css                      # Design system phong cách Soft-Luxury Glow Pilates
│       ├── components/                    # Navbar (Dropdown phân cấp Quản lý hệ thống), ProtectedRoute
│       ├── context/                       # AuthContext quản lý phiên đăng nhập và Token
│       ├── lib/                           # apiClient (Axios Interceptors), formatters chuẩn tiếng Việt
│       ├── types/                         # TypeScript interfaces (Auth, Pilates, Booking, Stats...)
│       └── pages/
│           ├── HomePage.tsx               # Trang chủ giới thiệu studio và bộ môn
│           ├── SchedulePage.tsx           # Thời khóa biểu dạng lưới 7 ngày/tuần trực quan
│           ├── auth/                      # LoginPage, RegisterPage, ForgotPasswordPage, ResetPasswordPage
│           ├── profile/                   # ProfilePage (Hồ sơ cá nhân & Đổi mật khẩu)
│           ├── member/                    # BuyPackagePage, MyMembershipPage, PaymentHistoryPage, MyBookingsPage
│           ├── trainer/                   # TrainerAttendancePage, TrainerStatsPage
│           └── admin/                     # AdminDashboardPage, SessionManagePage, AdminPaymentManagePage,
│                                          # PackageManagePage, ClassManagePage, RoomManagePage, UserManagePage,
│                                          # AdminProfileManagePage
│
├── export_for_report/                     # Thư mục chuẩn bị xuất nộp báo cáo
│   ├── backend.zip                        # Mã nguồn backend nén
│   ├── frontend.zip                       # Mã nguồn frontend nén
│   ├── docs.zip                           # Báo cáo Word SAD và tài liệu markdown nén
│   ├── mvn_test_result.txt                # Nhật ký kết quả chạy 73/73 tests PASS
│   ├── ACCOUNTS.md, IMPLEMENTATION_PLAN.md, PROJECT_STRUCTURE.md, README.md, AGENTS.md
│   └── run-all.bat, run-backend.bat, run-frontend.bat
├── export_for_report.zip                 # Gói zip master tổng thể nộp Canvas (~24 MB)
├── run-all.bat                            # Script 1-Click khởi động cả Backend và Frontend
├── run-backend.bat                        # Script khởi động Backend độc lập
├── run-frontend.bat                       # Script khởi động Frontend độc lập
├── ACCOUNTS.md                            # Danh sách chi tiết tài khoản kiểm thử
├── AGENTS.md                              # Ràng buộc nghiệp vụ và kiến trúc kỹ thuật
├── README.md                              # Hướng dẫn chi tiết dự án & Báo cáo tiến độ
└── .gitignore                             # Cấu hình loại trừ file nhạy cảm và build artifact
```

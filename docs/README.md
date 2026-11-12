# Phần mềm: Quản lý Phòng tập Pilates (Glow Pilates - N06-12)

Dự án bài tập lớn môn **Phân tích và Thiết kế Phần mềm (SAD)**  
Hệ thống quản lý phòng tập Pilates chuyên nghiệp, ứng dụng kiến trúc phân tầng chuẩn Enterprise Monorepo, tối ưu trải nghiệm người dùng với phong cách thiết kế Soft-Luxury Aesthetic.

---

## Mục lục (Table of Contents)

- [1. Giới thiệu dự án](#1-giới-thiệu-dự-án)
- [2. Thành viên nhóm & Giảng viên hướng dẫn](#2-thành-viên-nhóm--giảng-viên-hướng-dẫn)
- [3. Điểm nổi bật & Công nghệ sử dụng](#3-điểm-nổi-bật--công-nghệ-sử-dụng)
- [4. Cấu trúc thư mục Monorepo](#4-cấu-trúc-thư-mục-monorepo)
- [5. Hướng dẫn Khởi chạy Hệ thống](#5-hướng-dẫn-khởi-chạy-hệ-thống)
- [6. Tài khoản Kiểm thử Đăng nhập](#6-tài-khoản-kiểm-thử-đăng-nhập)
- [7. Tiến độ Triển khai 5 Nhóm Use Cases](#7-tiến-độ-triển-khai-5-nhóm-use-cases)
- [8. Kiểm thử Tự động (Automated Testing)](#8-kiểm-thử-tự-động-automated-testing)
- [9. Quy tắc Di trú Database (Flyway Migration Policy & History)](#9-quy-tắc-di-trú-database-flyway-migration-policy--history)
- [10. Tài liệu Tham khảo](#10-tài-liệu-tham-khảo)

---

## 1. Giới thiệu dự án

Hệ thống **Glow Pilates** cung cấp giải pháp chuyển đổi số toàn diện cho chuỗi phòng tập Pilates cao cấp, giải quyết triệt để các bài toán nghiệp vụ phức tạp:
- **Quản lý lịch tập & Chống xung đột:** Xếp lịch buổi tập chống trùng phòng tập và huấn luyện viên, kiểm soát sức chứa phòng tập nghiêm ngặt.
- **Quản lý gói tập & Thanh toán:** Hỗ trợ đa dạng gói tập (10, 20, 36, 60 buổi), kích hoạt và gia hạn gói nguyên tử trong 1 Transaction, chặn tạo 2 gói hoạt động cùng lúc.
- **Đặt chỗ & Xử lý đồng thời (Concurrency Safety):** Ứng dụng khóa bi quan (`PESSIMISTIC_WRITE`) chống Race Condition khi nhiều học viên cùng đặt chỗ cuối, tự động hoàn lượt theo quy tắc biên 2 giờ.
- **Điểm danh & Thống kê doanh thu:** Huấn luyện viên điểm danh trực quan, Quản trị viên theo dõi Dashboard doanh thu thực tế theo tháng/năm.

---

## 2. Thành viên nhóm & Giảng viên hướng dẫn

| STT | Họ và tên | Mã sinh viên | Vai trò trong dự án | GitHub |
| :---: | :--- | :---: | :--- | :---: |
| 1 | **Hồ Tuấn Anh** | **24100464** | Nhóm trưởng, Phân tích Use Case & Lập trình Backend | [@Tuananh464900](https://github.com/Tuananh464900) |
| 2 | **Đỗ Thanh Bình** | **24100465** | Thành viên, Phân tích Use Case & Lập trình Frontend | [@dtbinh3406](https://github.com/dtbinh3406) |

- **Giảng viên hướng dẫn:** TS. Mai Thuý Nga
- **Lớp tín chỉ:** CSE703048-1-1-26(N06)
- **Đơn vị đào tạo:** Trường Công nghệ Thông tin Phenikaa – Đại học Phenikaa

---

## 3. Điểm nổi bật & Công nghệ sử dụng

### 1. Backend (REST API):
- **Ngôn ngữ & Framework:** Java 17 LTS, Spring Boot 3.3.4.
- **Bảo mật & Phân quyền:** Spring Security 6, JJWT (HMAC SHA-256), Supabase Auth JWT Filter.
- **Cơ sở dữ liệu & ORM:** PostgreSQL 16 (Supabase Session Pooler), Spring Data JPA, Hibernate.
- **Quản lý phiên bản DB:** Flyway Migration (V1 đến V9 chuẩn Forward-Only, bảo toàn dữ liệu, chống xung đột lịch trùng).
- **Tài liệu API:** Springdoc OpenAPI 3.0 / Swagger UI (**12 Controllers, 56 Endpoints**).
- **Kiểm thử tự động:** JUnit 5, Mockito, AssertJ (Đạt **73/73** test case Unit & Integration Test qua 11 Test Classes, 100% Passed).

### 2. Frontend (Single Page Application):
- **Core:** React 18, TypeScript, Vite 5.
- **Kiến trúc giao diện:** Glow Pilates Custom Design System (Soft-Luxury Aesthetic, Warm Beige & Dusty Rose Palette).
- **Icons & Visual:** Lucide React (100% Vector SVG, không dùng emoji thô).
- **Điều hướng & Bảo vệ tuyến đường:** React Router DOM v6, Role-based `ProtectedRoute`.
- **Quản lý API:** Axios Client với Interceptor tự động gắn Bearer Token và xử lý lỗi tập trung.

---

## 4. Cấu trúc thư mục Monorepo

```text
quan-ly-pilates/
├── backend/                               # Spring Boot 3.3.4 (Java 17) REST API
│   ├── src/main/java/vn/n06g12/pilates/   # Controller, Service, Repository, Entity, DTO, Security
│   ├── src/main/resources/                # application.yml, db/migration (Flyway V1 -> V9)
│   ├── src/test/java/                     # 73 Unit & Integration Tests (11 test classes, 100% Passed)
│   ├── .env.example                       # Biến môi trường mẫu (Bảo mật: không commit .env)
│   └── pom.xml                            # Quản lý Maven dependencies
├── frontend/                              # Single Page Application (React 18 + Vite 5 + TypeScript)
│   ├── public/                            # Ảnh phòng tập thật & bộ môn Reformer, Mat, Tower
│   ├── src/                               # Components, Pages, Context, Libs, Types
│   ├── .env.example                       # Biến môi trường mẫu Frontend
│   └── package.json                       # Node dependencies
├── docs/                                  # Tài liệu SAD, đặc tả ca sử dụng, báo cáo Tuần 12
│   ├── N06_12_SAD_Quan_Ly_Phong_Tap_Pilates.docx # Báo cáo Word SAD hoàn chỉnh Tuần 12
│   ├── PROJECT_STRUCTURE.md               # Sơ đồ cấu trúc chi tiết Monorepo
│   ├── IMPLEMENTATION_PLAN.md             # Đặc tả 25 Use Case & Kế hoạch triển khai
│   ├── ACCOUNTS.md                        # Danh sách tài khoản kiểm thử chính thức
│   └── AGENTS.md                          # Ràng buộc nghiệp vụ và kiến trúc kỹ thuật
├── N06_12_SAD_Quan_Ly_Phong_Tap_Pilates/  # Thư mục chuẩn bị xuất nộp bài theo mã nhóm N06-12
├── N06_12_SAD_Quan_Ly_Phong_Tap_Pilates.zip # Gói zip master nộp Canvas theo mã nhóm N06-12 (~10.8 MB)
├── run-all.bat                            # Script 1-Click khởi động cả Backend và Frontend
├── run-backend.bat                        # Script khởi động Backend độc lập
├── run-frontend.bat                       # Script khởi động Frontend độc lập
├── ACCOUNTS.md                            # Danh sách chi tiết tài khoản kiểm thử
├── AGENTS.md                              # Ràng buộc nghiệp vụ và kiến trúc kỹ thuật
├── README.md                              # Hướng dẫn chi tiết dự án & Báo cáo tiến độ
└── .gitignore                             # Cấu hình loại trừ file nhạy cảm và build artifact
```

---

## 5. Hướng dẫn Khởi chạy Hệ thống

### Cách 1: Khởi chạy 1-Click (Khuyên dùng trên Windows)
Chỉ cần nhấp đúp chuột vào file:
```cmd
run-all.bat
```
Script sẽ tự động:
1. Mở cửa sổ Backend, tự động thiết lập Maven và khởi động Spring Boot trên cổng `8080`.
2. Mở cửa sổ Frontend và khởi chạy Vite dev server trên cổng `5173`.

---

### Cách 2: Khởi chạy thủ công từng dịch vụ

#### 1. Khởi chạy Backend (Port 8080):
```bash
cd backend
# Cấu hình biến môi trường nếu cần thiết (sao chép từ .env.example sang .env)
# Khởi động Spring Boot API Server
mvn spring-boot:run
```
* **API Base URL:** `http://localhost:8080/api/v1`
* **Swagger UI:** `http://localhost:8080/swagger-ui/index.html` (hoặc `/swagger-ui.html`)
* **OpenAPI Docs JSON:** `http://localhost:8080/v3/api-docs`

#### 2. Khởi chạy Frontend (Port 5173):
```bash
cd frontend
# Cài đặt thư viện dependencies (nếu chưa cài)
npm install
# Khởi chạy Vite dev server
npm run dev
```
* **Giao diện Web:** `http://localhost:5173`

---

## 6. Tài khoản Kiểm thử Đăng nhập

Hệ thống đã chuẩn bị sẵn tài khoản mẫu cho cả 3 vai trò với mật khẩu mặc định: `Password123!`

| Vai trò | Email đăng nhập | Mật khẩu | Quyền hạn & Chức năng chính |
| :--- | :--- | :--- | :--- |
| **ADMIN** (Quản trị viên) | `admin@pilates.com` | `Password123!` | Toàn quyền Dashboard doanh thu, xếp lịch buổi tập, duyệt thanh toán, quản lý gói, phòng, lớp học và phân quyền HLV. |
| **TRAINER** (Huấn luyện viên) | `trainer@pilates.com` | `Password123!` | Xem lịch dạy cá nhân, điểm danh học viên (Có mặt / Vắng mặt), theo dõi KPI và thống kê giảng dạy. |
| **MEMBER** (Học viên) | `nguyen12@gmail.com` | `MatKhauMoi123!` *(gốc: `Password123!`)* | Xem thời khóa biểu, đặt chỗ buổi tập, hủy buổi hoàn lượt (trước 2h), mua gói tập, xem lịch sử giao dịch. |

*(Xem thêm hướng dẫn chi tiết từng luồng tại [ACCOUNTS.md](file:///d:/quan-ly-pilates/ACCOUNTS.md))*

---

## 7. Tiến độ Triển khai 5 Nhóm Use Cases

Hệ thống đã hoàn thiện trọn vẹn **25/25 Use Cases** theo đúng tài liệu Đặc tả Phân tích Thiết kế (SAD):

### Nhóm UC1: Quản lý Tài khoản & Hồ sơ
- [x] **UC1.1:** Đăng ký hội viên công khai (luôn mặc định vai trò `MEMBER`).
- [x] **UC1.2:** Đăng nhập / Đăng xuất an toàn bằng JWT, chặn tài khoản `LOCKED` (403).
- [x] **UC1.3:** Xem và cập nhật hồ sơ cá nhân (chặn tự sửa Role hoặc Trạng thái).
- [x] **UC1.4:** Đổi mật khẩu cá nhân.
- [x] **UC1.5:** Admin quản lý người dùng, tạo tài khoản HLV (`TRAINER`), khóa/mở khóa tài khoản (chống Admin tự khóa chính mình).
- [x] **UC1.6:** Admin tra cứu, tìm kiếm và cập nhật chuyên môn, tiểu sử HLV & Hội viên.

### Nhóm UC2: Quản lý Lớp, Phòng & Lịch tập
- [x] **UC2.1:** Quản lý danh mục mẫu lớp Pilates (Classic, Dynamic Core, Flow, Tower...).
- [x] **UC2.2:** Quản lý danh sách phòng tập (Reformer, Mat) và sức chứa tối đa.
- [x] **UC2.3:** Admin xếp lịch buổi tập mới, kiểm tra chống trùng phòng và chống trùng lịch HLV.
- [x] **UC2.4:** Xem thời khóa biểu dạng lưới 7 ngày/tuần trực quan, có bộ lọc theo HLV, lớp và trạng thái.
- [x] **UC2.5:** Admin hủy buổi tập chưa diễn ra: Tự động hoàn lại 1 lượt tập cho tất cả học viên đã đặt chỗ về đúng gói tập đã trừ.

### Nhóm UC3: Quản lý Gói tập & Thanh toán
- [x] **UC3.1:** Quản lý danh mục các gói tập Pilates (Gói 10 buổi, 20 buổi, 36 buổi, VIP 60 buổi).
- [x] **UC3.2:** Hội viên chọn mua gói tập, tạo giao dịch thanh toán chờ duyệt (`UNPAID`).
- [x] **UC3.3:** Admin xác nhận thanh toán (`PAID`): Tự động kích hoạt gói mới hoặc gia hạn cộng dồn số buổi và cộng thêm ngày sử dụng nguyên tử trong 1 Transaction.
- [x] **UC3.4:** Cảnh báo hội viên khi gói tập sắp hết hạn ($\le 7$ ngày) hoặc sắp hết buổi ($\le 2$ lượt).
- [x] **UC3.5:** Theo dõi chi tiết lịch sử giao dịch và hóa đơn thanh toán của hội viên.

### Nhóm UC4: Đặt chỗ & Hủy lịch tập
- [x] **UC4.1:** Xem danh sách buổi tập đang mở (chỉ hiển thị buổi chưa bắt đầu, cho phép đăng ký).
- [x] **UC4.2:** Hội viên đặt chỗ buổi tập: Áp dụng **Khóa bi quan (Pessimistic Locking)** chống Race Condition khi nhiều người cùng đặt chỗ cuối, trừ 1 lượt nguyên tử.
- [x] **UC4.3:** Xem danh sách các buổi tập đã đặt chỗ của cá nhân Hội viên.
- [x] **UC4.4:** Hội viên hủy đặt chỗ: Áp dụng **Biên thời gian 2 giờ** (Hủy trước $\ge 2$ tiếng được hoàn trả 1 lượt về đúng gói; hủy trong vòng 2 tiếng sát giờ không hoàn lượt).
- [x] **UC4.5:** Xem thông báo xác nhận và cảnh báo trạng thái đặt/hủy chỗ trên giao diện người dùng.

### Nhóm UC5: Điểm danh & Báo cáo Thống kê
- [x] **UC5.1:** Xem danh sách học viên đã đặt chỗ của từng buổi tập (dành cho HLV phụ trách & Admin).
- [x] **UC5.2:** HLV / Admin điểm danh học viên tại buổi tập (Có mặt / Vắng mặt). Học viên vắng mặt không được hoàn lại buổi tập.
- [x] **UC5.3:** Học viên theo dõi lịch sử tập luyện và kết quả điểm danh cá nhân (`ATTENDED` / `ABSENT`).
- [x] **UC5.4:** Dashboard Báo cáo Doanh thu & Thống kê dành cho Quản trị viên (bộ lọc tháng/năm, doanh thu thực nhận từ đơn `PAID`, tỷ lệ lấp đầy lớp, tổng số hội viên active).

---

## 8. Kiểm thử Tự động (Automated Testing)

Toàn bộ nghiệp vụ quan trọng (bảo vệ phân quyền, khóa bi quan, biên 2 giờ hoàn lượt, kích hoạt gói nguyên tử, chặn điểm danh buổi hủy UC5.2, bảo mật production chặn mock login, bảo toàn dữ liệu Flyway V1..V9) đều có bài kiểm thử tự động:

```bash
cd backend
mvn test
# hoặc kiểm thử toàn diện và đóng gói:
mvn clean verify
```

**Kết quả kiểm thử thực tế:**
- **Tests run:** **73/73** (11 Test Classes)
- **Failures:** 0 | **Errors:** 0 | **Skipped:** 0
- **Trạng thái:** `BUILD SUCCESS` (100% Passed)
- **Frontend Build:** `npm run build` $\rightarrow$ `dist/` 100% PASS (Vite v5.4.21, 0 errors)

### Chi tiết 11 Test Classes (Nghiệp vụ, An toàn tương tranh & Bảo mật):
1. `AttendanceAndStatsTest` (7 tests): Điểm danh có mặt/vắng mặt, thống kê doanh thu tháng/năm, KPI giảng dạy HLV, chặn điểm danh buổi đã hủy.
2. `AuthControllerTest` (13 tests): Đăng ký Member, đổi mật khẩu, ẩn email/SĐT HLV ở API public (`/api/v1/auth/trainers`), xác thực JWT.
3. `BookingFlowTest` (9 tests): Đặt chỗ, biên 2h hoàn lượt, xem danh sách buổi tập và học viên.
4. `ConcurrencySafetyTest` (10 tests): Đặt chỗ trùng giờ đồng thời, hủy booking đồng thời, đặt buổi và hủy buổi đồng thời (Lost update phòng ngừa), Admin hủy buổi và Member hủy booking đồng thời (Deadlock prevention), chặn chuyển gói khác loại khi có booking dở dang, chặn tạo buổi tập vượt sức chứa phòng.
5. `DefaultProfileSecurityTest` (1 test): Mặc định bảo vệ không mở port nhạy cảm.
6. `FlywayMigrationTest` (6 tests): Kiểm tra tính hợp lệ và toàn vẹn của chuỗi di trú V1-V9.
7. `MembershipPaymentTest` (8 tests): Mua gói, xác nhận thanh toán nguyên tử, gia hạn gói EXHAUSTED cùng loại không tạo gói mới (chống 2 gói ACTIVE).
8. `MultiProfileSecurityTest` (1 test): Ưu tiên production tuyệt đối khi cấu hình đa profile.
9. `ProductionSecurityTest` (5 tests): Bắt buộc JWT xác minh tài khoản Member trên production, vô hiệu hóa mock login 100% trên production, kiểm tra độ dài JWT secret.
10. `SessionSchedulingTest` (6 tests): Xếp lịch buổi tập, kiểm tra sức chứa phòng, kiểm tra xung đột phòng và HLV.
11. `UserSecurityTest` (7 tests): Phân quyền RBAC, tài khoản bị khóa (`LOCKED`), cập nhật hồ sơ cá nhân.

---

## 9. Quy tắc Di trú Database (Flyway Migration Policy & History)

Hệ thống tuân thủ nghiêm ngặt các tiêu chuẩn quản trị cơ sở dữ liệu:
1. **Chính sách Forward-Only (V1 đến V9):** Tuyệt đối không xóa dữ liệu (`DELETE FROM bookings/sessions`, `TRUNCATE` hay `DROP TABLE`).
2. **Tách biệt Dữ liệu Mẫu (Demo Data):** Migration production (`db/migration/`) chỉ quản trị Schema DDL, danh mục chuẩn và chỉ mục tối ưu hiệu năng. Toàn bộ dữ liệu lịch tập demo phục vụ phát triển được tách riêng vào `backend/src/main/resources/db/demo/demo_data.sql`.
3. **Quản lý Checksum (`flyway_schema_history`):** Cấu hình `spring.flyway.validate-on-migrate: true` kiểm tra tính toàn vẹn:
   - **V1 đến V5:** Khởi tạo hoàn tất toàn bộ cấu trúc cốt lõi (Users, Profiles, Rooms, Classes, Sessions, Packages, Bookings).
   - **V6 (`reschedule sessions one per day`):** Checksum `837041814` (đồng bộ hoàn toàn với file trên disk, bảo toàn toàn vẹn 100% dữ liệu phòng tập).
   - **V7 (`prevent data loss and safe schedule`):** Checksum `1805188313` (kích hoạt chính sách Forward-Only và bảng nhật ký kiểm toán `system_audit_logs`).
   - **V8 (`enable row level security`):** Checksum `873088346` (kích hoạt Row-Level Security trên toàn bộ 11 bảng trong schema `public`, chặn nguy cơ rò rỉ dữ liệu qua Supabase PostgREST Data API).
   - **V9 (`prevent concurrent session overlap`):** Bổ sung ràng buộc và chỉ mục chống xếp trùng lịch phòng/HLV đồng thời ở tầng CSDL PostgreSQL bằng `btree_gist` và exclusion constraints.
4. **Bảo mật JWT Secret:** `application.yml` không chứa secret mặc định. Ứng dụng bắt buộc cấu hình `SUPABASE_JWT_SECRET` qua biến môi trường (tối thiểu 32 ký tự).

---

## 10. Tài liệu Tham khảo

- [1] **Báo cáo dự án:** [`N06_12_SAD_Quan_Ly_Phong_Tap_Pilates.docx`](file:///d:/quan-ly-pilates/docs/N06_12_SAD_Quan_Ly_Phong_Tap_Pilates.docx)
- [2] **Đặc tả 25 Use Cases & Kế hoạch Kỹ thuật:** [`docs/IMPLEMENTATION_PLAN.md`](file:///d:/quan-ly-pilates/docs/IMPLEMENTATION_PLAN.md)
- [3] **Sơ đồ Cấu trúc Dự án Thực tế:** [`docs/PROJECT_STRUCTURE.md`](file:///d:/quan-ly-pilates/docs/PROJECT_STRUCTURE.md)
- [4] **Danh sách Tài khoản Kiểm thử Đăng nhập:** [`docs/ACCOUNTS.md`](file:///d:/quan-ly-pilates/docs/ACCOUNTS.md)
- [5] **Tài liệu Tham khảo Công nghệ:** Spring Boot 3.3 Reference, React Quick Start, PostgreSQL 16 Docs.
- [6] **Mẫu Báo cáo Môn học:** `SAD_ReportTemplate_1_2026`
- [7] **Tài liệu Hướng dẫn Bài tập Thực hành:** `IBM_ex_workbook_new`
- [8] **Giáo trình Phân tích Thiết kế Hệ thống (SAD):** `Alan Dennis, Barbara Wixom, David Tegarden - Systems Analysis and Design_ An Object-Oriented Approach with UML-Wiley (2020).pdf`
- [9] **Sách Tham khảo Mẫu Thiết kế (Design Patterns):** `[O'Reilly. Head First] - Head First Design Patterns 2nd Edition - [Freeman].pdf`

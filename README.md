# Hệ thống Quản lý Phòng tập Pilates (Glow Pilates - N06-12)

Dự án bài tập lớn môn **Phân tích và Thiết kế Phần mềm (SAD)** – Đại học Phenikaa (Báo cáo Tuần 12 hoàn thiện).  
Hệ thống quản lý phòng tập Pilates chuyên nghiệp, ứng dụng kiến trúc phân tầng chuẩn Enterprise, tối ưu trải nghiệm người dùng với phong cách thiết kế Soft-Luxury.

---

## 1. Điểm nổi bật & Công nghệ sử dụng (Tech Stack)

### Backend (REST API):
- **Ngôn ngữ & Framework:** Java 17, Spring Boot 3.3.4.
- **Bảo mật & Phân quyền:** Spring Security 6, JJWT (HMAC SHA-256), Supabase Auth.
- **Cơ sở dữ liệu & ORM:** PostgreSQL (Supabase Session Pooler), Spring Data JPA, Hibernate.
- **Quản lý phiên bản DB:** Flyway Migration (V1 đến V9 chuẩn Forward-Only, bảo toàn dữ liệu, chống xung đột lịch trùng).
- **Tài liệu API:** Springdoc OpenAPI 3.0 / Swagger UI (56 endpoints).
- **Kiểm thử tự động:** JUnit 5, Mockito, AssertJ (Đạt **73/73** test case Unit & Integration Test qua 11 Test Classes, 100% Passed).

### Frontend (Single Page Application):
- **Core:** React 18, TypeScript, Vite 5.
- **Kiến trúc giao diện:** Glow Pilates Custom Design System (Soft-Luxury Aesthetic, Warm Beige & Dusty Rose Palette).
- **Icons & Visual:** Lucide React (100% Vector SVG, không dùng emoji thô).
- **Điều hướng & Bảo vệ tuyến đường:** React Router DOM v6, Role-based `ProtectedRoute`.
- **Quản lý API:** Axios Client với Interceptor tự động gắn Bearer Token và xử lý lỗi tập trung.

---

## 2. Cấu trúc thư mục Monorepo

```
quan-ly-pilates/
├── backend/                       # Source code Spring Boot REST API
│   ├── src/main/java/             # Controller, Service, Repository, Entity, DTO, Security
│   ├── src/main/resources/        # application.yml, db/migration (Flyway V1 -> V9)
│   ├── src/test/java/             # 73 Unit & Integration Tests (11 test classes, 100% Passed)
│   ├── .env.example               # Biến môi trường mẫu (Bảo mật: không commit .env)
│   └── pom.xml                    # Maven dependencies
├── frontend/                      # Source code React Vite TypeScript
│   ├── public/                    # Ảnh thực tế phòng tập & bộ môn Pilates
│   ├── src/
│   │   ├── components/            # Navbar (Dropdown phân cấp), ProtectedRoute
│   │   ├── pages/                 # HomePage, SchedulePage, Auth, Member, Admin, Trainer
│   │   ├── lib/                   # apiClient, formatters tiếng Việt
│   │   └── types/                 # TypeScript interfaces & types
│   ├── .env.example               # Biến môi trường mẫu Frontend
│   └── package.json               # Node dependencies
├── docs/                          # Tài liệu SAD, đặc tả ca sử dụng, báo cáo Tuần 12
│   ├── N06_12_SAD_Quan_Ly_Phong_Tap_Pilates.docx # File Word báo cáo Tuần 12 chính thức
│   ├── PROJECT_STRUCTURE.md       # Cấu trúc chi tiết Monorepo
│   ├── IMPLEMENTATION_PLAN.md     # Kế hoạch và đặc tả 25 Use Case
│   ├── ACCOUNTS.md                # Danh sách tài khoản kiểm thử
│   └── AGENTS.md                  # Quy tắc nghiệp vụ và ràng buộc hệ thống
├── export_for_report/             # Gói xuất nộp báo cáo hoàn chỉnh (code sạch + log test)
├── N06_12_SAD_Quan_Ly_Phong_Tap_Pilates.zip # Gói nén nộp Canvas trọn gói (hoặc export_for_report.zip)
├── run-all.bat                    # Script khởi chạy 1-Click cả Backend và Frontend
├── run-backend.bat                # Script chạy riêng Backend (Port 8080)
├── run-frontend.bat               # Script chạy riêng Frontend (Port 5173)
├── ACCOUNTS.md                    # Danh sách chi tiết tài khoản kiểm thử
├── AGENTS.md                      # Đặc tả kỹ thuật và ràng buộc nghiệp vụ
└── .gitignore                     # Cấu hình bỏ qua file nhạy cảm và build artifact
```

---

## 3. Hướng dẫn Khởi chạy Hệ thống

### Cách 1: Khởi chạy 1-Click (Khuyên dùng trên Windows)
Chỉ cần nhấp đúp chuột vào file:
```cmd
run-all.bat
```
Script sẽ tự động:
1. Mở cửa sổ Backend, cấu hình đường dẫn Maven và khởi động Spring Boot trên cổng `8080`.
2. Mở cửa sổ Frontend và khởi chạy Vite dev server trên cổng `5173`.

---

### Cách 2: Khởi chạy thủ công bằng dòng lệnh

#### 1. Khởi chạy Backend (Port 8080):
```bash
cd backend
mvn spring-boot:run
```
* **API Base URL:** `http://localhost:8080/api/v1`
* **Swagger UI:** `http://localhost:8080/swagger-ui.html`

#### 2. Khởi chạy Frontend (Port 5173):
```bash
cd frontend
npm install
npm run dev
```
* **Giao diện Web:** `http://localhost:5173`

---

## 4. Tài khoản Kiểm thử Đăng nhập (Test Accounts)

Hệ thống đã chuẩn bị sẵn tài khoản mẫu cho cả 3 vai trò với mật khẩu mặc định: `Password123!`

| Vai trò | Email đăng nhập | Mật khẩu | Quyền hạn & Chức năng chính |
| :--- | :--- | :--- | :--- |
| **ADMIN** (Quản trị viên) | `admin@pilates.com` | `Password123!` | Toàn quyền Dashboard doanh thu, xếp lịch buổi tập, duyệt thanh toán, quản lý gói, phòng, lớp học và phân quyền HLV. |
| **TRAINER** (Huấn luyện viên) | `trainer@pilates.com` | `Password123!` | Xem lịch dạy cá nhân, điểm danh học viên (Có mặt / Vắng mặt), theo dõi KPI và thống kê giảng dạy. |
| **MEMBER** (Học viên) | `nguyen12@gmail.com` | `MatKhauMoi123!` *(gốc: `Password123!`)* | Xem thời khóa biểu, đặt chỗ buổi tập, hủy buổi hoàn lượt (trước 2h), mua gói tập, xem lịch sử giao dịch. |

*(Xem thêm hướng dẫn chi tiết từng luồng tại [ACCOUNTS.md](file:///d:/quan-ly-pilates/ACCOUNTS.md))*

---

## 5. Tiến độ Triển khai 5 Nhóm Ca Sử Dụng (Hoàn thành 100%)

Hệ thống đã hoàn thiện trọn vẹn **25/25 Use Cases** theo đúng tài liệu Đặc tả Phân tích Thiết kế (SAD):

###  Nhóm UC1: Quản lý Tài khoản & Hồ sơ (100%)
- [x] **UC1.1:** Đăng ký hội viên công khai (luôn mặc định vai trò `MEMBER`).
- [x] **UC1.2:** Đăng nhập / Đăng xuất an toàn bằng JWT, chặn tài khoản `LOCKED` (403).
- [x] **UC1.3:** Xem và cập nhật hồ sơ cá nhân (chặn tự sửa Role hoặc Trạng thái).
- [x] **UC1.4:** Đổi mật khẩu cá nhân.
- [x] **UC1.5:** Admin quản lý người dùng, tạo tài khoản HLV (`TRAINER`), khóa/mở khóa tài khoản (chống Admin tự khóa chính mình).
- [x] **UC1.6:** Admin tra cứu, tìm kiếm và cập nhật chuyên môn, tiểu sử HLV & Hội viên.

###  Nhóm UC2: Quản lý Lớp, Phòng & Lịch tập (100%)
- [x] **UC2.1:** Quản lý danh mục mẫu lớp Pilates (Classic, Dynamic Core, Flow, Tower...).
- [x] **UC2.2:** Quản lý danh sách phòng tập (Reformer, Mat) và sức chứa tối đa.
- [x] **UC2.3:** Admin xếp lịch buổi tập mới, kiểm tra chống trùng phòng và chống trùng lịch HLV.
- [x] **UC2.4:** Xem thời khóa biểu dạng lưới 7 ngày/tuần trực quan, có bộ lọc theo HLV, lớp và trạng thái.
- [x] **UC2.5:** Admin hủy buổi tập chưa diễn ra: Tự động hoàn lại 1 lượt tập cho tất cả học viên đã đặt chỗ về đúng gói tập đã trừ.

###  Nhóm UC3: Quản lý Gói tập & Thanh toán (100%)
- [x] **UC3.1:** Quản lý danh mục các gói tập Pilates (Gói 10 buổi, 20 buổi, 36 buổi, VIP 60 buổi).
- [x] **UC3.2:** Hội viên chọn mua gói tập, tạo giao dịch thanh toán chờ duyệt (`UNPAID`).
- [x] **UC3.3:** Admin xác nhận thanh toán (`PAID`): Tự động kích hoạt gói mới hoặc **gia hạn cộng dồn số buổi và cộng thêm ngày sử dụng** nguyên tử trong 1 Transaction.
- [x] **UC3.4:** Cảnh báo hội viên khi gói tập sắp hết hạn ($\le 7$ ngày) hoặc sắp hết buổi ($\le 2$ lượt).
- [x] **UC3.5:** Theo dõi chi tiết lịch sử giao dịch và hóa đơn thanh toán của hội viên.

###  Nhóm UC4: Đặt chỗ & Hủy lịch tập (100%)
- [x] **UC4.1:** Hội viên đặt chỗ buổi tập: Áp dụng **Khóa bi quan (Pessimistic Locking)** chống Race Condition khi nhiều người cùng đặt chỗ cuối, trừ 1 lượt nguyên tử.
- [x] **UC4.2:** Hội viên hủy đặt chỗ: Áp dụng **Biên thời gian 2 giờ** (Hủy trước $\ge 2$ tiếng được hoàn trả 1 lượt về đúng gói; hủy trong vòng 2 tiếng sát giờ không hoàn lượt).
- [x] **UC4.3:** Trainer và Admin theo dõi danh sách học viên đã giữ chỗ của từng buổi tập.

###  Nhóm UC5: Điểm danh & Báo cáo Thống kê (100%)
- [x] **UC5.1:** HLV điểm danh học viên tại buổi tập (Có mặt / Vắng mặt). Học viên vắng mặt không được hoàn lại buổi tập.
- [x] **UC5.2:** Báo cáo Thống kê KPI Giảng dạy dành cho Huấn luyện viên (tổng số buổi đứng lớp, tỷ lệ chuyên cần của học viên).
- [x] **UC5.3:** Dashboard Báo cáo Doanh thu & Vận hành dành cho Quản trị viên (tính doanh thu thực nhận từ đơn `PAID`, tỷ lệ lấp đầy lớp, tổng số hội viên active).

---

## 6. Chạy Kiểm thử Tự động (Automated Testing)

Toàn bộ nghiệp vụ quan trọng (bảo vệ phân quyền, khóa bi quan, biên 2 giờ hoàn lượt, kích hoạt gói nguyên tử, chặn điểm danh buổi hủy UC5.2, bảo mật production chặn mock login, bảo toàn dữ liệu Flyway V1..V7) đều có bài kiểm thử tự động:

```bash
cd backend
mvn test
# hoặc kiểm thử toàn diện và đóng gói:
mvn clean verify
```

**Kết quả kiểm thử:**
- **Tests run:** 73 (11 Test Classes)
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0
- **Trạng thái:** `BUILD SUCCESS` (100% Passed)
- **Frontend Build:** `npm run build` -> `dist/` 100% PASS (Vite v5.4.21, 0 errors)

### Các bài kiểm thử nghiệp vụ và an toàn tương tranh (Concurrency & Security):
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

## 7. Quy tắc Di trú Database (Flyway Migration Policy & History)

Hệ thống tuân thủ nghiêm ngặt các tiêu chuẩn quản trị cơ sở dữ liệu:
1. **Chính sách Forward-Only (V1 đến V9):** Tuyệt đối không xóa dữ liệu (`DELETE FROM bookings/sessions`, `TRUNCATE` hay `DROP TABLE`).
2. **Tách biệt Dữ liệu Mẫu (Demo Data):** Migration production (`db/migration/`) chỉ quản trị Schema DDL, danh mục chuẩn và chỉ mục tối ưu hiệu năng. Toàn bộ dữ liệu lịch tập demo phục vụ phát triển được tách riêng vào `backend/src/main/resources/db/demo/demo_data.sql`.
3. **Xử lý Checksum Lịch sử (`flyway_schema_history`):** Hệ thống cấu hình chuẩn `spring.flyway.validate-on-migrate: true` trong `application.yml` để Flyway luôn kiểm tra tính toàn vẹn checksum. Kết quả kiểm tra và di trú thực tế trên cơ sở dữ liệu xác nhận:
   * **V1 đến V5:** Đã khởi tạo hoàn tất toàn bộ cấu trúc cốt lõi (Users, Profiles, Rooms, Classes, Sessions, Packages, Bookings).
   * **V6 (`reschedule sessions one per day`):** Checksum `837041814` (đồng bộ hoàn toàn với file trên disk, bảo toàn toàn vẹn 100% dữ liệu phòng tập, loại bỏ toàn bộ lệnh xóa dữ liệu và tách dữ liệu demo sang file riêng).
   * **V7 (`prevent data loss and safe schedule`):** Checksum `1805188313` (kích hoạt chính sách Forward-Only và bảng nhật ký kiểm toán `system_audit_logs`).
   * **V8 (`enable row level security`):** Checksum `873088346` (kích hoạt Row-Level Security trên toàn bộ 11 bảng trong schema `public`, chặn 100% nguy cơ rò rỉ dữ liệu qua Supabase PostgREST Data API theo đúng khuyến cáo bảo mật của Supabase).
   * **V9 (`prevent concurrent session overlap`):** Bổ sung ràng buộc và chỉ mục chống xếp trùng lịch phòng/HLV đồng thời ở tầng CSDL PostgreSQL.
   * **Toàn bộ 9 migrations (V1 đến V9)** đều ở trạng thái `success: true`, cơ sở dữ liệu bảo toàn trọn vẹn 28 buổi tập (sessions), 6 lượt đặt chỗ (bookings), 4 gói tập và tài khoản người dùng mà không có bất kỳ mất mát dữ liệu nào.
4. **Bảo mật JWT Secret:** `application.yml` không chứa secret mặc định. Ứng dụng bắt buộc cấu hình `SUPABASE_JWT_SECRET` qua biến môi trường (tối thiểu 32 ký tự) và từ chối khởi động nếu thiếu hoặc dùng secret mặc định trong production (được bảo vệ bởi bài test trong `ProductionSecurityTest`).

---

## 7. Lưu ý Bảo mật khi Đưa lên GitHub

Dự án đã được cấu hình sẵn file `.gitignore` ở thư mục gốc. Trước khi push lên GitHub repository, cần lưu ý:
1. **Tuyệt đối không commit file `.env`**: File `backend/.env` chứa chuỗi kết nối trực tiếp đến database Supabase và JWT Secret. Luôn dùng `.env.example` làm mẫu cấu hình.
2. **Không commit thư mục build**: `backend/target/` và `frontend/dist/`, `frontend/node_modules/` đã được chặn tự động bởi `.gitignore`.
3. **Các tài liệu Word/PDF dung lượng lớn**: Đã được thêm vào `.gitignore` để tránh làm nặng repository.

---

## 8. Tác giả & Đơn vị thực hiện
- **Đề tài:** Hệ thống Quản lý phòng tập Pilates (Glow Pilates Studio).
- **Nhóm thực hiện:** Nhóm N06–12.
- **Học phần:** Phân tích & Thiết kế Phần mềm (SAD) – Đại học Phenikaa.

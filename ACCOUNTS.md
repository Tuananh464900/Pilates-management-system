# DANH SÁCH TÀI KHOẢN HỆ THỐNG GLOW PILATES (N06-12)

Tài liệu này lưu trữ thông tin danh sách các tài khoản kiểm thử chính thức cho 3 vai trò (**ADMIN**, **TRAINER**, **MEMBER**) trong hệ thống quản lý phòng tập Pilates, kèm theo quyền hạn, phạm vi nghiệp vụ và hướng dẫn sử dụng.

---

##  Thông tin đăng nhập nhanh

```
┌──────────┬───────────────────────┬────────────────┬───────────────────────────────┐
│ VAI TRÒ  │ EMAIL ĐĂNG NHẬP       │ MẬT KHẨU       │ HỌ TÊN & GHI CHÚ              │
├──────────┼───────────────────────┼────────────────┼───────────────────────────────┤
│ ADMIN    │ admin@pilates.com     │ Password123!   │ Quản Trị Viên                 │
│ TRAINER  │ trainer@pilates.com   │ Password123!   │ HLV Mai Lan                   │
│ MEMBER   │ nguyen12@gmail.com    │ MatKhauMoi123! │ Học viên Nguyên               │
└──────────┴───────────────────────┴────────────────┴───────────────────────────────┘
```

| Vai trò | Email đăng nhập | Mật khẩu hiện tại | Ghi chú |
| :--- | :--- | :--- | :--- |
|  **ADMIN** (Quản trị viên) | `admin@pilates.com` | `Password123!` | Mật khẩu mặc định |
|  **TRAINER** (Huấn luyện viên) | `trainer@pilates.com` | `Password123!` | Mật khẩu mặc định |
|  **MEMBER** (Học viên) | `nguyen12@gmail.com` | `MatKhauMoi123!` | Đã đổi thành công khi test UC1.4 *(Mật khẩu ban đầu: `Password123!`)* |

>  **Mẹo đăng nhập:** Bạn có thể đăng nhập tại: [http://localhost:5173/login](http://localhost:5173/login). Sau khi đăng nhập, hệ thống sẽ tự động chuyển hướng và hiển thị menu điều hướng tương ứng với vai trò của bạn.

---

## 1. Chi tiết tài khoản Quản trị viên (ADMIN)

* **Email:** `admin@pilates.com`
* **Mật khẩu:** `Password123!`
* **Vai trò:** `ADMIN`
* **Quyền hạn & Các màn hình quản trị:**
  1. **Dashboard & Thống kê ([/admin/dashboard](http://localhost:5173/admin/dashboard)):**
     * Xem tổng doanh thu thực tế từ các đơn hàng đã thanh toán (`PAID`).
     * Thống kê tỷ lệ lấp đầy phòng tập, số lượt đặt chỗ, tỷ lệ tham gia lớp.
     * Bảng xếp hạng KPI huấn luyện viên theo số giờ dạy thực tế.
  2. **Quản lý buổi tập ([/admin/sessions](http://localhost:5173/admin/sessions)):**
     * Lên lịch buổi tập mới (chọn lớp, huấn luyện viên, phòng tập, thời gian).
     * Kiểm tra chống trùng giờ, trùng phòng tập và trùng lịch của HLV.
     * Hủy buổi tập chưa diễn ra: Tự động hoàn lại 1 lượt cho tất cả học viên đã đặt chỗ về đúng gói tập đã trừ.
  3. **Quản lý doanh thu & Giao dịch ([/admin/payments](http://localhost:5173/admin/payments)):**
     * Danh sách tất cả giao dịch thanh toán trong hệ thống (`UNPAID`, `PAID`, `CANCELLED`).
     * Xác nhận thanh toán (`Xác nhận PAID`): Kích hoạt gói tập mới hoặc gia hạn gói tập cho học viên nguyên tử trong 1 transaction.
  4. **Quản lý gói tập ([/admin/packages](http://localhost:5173/admin/packages)):**
     * Tạo và chỉnh sửa danh mục gói tập (Gói Tiêu chuẩn, Nâng cao, VIP, v.v.).
  5. **Quản lý lớp Pilates ([/admin/classes](http://localhost:5173/admin/classes)):**
     * Danh mục các mẫu lớp tập Pilates (Mat, Reformer, Tower, Cadillac...), cấp độ và sức chứa mặc định.
  6. **Quản lý phòng tập ([/admin/rooms](http://localhost:5173/admin/rooms)):**
     * Danh sách phòng tập luyện, trang thiết bị máy móc và sức chứa tối đa.
  7. **Quản lý tài khoản & HLV ([/admin/users](http://localhost:5173/admin/users)):**
     * Xem danh sách tất cả tài khoản người dùng trong hệ thống.
     * Tạo tài khoản Huấn luyện viên mới (chỉ Admin mới có quyền tạo HLV).
     * Khóa (`LOCKED`) hoặc Mở khóa (`ACTIVE`) tài khoản người dùng (chặn tài khoản bị khóa không được thao tác).

---

## 2. Chi tiết tài khoản Huấn luyện viên (TRAINER)

* **Email:** `trainer@pilates.com`
* **Mật khẩu:** `Password123!`
* **Vai trò:** `TRAINER`
* **Họ và tên:** HLV Mai Lan
* **Số điện thoại:** `0912345678`
* **Chuyên môn:** Pilates Reformer, Cadillac & Mat Classic (Chứng chỉ quốc tế PMA)
* **Quyền hạn & Màn hình làm việc:**
  1. **Thời khóa biểu giảng dạy ([/schedule](http://localhost:5173/schedule)):**
     * Xem lịch dạy các buổi tập do chính mình phụ trách theo tuần.
     * Bấm vào từng buổi tập để xem danh sách học viên đã đăng ký chỗ.
  2. **Điểm danh học viên (UC5.1):**
     * HLV chỉ được điểm danh những buổi tập do chính mình phụ trách và khi buổi tập đã bắt đầu.
     * Đánh dấu học viên **Có mặt** (`ATTENDED`) hoặc **Vắng mặt** (`ABSENT`).
     * Quy tắc nghiệp vụ: Học viên vắng mặt không được hoàn lại lượt tập.

---

## 3. Chi tiết tài khoản Hội viên (MEMBER)

* **Email:** `nguyen12@gmail.com`
* **Mật khẩu hiện tại:** `MatKhauMoi123!` *(Mật khẩu ban đầu là `Password123!`, đã đổi thành công trong bài test UC1.4)*
* **Vai trò:** `MEMBER`
* **Họ và tên:** Học viên Nguyên
* **Số điện thoại:** `0987654321`
* **Tình trạng gói tập hiện tại:**
  * **Tên gói:** Gói Tiêu Chuẩn (20 buổi)
  * **Số buổi còn lại:** `20 / 20` buổi
  * **Trạng thái:** `ACTIVE` (Đang hoạt động)
  * **Thời hạn gói:** Đến ngày `16/12/2026`
* **Quyền hạn & Màn hình học viên:**
  1. **Thời khóa biểu & Đặt chỗ ([/schedule](http://localhost:5173/schedule)):**
     * Xem lịch tập tất cả các lớp trong tuần.
     * Đặt chỗ buổi tập: Kiểm tra điều kiện gói còn hiệu lực, còn lượt, không trùng giờ và còn chỗ trống. Đặt thành công tự động trừ 1 lượt tập.
  2. **Quản lý lịch đã đặt ([/my-bookings](http://localhost:5173/my-bookings)):**
     * Xem danh sách các buổi tập đã đăng ký.
     * Hủy đặt chỗ:
       * Hủy trước giờ bắt đầu **từ 2 tiếng trở lên**: Được hoàn trả lại 1 lượt tập vào gói.
       * Hủy trong vòng **dưới 2 tiếng trước giờ bắt đầu**: Không được hoàn lượt.
       * Sau khi buổi tập đã bắt đầu: Không thể hủy.
  3. **Gói tập của tôi ([/my-membership](http://localhost:5173/my-membership)):**
     * Theo dõi số buổi còn lại, hạn sử dụng và cảnh báo sắp hết hạn (khi còn <= 7 ngày hoặc <= 2 lượt).
  4. **Lịch sử thanh toán ([/my-payments](http://localhost:5173/my-payments)):**
     * Xem danh sách các đơn hàng đã tạo, mã giao dịch, số tiền và trạng thái thanh toán.
  5. **Mua & Gia hạn gói tập ([/buy-package](http://localhost:5173/buy-package)):**
     * Chọn mua gói tập mới hoặc gia hạn gói khi sắp hết.

---

## 4. Cách tạo thêm tài khoản mới khi cần

* **Tạo thêm Học viên (Member):**
  * Truy cập trang Đăng ký: [http://localhost:5173/register](http://localhost:5173/register)
  * Nhập họ tên, email, số điện thoại, mật khẩu.
  * Tài khoản được tạo tự động mang vai trò `MEMBER`.
* **Tạo thêm Huấn luyện viên (Trainer):**
  * Đăng nhập tài khoản Admin (`admin@pilates.com`).
  * Vào mục **Tài khoản** ([/admin/users](http://localhost:5173/admin/users)) -> Bấm nút **"+ Thêm Huấn luyện viên"**.
  * Nhập thông tin HLV và chuyên môn để cấp tài khoản.

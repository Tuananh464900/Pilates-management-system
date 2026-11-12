package vn.n06g12.pilates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.n06g12.pilates.attendance.dto.AttendanceItemRequest;
import vn.n06g12.pilates.attendance.dto.MarkAttendanceRequest;
import vn.n06g12.pilates.booking.entity.Booking;
import vn.n06g12.pilates.booking.entity.BookingStatus;
import vn.n06g12.pilates.booking.repository.BookingRepository;
import vn.n06g12.pilates.membership.entity.*;
import vn.n06g12.pilates.membership.repository.MemberMembershipRepository;
import vn.n06g12.pilates.membership.repository.MembershipPackageRepository;
import vn.n06g12.pilates.membership.repository.PaymentRepository;
import vn.n06g12.pilates.pilatesclass.entity.ClassLevel;
import vn.n06g12.pilates.pilatesclass.entity.ClassStatus;
import vn.n06g12.pilates.pilatesclass.entity.PilatesClass;
import vn.n06g12.pilates.pilatesclass.repository.PilatesClassRepository;
import vn.n06g12.pilates.room.entity.Room;
import vn.n06g12.pilates.room.entity.RoomStatus;
import vn.n06g12.pilates.room.repository.RoomRepository;
import vn.n06g12.pilates.security.JwtTokenValidator;
import vn.n06g12.pilates.session.entity.Session;
import vn.n06g12.pilates.session.entity.SessionStatus;
import vn.n06g12.pilates.session.repository.SessionRepository;
import vn.n06g12.pilates.user.entity.Role;
import vn.n06g12.pilates.user.entity.User;
import vn.n06g12.pilates.user.entity.UserStatus;
import vn.n06g12.pilates.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AttendanceAndStatsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MemberMembershipRepository membershipRepository;

    @Autowired
    private MembershipPackageRepository packageRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PilatesClassRepository classRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    private User adminUser;
    private User trainer1;
    private User trainer2;
    private User member1;
    private User member2;

    private String adminToken;
    private String trainer1Token;
    private String trainer2Token;
    private String member1Token;

    private PilatesClass pilatesClass;
    private Room room;
    private MembershipPackage membershipPackage;

    @BeforeEach
    void setUp() {
        // Thứ tự xóa sạch dữ liệu tránh xung đột khóa ngoại
        bookingRepository.deleteAll();
        paymentRepository.deleteAll();
        membershipRepository.deleteAll();
        sessionRepository.deleteAll();
        classRepository.deleteAll();
        roomRepository.deleteAll();
        packageRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Khởi tạo người dùng
        adminUser = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("admin.stats@pilates.vn")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        trainer1 = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("trainer1.stats@pilates.vn")
                .role(Role.TRAINER)
                .status(UserStatus.ACTIVE)
                .build());

        trainer2 = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("trainer2.stats@pilates.vn")
                .role(Role.TRAINER)
                .status(UserStatus.ACTIVE)
                .build());

        member1 = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("member1.stats@pilates.vn")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .build());

        member2 = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("member2.stats@pilates.vn")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .build());

        adminToken = jwtTokenValidator.generateTokenForTest(adminUser.getId(), adminUser.getEmail(), 3600000L);
        trainer1Token = jwtTokenValidator.generateTokenForTest(trainer1.getId(), trainer1.getEmail(), 3600000L);
        trainer2Token = jwtTokenValidator.generateTokenForTest(trainer2.getId(), trainer2.getEmail(), 3600000L);
        member1Token = jwtTokenValidator.generateTokenForTest(member1.getId(), member1.getEmail(), 3600000L);

        // 2. Tạo phòng & lớp học
        room = roomRepository.save(Room.builder()
                .name("Phòng Điểm Danh")
                .capacity(10)
                .status(RoomStatus.ACTIVE)
                .build());

        pilatesClass = classRepository.save(PilatesClass.builder()
                .name("Pilates Reformer Stats")
                .description("Lớp kiểm thử điểm danh và thống kê")
                .level(ClassLevel.INTERMEDIATE)
                .defaultCapacity(10)
                .status(ClassStatus.ACTIVE)
                .build());

        // 3. Tạo gói tập
        membershipPackage = packageRepository.save(MembershipPackage.builder()
                .name("Gói 10 Buổi Điểm Danh")
                .description("Gói phục vụ test UC5")
                .totalSessions(10)
                .durationDays(30)
                .price(BigDecimal.valueOf(1500000))
                .status(PackageStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("UC5.1 - Điểm danh thành công: Trainer phụ trách điểm danh Có mặt và Vắng mặt khi buổi tập đã bắt đầu")
    void testTrainerMarksAttendanceSuccess() throws Exception {
        Instant pastStartTime = Instant.now().minus(30, ChronoUnit.MINUTES);
        Instant pastEndTime = Instant.now().plus(30, ChronoUnit.MINUTES);

        Session session = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .trainer(trainer1)
                .room(room)
                .startTime(pastStartTime)
                .endTime(pastEndTime)
                .capacity(10)
                .bookedCount(2)
                .status(SessionStatus.SCHEDULED)
                .build());

        MemberMembership mem1 = membershipRepository.save(MemberMembership.builder()
                .member(member1)
                .membershipPackage(membershipPackage)
                .remainingSessions(9)
                .startDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(29, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build());

        MemberMembership mem2 = membershipRepository.save(MemberMembership.builder()
                .member(member2)
                .membershipPackage(membershipPackage)
                .remainingSessions(9)
                .startDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(29, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build());

        Booking b1 = bookingRepository.save(Booking.builder()
                .session(session)
                .member(member1)
                .membership(mem1)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build());

        Booking b2 = bookingRepository.save(Booking.builder()
                .session(session)
                .member(member2)
                .membership(mem2)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build());

        // Trainer1 điểm danh: b1 có mặt, b2 vắng mặt, kèm hoàn tất buổi tập
        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .completeSession(true)
                .items(List.of(
                        AttendanceItemRequest.builder().bookingId(b1.getId()).status(BookingStatus.ATTENDED).build(),
                        AttendanceItemRequest.builder().bookingId(b2.getId()).status(BookingStatus.ABSENT).build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/attendance/sessions/" + session.getId())
                        .header("Authorization", "Bearer " + trainer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.length()", is(2)));

        // Kiểm tra cơ sở dữ liệu
        Booking updatedB1 = bookingRepository.findById(b1.getId()).orElseThrow();
        assertEquals(BookingStatus.ATTENDED, updatedB1.getStatus());
        assertFalse(updatedB1.getRefunded());

        Booking updatedB2 = bookingRepository.findById(b2.getId()).orElseThrow();
        assertEquals(BookingStatus.ABSENT, updatedB2.getStatus());
        assertFalse(updatedB2.getRefunded(), "Vắng mặt tuyệt đối không được hoàn lượt");

        // Kiểm tra gói tập của Member2: tuyệt đối KHÔNG được hoàn lượt (vẫn là 9)
        MemberMembership refreshedMem2 = membershipRepository.findById(mem2.getId()).orElseThrow();
        assertEquals(9, refreshedMem2.getRemainingSessions());

        // Buổi tập đã chuyển sang COMPLETED
        Session refreshedSession = sessionRepository.findById(session.getId()).orElseThrow();
        assertEquals(SessionStatus.COMPLETED, refreshedSession.getStatus());
    }

    @Test
    @DisplayName("UC5.1 - Chặn điểm danh trước giờ tập: Buổi tập chưa bắt đầu thì không được điểm danh")
    void testBlockAttendanceBeforeSessionStarts() throws Exception {
        Instant futureStartTime = Instant.now().plus(2, ChronoUnit.HOURS);
        Instant futureEndTime = Instant.now().plus(3, ChronoUnit.HOURS);

        Session session = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .trainer(trainer1)
                .room(room)
                .startTime(futureStartTime)
                .endTime(futureEndTime)
                .capacity(10)
                .bookedCount(1)
                .status(SessionStatus.SCHEDULED)
                .build());

        MemberMembership mem1 = membershipRepository.save(MemberMembership.builder()
                .member(member1)
                .membershipPackage(membershipPackage)
                .remainingSessions(9)
                .startDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(29, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build());

        Booking b1 = bookingRepository.save(Booking.builder()
                .session(session)
                .member(member1)
                .membership(mem1)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build());

        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .completeSession(false)
                .items(List.of(
                        AttendanceItemRequest.builder().bookingId(b1.getId()).status(BookingStatus.ATTENDED).build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/attendance/sessions/" + session.getId())
                        .header("Authorization", "Bearer " + trainer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Chưa đến giờ diễn ra buổi tập")));
    }

    @Test
    @DisplayName("UC5.2 - Chặn điểm danh buổi tập đã hủy: Session có trạng thái CANCELED phải bị từ chối")
    void testBlockAttendanceWhenSessionCanceled() throws Exception {
        Instant pastStartTime = Instant.now().minus(60, ChronoUnit.MINUTES);
        Instant pastEndTime = Instant.now().plus(30, ChronoUnit.MINUTES);

        Session session = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .trainer(trainer1)
                .room(room)
                .startTime(pastStartTime)
                .endTime(pastEndTime)
                .capacity(10)
                .bookedCount(1)
                .status(SessionStatus.CANCELED)
                .cancelReason("Huấn luyện viên bận đột xuất")
                .build());

        MemberMembership mem1 = membershipRepository.save(MemberMembership.builder()
                .member(member1)
                .membershipPackage(membershipPackage)
                .remainingSessions(9)
                .startDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(29, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build());

        Booking b1 = bookingRepository.save(Booking.builder()
                .session(session)
                .member(member1)
                .membership(mem1)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build());

        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .completeSession(false)
                .items(List.of(
                        AttendanceItemRequest.builder().bookingId(b1.getId()).status(BookingStatus.ATTENDED).build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/attendance/sessions/" + session.getId())
                        .header("Authorization", "Bearer " + trainer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Buổi tập này đã bị hủy, không thể thực hiện điểm danh")));

        // Đảm bảo không thay đổi dữ liệu booking
        Booking unchanged = bookingRepository.findById(b1.getId()).orElseThrow();
        assertEquals(BookingStatus.CONFIRMED, unchanged.getStatus(), "Trạng thái booking không được thay đổi khi điểm danh thất bại");
        assertFalse(unchanged.getRefunded());
    }

    @Test
    @DisplayName("UC5.1 - Phân quyền điểm danh: Trainer không được điểm danh buổi của Trainer khác")
    void testBlockOtherTrainerFromMarkingAttendance() throws Exception {
        Instant pastStartTime = Instant.now().minus(30, ChronoUnit.MINUTES);
        Instant pastEndTime = Instant.now().plus(30, ChronoUnit.MINUTES);

        // Buổi do trainer1 phụ trách
        Session session = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .trainer(trainer1)
                .room(room)
                .startTime(pastStartTime)
                .endTime(pastEndTime)
                .capacity(10)
                .bookedCount(1)
                .status(SessionStatus.SCHEDULED)
                .build());

        MemberMembership mem1 = membershipRepository.save(MemberMembership.builder()
                .member(member1)
                .membershipPackage(membershipPackage)
                .remainingSessions(9)
                .startDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(29, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build());

        Booking b1 = bookingRepository.save(Booking.builder()
                .session(session)
                .member(member1)
                .membership(mem1)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build());

        MarkAttendanceRequest request = MarkAttendanceRequest.builder()
                .completeSession(false)
                .items(List.of(
                        AttendanceItemRequest.builder().bookingId(b1.getId()).status(BookingStatus.ATTENDED).build()
                ))
                .build();

        // Trainer 2 cố tình gửi điểm danh buổi của Trainer 1 -> 400 BusinessException
        mockMvc.perform(post("/api/v1/attendance/sessions/" + session.getId())
                        .header("Authorization", "Bearer " + trainer2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Bạn chỉ được phép điểm danh buổi tập do chính mình phụ trách")));
    }

    @Test
    @DisplayName("UC5.2 - Thống kê giảng dạy của Huấn luyện viên")
    void testTrainerStats() throws Exception {
        // Trainer 1 có: 1 buổi completed, 1 buổi scheduled
        Session completedSession = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .trainer(trainer1)
                .room(room)
                .startTime(Instant.now().minus(2, ChronoUnit.HOURS))
                .endTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .capacity(10)
                .bookedCount(3)
                .status(SessionStatus.COMPLETED)
                .build());

        sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .trainer(trainer1)
                .room(room)
                .startTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .capacity(10)
                .bookedCount(0)
                .status(SessionStatus.SCHEDULED)
                .build());

        MemberMembership mem1 = membershipRepository.save(MemberMembership.builder()
                .member(member1)
                .membershipPackage(membershipPackage)
                .remainingSessions(9)
                .startDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(29, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build());

        // 3 học viên: 2 attended, 1 absent -> tỷ lệ chuyên cần: 2 / 3 = 66.7%
        bookingRepository.save(Booking.builder()
                .session(completedSession)
                .member(member1)
                .membership(mem1)
                .status(BookingStatus.ATTENDED)
                .refunded(false)
                .build());

        bookingRepository.save(Booking.builder()
                .session(completedSession)
                .member(member2)
                .membership(mem1)
                .status(BookingStatus.ATTENDED)
                .refunded(false)
                .build());

        User member3 = userRepository.save(User.builder().id(UUID.randomUUID()).email("m3@pilates.vn").role(Role.MEMBER).status(UserStatus.ACTIVE).build());
        bookingRepository.save(Booking.builder()
                .session(completedSession)
                .member(member3)
                .membership(mem1)
                .status(BookingStatus.ABSENT)
                .refunded(false)
                .build());

        mockMvc.perform(get("/api/v1/statistics/trainer/my-stats")
                        .header("Authorization", "Bearer " + trainer1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalSessionsTaught", is(1)))
                .andExpect(jsonPath("$.data.totalUpcomingSessions", is(1)))
                .andExpect(jsonPath("$.data.totalStudentsAttended", is(2)))
                .andExpect(jsonPath("$.data.totalStudentsAbsent", is(1)))
                .andExpect(jsonPath("$.data.attendanceRate", is(66.7)));
    }

    @Test
    @DisplayName("UC5.3 - Báo cáo Dashboard Quản trị viên: Doanh thu chỉ tính PAID, phân loại chi tiết")
    void testAdminDashboardStats() throws Exception {
        // 1 giao dịch PAID chuyển khoản: 1,500,000
        paymentRepository.save(Payment.builder()
                .member(member1)
                .membershipPackage(membershipPackage)
                .transactionCode("TX-PAID-BANK")
                .amount(BigDecimal.valueOf(1500000))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.PAID)
                .build());

        // 1 giao dịch PAID tiền mặt: 500,000
        paymentRepository.save(Payment.builder()
                .member(member2)
                .membershipPackage(membershipPackage)
                .transactionCode("TX-PAID-CASH")
                .amount(BigDecimal.valueOf(500000))
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.PAID)
                .build());

        // 1 giao dịch UNPAID: 2,000,000 (Tuyệt đối không tính vào doanh thu)
        paymentRepository.save(Payment.builder()
                .member(member1)
                .membershipPackage(membershipPackage)
                .transactionCode("TX-UNPAID")
                .amount(BigDecimal.valueOf(2000000))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.UNPAID)
                .build());

        // 1 hội viên có gói active
        membershipRepository.save(MemberMembership.builder()
                .member(member1)
                .membershipPackage(membershipPackage)
                .remainingSessions(8)
                .startDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(29, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build());

        // 1 hội viên bị khóa
        userRepository.save(User.builder().id(UUID.randomUUID()).email("locked@pilates.vn").role(Role.MEMBER).status(UserStatus.LOCKED).build());

        mockMvc.perform(get("/api/v1/statistics/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                // Tổng doanh thu PAID = 1.500.000 + 500.000 = 2.000.000 (không tính UNPAID 2.000.000)
                .andExpect(jsonPath("$.data.totalPaidRevenue").value(2000000.0))
                .andExpect(jsonPath("$.data.bankTransferRevenue").value(1500000.0))
                .andExpect(jsonPath("$.data.cashRevenue").value(500000.0))
                .andExpect(jsonPath("$.data.unpaidPaymentsCount", is(1)))
                .andExpect(jsonPath("$.data.totalMembers", is(3))) // member1, member2, locked member
                .andExpect(jsonPath("$.data.activeMembers", is(2)))
                .andExpect(jsonPath("$.data.lockedMembers", is(1)))
                .andExpect(jsonPath("$.data.membersWithActivePackage", is(1)));

        // Kiểm tra phân quyền: Member gọi Dashboard -> 403 Forbidden
        mockMvc.perform(get("/api/v1/statistics/admin/dashboard")
                        .header("Authorization", "Bearer " + member1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("UC5.4 (R5.5) - Thống kê doanh thu theo Tháng/Năm: Kiểm tra lọc chính xác từng tháng")
    void testAdminDashboardStats_WithMonthAndYearFilter() throws Exception {
        java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        // 1 giao dịch PAID trong tháng hiện tại: 1,200,000
        paymentRepository.save(Payment.builder()
                .member(member1)
                .membershipPackage(membershipPackage)
                .transactionCode("TX-MONTH-CURRENT")
                .amount(BigDecimal.valueOf(1200000))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.PAID)
                .paidAt(Instant.now())
                .build());

        // 1 giao dịch PAID của tháng trước (hoặc 60 ngày trước): 800,000
        Instant pastDate = Instant.now().minus(60, ChronoUnit.DAYS);
        java.time.LocalDate pastLocalDate = pastDate.atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate();
        int pastMonth = pastLocalDate.getMonthValue();
        int pastYear = pastLocalDate.getYear();

        paymentRepository.save(Payment.builder()
                .member(member2)
                .membershipPackage(membershipPackage)
                .transactionCode("TX-MONTH-PAST")
                .amount(BigDecimal.valueOf(800000))
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.PAID)
                .paidAt(pastDate)
                .build());

        // Gọi API lọc theo tháng hiện tại
        mockMvc.perform(get("/api/v1/statistics/admin/dashboard")
                        .param("month", String.valueOf(currentMonth))
                        .param("year", String.valueOf(currentYear))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.selectedMonth", is(currentMonth)))
                .andExpect(jsonPath("$.data.selectedYear", is(currentYear)))
                .andExpect(jsonPath("$.data.monthlyPaidRevenue").value(1200000.0))
                .andExpect(jsonPath("$.data.monthlyBankTransferRevenue").value(1200000.0))
                .andExpect(jsonPath("$.data.monthlyCashRevenue").value(0.0))
                .andExpect(jsonPath("$.data.totalPaidRevenue").value(2000000.0));

        // Gọi API lọc theo tháng trước
        mockMvc.perform(get("/api/v1/statistics/admin/dashboard")
                        .param("month", String.valueOf(pastMonth))
                        .param("year", String.valueOf(pastYear))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.selectedMonth", is(pastMonth)))
                .andExpect(jsonPath("$.data.selectedYear", is(pastYear)))
                .andExpect(jsonPath("$.data.monthlyPaidRevenue").value(800000.0))
                .andExpect(jsonPath("$.data.monthlyCashRevenue").value(800000.0))
                .andExpect(jsonPath("$.data.monthlyBankTransferRevenue").value(0.0));
    }
}

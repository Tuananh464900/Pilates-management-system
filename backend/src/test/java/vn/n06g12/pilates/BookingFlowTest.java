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
import vn.n06g12.pilates.booking.dto.CancelBookingRequest;
import vn.n06g12.pilates.booking.dto.CreateBookingRequest;
import vn.n06g12.pilates.booking.entity.Booking;
import vn.n06g12.pilates.booking.entity.BookingStatus;
import vn.n06g12.pilates.booking.repository.BookingRepository;
import vn.n06g12.pilates.membership.entity.MemberMembership;
import vn.n06g12.pilates.membership.entity.MembershipPackage;
import vn.n06g12.pilates.membership.entity.MembershipStatus;
import vn.n06g12.pilates.membership.entity.PackageStatus;
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
import vn.n06g12.pilates.session.dto.CancelSessionRequest;
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
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookingFlowTest {

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
    private User trainerUser;
    private User memberUser;
    private String adminToken;
    private String memberToken;
    private PilatesClass pilatesClass;
    private Room room;
    private MembershipPackage membershipPackage;
    private MemberMembership activeMembership;
    private Session upcomingSession;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        paymentRepository.deleteAll();
        membershipRepository.deleteAll();
        sessionRepository.deleteAll();
        classRepository.deleteAll();
        roomRepository.deleteAll();
        packageRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Admin & Trainer & Member
        adminUser = User.builder().id(UUID.randomUUID()).email("admin@pilates.com").role(Role.ADMIN).status(UserStatus.ACTIVE).build();
        trainerUser = User.builder().id(UUID.randomUUID()).email("trainer@pilates.com").role(Role.TRAINER).status(UserStatus.ACTIVE).build();
        memberUser = User.builder().id(UUID.randomUUID()).email("member@pilates.com").role(Role.MEMBER).status(UserStatus.ACTIVE).build();
        userRepository.save(adminUser);
        userRepository.save(trainerUser);
        userRepository.save(memberUser);

        adminToken = jwtTokenValidator.generateTokenForTest(adminUser.getId(), adminUser.getEmail(), 3600000L);
        memberToken = jwtTokenValidator.generateTokenForTest(memberUser.getId(), memberUser.getEmail(), 3600000L);

        // 2. Class & Room
        pilatesClass = PilatesClass.builder().name("Reformer Cơ Bản").level(ClassLevel.BEGINNER).defaultCapacity(5).status(ClassStatus.ACTIVE).build();
        pilatesClass = classRepository.save(pilatesClass);

        room = Room.builder().name("Phòng Reformer 1").capacity(5).status(RoomStatus.ACTIVE).build();
        room = roomRepository.save(room);

        // 3. Package & Active Membership (có 5 buổi, hạn 30 ngày)
        membershipPackage = MembershipPackage.builder().name("Gói 10 buổi").price(BigDecimal.valueOf(1500000)).totalSessions(10).durationDays(30).status(PackageStatus.ACTIVE).build();
        membershipPackage = packageRepository.save(membershipPackage);

        activeMembership = MemberMembership.builder()
                .member(memberUser)
                .membershipPackage(membershipPackage)
                .remainingSessions(5)
                .startDate(Instant.now().minus(5, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(25, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build();
        activeMembership = membershipRepository.save(activeMembership);

        // 4. Session bắt đầu sau 4 giờ (còn 5 chỗ)
        upcomingSession = Session.builder()
                .pilatesClass(pilatesClass)
                .room(room)
                .trainer(trainerUser)
                .startTime(Instant.now().plus(4, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(5, ChronoUnit.HOURS))
                .capacity(5)
                .bookedCount(0)
                .status(SessionStatus.SCHEDULED)
                .build();
        upcomingSession = sessionRepository.save(upcomingSession);
    }

    @Test
    @DisplayName("UC4.1: Đăng ký buổi tập thành công -> trừ 1 lượt gói tập và tăng bookedCount")
    void testCreateBooking_Success() throws Exception {
        CreateBookingRequest req = CreateBookingRequest.builder()
                .sessionId(upcomingSession.getId())
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                .header("Authorization", "Bearer " + memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.data.className", is("Reformer Cơ Bản")));

        // Kiểm tra bookedCount tăng lên 1
        Session updatedSession = sessionRepository.findById(upcomingSession.getId()).orElseThrow();
        assertEquals(1, updatedSession.getBookedCount());

        // Kiểm tra số lượt gói tập bị trừ từ 5 xuống 4
        MemberMembership updatedMembership = membershipRepository.findById(activeMembership.getId()).orElseThrow();
        assertEquals(4, updatedMembership.getRemainingSessions());
    }

    @Test
    @DisplayName("UC4.1: Buổi tập đã hết chỗ (bookedCount == capacity) -> Bị từ chối lỗi 400")
    void testCreateBooking_FullCapacity_Fails() throws Exception {
        upcomingSession.setBookedCount(upcomingSession.getCapacity());
        sessionRepository.save(upcomingSession);

        CreateBookingRequest req = CreateBookingRequest.builder().sessionId(upcomingSession.getId()).build();

        mockMvc.perform(post("/api/v1/bookings")
                .header("Authorization", "Bearer " + memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("UC4.1: Đăng ký trùng cùng một buổi tập -> Bị từ chối lỗi 400")
    void testCreateBooking_DuplicateBooking_Fails() throws Exception {
        // Đặt trước 1 lần
        Booking b = Booking.builder()
                .member(memberUser)
                .session(upcomingSession)
                .membership(activeMembership)
                .status(BookingStatus.CONFIRMED)
                .build();
        bookingRepository.save(b);

        CreateBookingRequest req = CreateBookingRequest.builder().sessionId(upcomingSession.getId()).build();

        mockMvc.perform(post("/api/v1/bookings")
                .header("Authorization", "Bearer " + memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("UC4.1: Trùng khung giờ với một buổi khác đã đăng ký -> Bị từ chối lỗi 400")
    void testCreateBooking_TimeConflict_Fails() throws Exception {
        // Buổi 1 đã đặt từ now+4h đến now+5h
        Booking b = Booking.builder()
                .member(memberUser)
                .session(upcomingSession)
                .membership(activeMembership)
                .status(BookingStatus.CONFIRMED)
                .build();
        bookingRepository.save(b);

        // Buổi 2 trùng khung giờ (now+4h30 đến now+5h30)
        Session overlappingSession = Session.builder()
                .pilatesClass(pilatesClass)
                .room(room)
                .trainer(trainerUser)
                .startTime(Instant.now().plus(4, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES))
                .endTime(Instant.now().plus(5, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES))
                .capacity(5)
                .bookedCount(0)
                .status(SessionStatus.SCHEDULED)
                .build();
        overlappingSession = sessionRepository.save(overlappingSession);

        CreateBookingRequest req = CreateBookingRequest.builder().sessionId(overlappingSession.getId()).build();

        mockMvc.perform(post("/api/v1/bookings")
                .header("Authorization", "Bearer " + memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("UC4.2: Hủy buổi tập trước giờ bắt đầu >= 2 giờ -> Được hoàn 1 lượt về gói tập")
    void testCancelBooking_Before2Hours_RefundsSession() throws Exception {
        // Buổi tập bắt đầu sau 4 giờ (> 2 giờ)
        upcomingSession.setBookedCount(1);
        sessionRepository.save(upcomingSession);

        activeMembership.setRemainingSessions(4); // Đã trừ 1 lượt
        membershipRepository.save(activeMembership);

        Booking booking = Booking.builder()
                .member(memberUser)
                .session(upcomingSession)
                .membership(activeMembership)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build();
        booking = bookingRepository.save(booking);

        CancelBookingRequest cancelReq = CancelBookingRequest.builder().reason("Bận việc đột xuất").build();

        mockMvc.perform(post("/api/v1/bookings/" + booking.getId() + "/cancel")
                .header("Authorization", "Bearer " + memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("CANCELED")))
                .andExpect(jsonPath("$.data.refunded", is(true)));

        // Kiểm tra số lượt được hoàn từ 4 -> 5
        MemberMembership refundedMembership = membershipRepository.findById(activeMembership.getId()).orElseThrow();
        assertEquals(5, refundedMembership.getRemainingSessions());

        // Kiểm tra bookedCount của session giảm về 0
        Session sessionAfter = sessionRepository.findById(upcomingSession.getId()).orElseThrow();
        assertEquals(0, sessionAfter.getBookedCount());
    }

    @Test
    @DisplayName("UC4.2: Hủy buổi tập sát giờ trong vòng 2 giờ -> Hủy thành công nhưng KHÔNG hoàn lượt")
    void testCancelBooking_Within2Hours_NoRefund() throws Exception {
        // Buổi tập bắt đầu sau 1 giờ (< 2 giờ)
        upcomingSession.setStartTime(Instant.now().plus(1, ChronoUnit.HOURS));
        upcomingSession.setEndTime(Instant.now().plus(2, ChronoUnit.HOURS));
        upcomingSession.setBookedCount(1);
        sessionRepository.save(upcomingSession);

        activeMembership.setRemainingSessions(4);
        membershipRepository.save(activeMembership);

        Booking booking = Booking.builder()
                .member(memberUser)
                .session(upcomingSession)
                .membership(activeMembership)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build();
        booking = bookingRepository.save(booking);

        mockMvc.perform(post("/api/v1/bookings/" + booking.getId() + "/cancel")
                .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("CANCELED")))
                .andExpect(jsonPath("$.data.refunded", is(false)));

        // Số lượt vẫn giữ nguyên là 4 (không hoàn)
        MemberMembership checkMembership = membershipRepository.findById(activeMembership.getId()).orElseThrow();
        assertEquals(4, checkMembership.getRemainingSessions());
    }

    @Test
    @DisplayName("UC4.2: Buổi tập đã bắt đầu diễn ra -> Từ chối hủy (lỗi 400)")
    void testCancelBooking_AfterStartTime_Forbidden() throws Exception {
        upcomingSession.setStartTime(Instant.now().minus(10, ChronoUnit.MINUTES));
        upcomingSession.setEndTime(Instant.now().plus(50, ChronoUnit.MINUTES));
        sessionRepository.save(upcomingSession);

        Booking booking = Booking.builder()
                .member(memberUser)
                .session(upcomingSession)
                .membership(activeMembership)
                .status(BookingStatus.CONFIRMED)
                .build();
        booking = bookingRepository.save(booking);

        mockMvc.perform(post("/api/v1/bookings/" + booking.getId() + "/cancel")
                .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("UC2.5 & UC4.2: Admin hủy buổi tập -> Tự động hủy và hoàn lượt cho tất cả học viên")
    void testAdminCancelSession_RefundsAllBookings() throws Exception {
        upcomingSession.setBookedCount(1);
        sessionRepository.save(upcomingSession);

        activeMembership.setRemainingSessions(3);
        membershipRepository.save(activeMembership);

        Booking booking = Booking.builder()
                .member(memberUser)
                .session(upcomingSession)
                .membership(activeMembership)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build();
        booking = bookingRepository.save(booking);

        CancelSessionRequest adminCancelReq = CancelSessionRequest.builder()
                .reason("Huấn luyện viên bị sốt đột xuất, đổi sang buổi khác")
                .build();

        mockMvc.perform(post("/api/v1/admin/sessions/" + upcomingSession.getId() + "/cancel")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminCancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("CANCELED")));

        // Kiểm tra booking được hoàn lượt và chuyển CANCELED
        Booking bAfter = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.CANCELED, bAfter.getStatus());
        assertTrue(bAfter.getRefunded());

        // Gói tập được hoàn trả 1 lượt từ 3 -> 4
        MemberMembership mAfter = membershipRepository.findById(activeMembership.getId()).orElseThrow();
        assertEquals(4, mAfter.getRemainingSessions());
    }

    @Test
    @DisplayName("Nghiệp vụ: Khi gói cũ EXHAUSTED được hoàn lượt nhưng Member đã có gói mới ACTIVE, không để xuất hiện 2 gói ACTIVE")
    void testCancelBooking_WhenMembershipExhausted_AndOtherActiveExists_PreventsTwoActiveMemberships() throws Exception {
        // 1. Gói 1 đã hết lượt (EXHAUSTED) còn hạn
        activeMembership.setRemainingSessions(0);
        activeMembership.setStatus(MembershipStatus.EXHAUSTED);
        membershipRepository.save(activeMembership);

        // 2. Gói 2 mới mua (ACTIVE) cùng loại gói
        MemberMembership newActiveMembership = MemberMembership.builder()
                .member(memberUser)
                .membershipPackage(activeMembership.getMembershipPackage())
                .remainingSessions(10)
                .startDate(Instant.now())
                .endDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build();
        newActiveMembership = membershipRepository.save(newActiveMembership);

        // 3. Đã có booking trước đó thuộc gói 1
        upcomingSession.setBookedCount(1);
        sessionRepository.save(upcomingSession);

        Booking booking = Booking.builder()
                .member(memberUser)
                .session(upcomingSession)
                .membership(activeMembership)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build();
        booking = bookingRepository.save(booking);

        CancelBookingRequest cancelReq = CancelBookingRequest.builder().reason("Hủy buổi đặt từ gói cũ").build();

        // 4. Hủy booking trước 2 giờ -> Được hoàn lượt
        mockMvc.perform(post("/api/v1/bookings/" + booking.getId() + "/cancel")
                .header("Authorization", "Bearer " + memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.refunded", is(true)));

        // 5. Kiểm tra: Chỉ có DUY NHẤT 1 gói ACTIVE (gói mới được cộng lượt: 10 -> 11, gói cũ không biến thành ACTIVE)
        var allMemberships = membershipRepository.findByMemberIdOrderByCreatedAtDesc(memberUser.getId());
        long activeCount = allMemberships.stream().filter(m -> m.getStatus() == MembershipStatus.ACTIVE).count();
        assertEquals(1, activeCount, "Tuyệt đối chỉ có duy nhất 1 gói ACTIVE");

        MemberMembership updatedNew = membershipRepository.findById(newActiveMembership.getId()).orElseThrow();
        assertEquals(11, updatedNew.getRemainingSessions(), "Lượt hoàn được gộp an toàn vào gói ACTIVE hiện tại");
    }
}

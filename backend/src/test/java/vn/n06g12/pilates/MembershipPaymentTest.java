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
import vn.n06g12.pilates.membership.dto.ConfirmPaymentRequest;
import vn.n06g12.pilates.membership.dto.CreatePaymentRequest;
import vn.n06g12.pilates.membership.dto.PackageRequest;
import vn.n06g12.pilates.membership.entity.*;
import vn.n06g12.pilates.membership.repository.MemberMembershipRepository;
import vn.n06g12.pilates.membership.repository.MembershipPackageRepository;
import vn.n06g12.pilates.membership.repository.PaymentRepository;
import vn.n06g12.pilates.security.JwtTokenValidator;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MembershipPaymentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MemberMembershipRepository membershipRepository;

    @Autowired
    private MembershipPackageRepository packageRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private vn.n06g12.pilates.booking.repository.BookingRepository bookingRepository;

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    private User adminUser;
    private User memberUser;
    private String adminToken;
    private String memberToken;
    private MembershipPackage standardPackage;
    private MembershipPackage vipPackage;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        paymentRepository.deleteAll();
        membershipRepository.deleteAll();
        packageRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Tạo Admin
        adminUser = User.builder()
                .id(UUID.randomUUID())
                .email("admin.test@pilates.com")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(adminUser);
        adminToken = jwtTokenValidator.generateTokenForTest(adminUser.getId(), adminUser.getEmail(), 3600000L);

        // 2. Tạo Member
        memberUser = User.builder()
                .id(UUID.randomUUID())
                .email("member.test@pilates.com")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(memberUser);
        memberToken = jwtTokenValidator.generateTokenForTest(memberUser.getId(), memberUser.getEmail(), 3600000L);

        // 3. Tạo Gói tập mẫu
        standardPackage = MembershipPackage.builder()
                .name("Gói Tiêu Chuẩn")
                .description("20 buổi trong 60 ngày")
                .price(BigDecimal.valueOf(2500000))
                .totalSessions(20)
                .durationDays(60)
                .status(PackageStatus.ACTIVE)
                .build();
        standardPackage = packageRepository.save(standardPackage);

        vipPackage = MembershipPackage.builder()
                .name("Gói VIP")
                .description("60 buổi trong 180 ngày")
                .price(BigDecimal.valueOf(7000000))
                .totalSessions(60)
                .durationDays(180)
                .status(PackageStatus.ACTIVE)
                .build();
        vipPackage = packageRepository.save(vipPackage);
    }

    @Test
    @DisplayName("UC3.1: Admin tạo mới gói tập thành công")
    void testAdminCreatePackage_Success() throws Exception {
        PackageRequest request = PackageRequest.builder()
                .name("Gói Mùa Hè Rực Rỡ")
                .description("15 buổi tập")
                .price(BigDecimal.valueOf(2000000))
                .totalSessions(15)
                .durationDays(45)
                .status(PackageStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/v1/admin/packages")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Gói Mùa Hè Rực Rỡ")))
                .andExpect(jsonPath("$.data.totalSessions", is(15)))
                .andExpect(jsonPath("$.data.durationDays", is(45)));
    }

    @Test
    @DisplayName("UC3.2: Member tạo đơn mua gói tập -> trạng thái UNPAID và có mã TXN-")
    void testCreatePayment_Success() throws Exception {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .packageId(standardPackage.getId())
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .note("Thanh toán chuyển khoản qua QR")
                .build();

        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("UNPAID")))
                .andExpect(jsonPath("$.data.transactionCode", startsWith("TXN-")))
                .andExpect(jsonPath("$.data.amount", is(2500000.0)));
    }

    @Test
    @DisplayName("UC3.2 & UC3.3: Admin xác nhận PAID -> Kích hoạt gói tập mới thành công")
    void testConfirmPayment_ActivatesNewMembership() throws Exception {
        // Tạo giao dịch UNPAID
        Payment payment = Payment.builder()
                .transactionCode("TXN-TEST-001")
                .member(memberUser)
                .membershipPackage(standardPackage)
                .amount(standardPackage.getPrice())
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.UNPAID)
                .build();
        payment = paymentRepository.save(payment);

        ConfirmPaymentRequest confirmReq = ConfirmPaymentRequest.builder()
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .note("Đã nhận đủ tiền từ ngân hàng VCB")
                .build();

        mockMvc.perform(post("/api/v1/admin/payments/" + payment.getId() + "/confirm")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("PAID")));

        // Kiểm tra trong DB: gói của member đã được kích hoạt
        var activeOpt = membershipRepository.findActiveMembership(memberUser.getId(), MembershipStatus.ACTIVE, Instant.now());
        assertTrue(activeOpt.isPresent());
        MemberMembership membership = activeOpt.get();
        assertEquals(20, membership.getRemainingSessions());
        assertEquals(standardPackage.getId(), membership.getMembershipPackage().getId());
        assertTrue(membership.getEndDate().isAfter(Instant.now().plus(58, ChronoUnit.DAYS)));
    }

    @Test
    @DisplayName("UC3.3: Gia hạn cùng loại gói -> Cộng dồn buổi tập và gia hạn ngày hết hạn")
    void testRenewMembership_SamePackage_Success() throws Exception {
        // Member đã có gói Standard còn 5 buổi, hạn còn 10 ngày
        Instant initialEnd = Instant.now().plus(10, ChronoUnit.DAYS);
        MemberMembership existingMembership = MemberMembership.builder()
                .member(memberUser)
                .membershipPackage(standardPackage)
                .remainingSessions(5)
                .startDate(Instant.now().minus(50, ChronoUnit.DAYS))
                .endDate(initialEnd)
                .status(MembershipStatus.ACTIVE)
                .build();
        existingMembership = membershipRepository.save(existingMembership);

        // Member mua tiếp gói Standard (cùng loại)
        Payment renewPayment = Payment.builder()
                .transactionCode("TXN-RENEW-001")
                .member(memberUser)
                .membershipPackage(standardPackage)
                .amount(standardPackage.getPrice())
                .status(PaymentStatus.UNPAID)
                .build();
        renewPayment = paymentRepository.save(renewPayment);

        // Admin duyệt thanh toán
        mockMvc.perform(post("/api/v1/admin/payments/" + renewPayment.getId() + "/confirm")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Kiểm tra: số buổi mới = 5 + 20 = 25, hạn mới = initialEnd + 60 ngày
        MemberMembership updated = membershipRepository.findById(existingMembership.getId()).orElseThrow();
        assertEquals(25, updated.getRemainingSessions());
        // Hạn ngày tăng thêm 60 ngày so với initialEnd
        long daysDiff = ChronoUnit.DAYS.between(initialEnd, updated.getEndDate());
        assertTrue(daysDiff >= 59 && daysDiff <= 61);
    }

    @Test
    @DisplayName("UC3.3: Quy tắc cố định - Đang có gói active, mua khác loại gói -> Bị từ chối (400)")
    void testRenewMembership_DifferentPackage_Rejected() throws Exception {
        // Member đang có gói Standard active
        MemberMembership existingMembership = MemberMembership.builder()
                .member(memberUser)
                .membershipPackage(standardPackage)
                .remainingSessions(10)
                .startDate(Instant.now())
                .endDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build();
        membershipRepository.save(existingMembership);

        // Member cố tình tạo đơn mua gói VIP khác loại
        CreatePaymentRequest diffRequest = CreatePaymentRequest.builder()
                .packageId(vipPackage.getId())
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build();

        mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", "Bearer " + memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(diffRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("UC3.4: Member xem gói hiện tại và kiểm tra cảnh báo <= 2 lượt hoặc <= 7 ngày")
    void testMemberActiveMembership_Warnings() throws Exception {
        // Member có gói còn 2 buổi tập và còn 5 ngày -> Cả 2 cảnh báo đều true
        MemberMembership warningMembership = MemberMembership.builder()
                .member(memberUser)
                .membershipPackage(standardPackage)
                .remainingSessions(2)
                .startDate(Instant.now().minus(55, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(5, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build();
        membershipRepository.save(warningMembership);

        mockMvc.perform(get("/api/v1/memberships/my-active")
                .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.remainingSessions", is(2)))
                .andExpect(jsonPath("$.data.warningSessions", is(true)))
                .andExpect(jsonPath("$.data.warningDays", is(true)));
    }

    @Test
    @DisplayName("UC3.5: Tính tổng doanh thu chỉ từ các giao dịch PAID")
    void testRevenueCalculation_OnlyIncludesPaid() throws Exception {
        // 1 giao dịch PAID 2.500.000
        Payment p1 = Payment.builder()
                .transactionCode("TXN-PAID-01")
                .member(memberUser)
                .membershipPackage(standardPackage)
                .amount(BigDecimal.valueOf(2500000))
                .status(PaymentStatus.PAID)
                .paidAt(Instant.now())
                .build();
        paymentRepository.save(p1);

        // 1 giao dịch UNPAID 7.000.000 (không tính)
        Payment p2 = Payment.builder()
                .transactionCode("TXN-UNPAID-02")
                .member(memberUser)
                .membershipPackage(vipPackage)
                .amount(BigDecimal.valueOf(7000000))
                .status(PaymentStatus.UNPAID)
                .build();
        paymentRepository.save(p2);

        // 1 giao dịch CANCELED 2.500.000 (không tính)
        Payment p3 = Payment.builder()
                .transactionCode("TXN-CANCELED-03")
                .member(memberUser)
                .membershipPackage(standardPackage)
                .amount(BigDecimal.valueOf(2500000))
                .status(PaymentStatus.CANCELED)
                .build();
        paymentRepository.save(p3);

        mockMvc.perform(get("/api/v1/admin/payments/revenue")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRevenue", is(2500000.0)));
    }

    @Test
    @DisplayName("Nghiệp vụ: Gia hạn gói EXHAUSTED còn hạn sử dụng sẽ gia hạn gói cũ, không tạo gói mới, tránh xuất hiện 2 gói ACTIVE")
    void testRenewExhaustedMembership_DoesNotCreateDuplicateActiveMembership() throws Exception {
        // 1. Tạo gói EXHAUSTED còn hạn sử dụng (hết lượt nhưng chưa hết ngày)
        MemberMembership exhaustedMembership = MemberMembership.builder()
                .member(memberUser)
                .membershipPackage(standardPackage)
                .remainingSessions(0)
                .startDate(Instant.now().minus(10, ChronoUnit.DAYS))
                .endDate(Instant.now().plus(20, ChronoUnit.DAYS))
                .status(MembershipStatus.EXHAUSTED)
                .build();
        membershipRepository.save(exhaustedMembership);

        // 2. Member mua tiếp cùng loại gói standardPackage
        Payment payment = Payment.builder()
                .transactionCode("TXN-RENEW-EXHAUSTED")
                .member(memberUser)
                .membershipPackage(standardPackage)
                .amount(BigDecimal.valueOf(2500000))
                .status(PaymentStatus.UNPAID)
                .build();
        paymentRepository.save(payment);

        // 3. Admin duyệt thanh toán
        ConfirmPaymentRequest confirmRequest = ConfirmPaymentRequest.builder()
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .note("Duyệt thanh toán gia hạn cho gói đã hết lượt")
                .build();

        mockMvc.perform(post("/api/v1/admin/payments/" + payment.getId() + "/confirm")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("PAID")))
                .andExpect(jsonPath("$.data.membershipId", notNullValue()));

        // 4. Kiểm tra trong DB: chỉ có DUY NHẤT 1 bản ghi Membership, và trạng thái là ACTIVE!
        var allMemberships = membershipRepository.findByMemberIdOrderByCreatedAtDesc(memberUser.getId());
        assertEquals(1, allMemberships.size(), "Phải gia hạn gói hiện tại, tuyệt đối không tạo thêm bản ghi mới");
        assertEquals(MembershipStatus.ACTIVE, allMemberships.get(0).getStatus());
        assertEquals(standardPackage.getTotalSessions(), allMemberships.get(0).getRemainingSessions());
    }
}

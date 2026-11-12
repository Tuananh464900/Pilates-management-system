package vn.n06g12.pilates.statistics.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.n06g12.pilates.booking.entity.BookingStatus;
import vn.n06g12.pilates.booking.repository.BookingRepository;
import vn.n06g12.pilates.membership.entity.PaymentMethod;
import vn.n06g12.pilates.membership.entity.PaymentStatus;
import vn.n06g12.pilates.membership.repository.MemberMembershipRepository;
import vn.n06g12.pilates.membership.repository.PaymentRepository;
import vn.n06g12.pilates.session.entity.SessionStatus;
import vn.n06g12.pilates.session.repository.SessionRepository;
import vn.n06g12.pilates.statistics.dto.AdminDashboardResponse;
import vn.n06g12.pilates.statistics.dto.TrainerStatsResponse;
import vn.n06g12.pilates.user.entity.Role;
import vn.n06g12.pilates.user.entity.UserStatus;
import vn.n06g12.pilates.user.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
@Service
public class StatisticsService {

    private final SessionRepository sessionRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final MemberMembershipRepository memberMembershipRepository;

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Autowired
    public StatisticsService(
            SessionRepository sessionRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            MemberMembershipRepository memberMembershipRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.memberMembershipRepository = memberMembershipRepository;
    }

    /**
     * UC5.2: Thống kê hiệu suất và lịch dạy của Huấn luyện viên
     */
    @Transactional(readOnly = true)
    public TrainerStatsResponse getTrainerStats(UUID trainerId) {
        long totalSessionsTaught = sessionRepository.countByTrainerIdAndStatus(trainerId, SessionStatus.COMPLETED);
        long totalUpcomingSessions = sessionRepository.countByTrainerIdAndStatus(trainerId, SessionStatus.SCHEDULED);
        long totalStudentsAttended = bookingRepository.countTrainerBookingsByStatus(trainerId, BookingStatus.ATTENDED);
        long totalStudentsAbsent = bookingRepository.countTrainerBookingsByStatus(trainerId, BookingStatus.ABSENT);

        long totalMarked = totalStudentsAttended + totalStudentsAbsent;
        double attendanceRate = totalMarked > 0
                ? BigDecimal.valueOf((totalStudentsAttended * 100.0) / totalMarked)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue()
                : 0.0;

        return TrainerStatsResponse.builder()
                .totalSessionsTaught(totalSessionsTaught)
                .totalUpcomingSessions(totalUpcomingSessions)
                .totalStudentsAttended(totalStudentsAttended)
                .totalStudentsAbsent(totalStudentsAbsent)
                .attendanceRate(attendanceRate)
                .build();
    }

    /**
     * UC5.3 & UC5.4 (R5.5): Báo cáo & Thống kê tổng hợp dành cho Quản trị viên (Admin Dashboard)
     * Ràng buộc:
     * - Doanh thu chỉ được tính từ các giao dịch có trạng thái PAID
     * - Hỗ trợ lọc doanh thu theo Tháng/Năm được chỉ định
     */
    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboardStats(Integer month, Integer year) {
        LocalDate today = LocalDate.now(VIETNAM_ZONE);
        int queryMonth = (month != null && month >= 1 && month <= 12) ? month : today.getMonthValue();
        int queryYear = (year != null && year >= 2020) ? year : today.getYear();

        YearMonth ym = YearMonth.of(queryYear, queryMonth);
        Instant startOfMonth = ym.atDay(1).atStartOfDay(VIETNAM_ZONE).toInstant();
        Instant endOfMonth = ym.atEndOfMonth().atTime(23, 59, 59, 999_999_999).atZone(VIETNAM_ZONE).toInstant();

        // 1. Doanh thu theo tháng/năm đã chọn
        BigDecimal monthlyPaidRevenue = paymentRepository.calculatePaidRevenueBetween(startOfMonth, endOfMonth);
        BigDecimal monthlyBankTransferRevenue = paymentRepository.calculatePaidRevenueBetweenAndMethod(startOfMonth, endOfMonth, PaymentMethod.BANK_TRANSFER);
        BigDecimal monthlyCashRevenue = paymentRepository.calculatePaidRevenueBetweenAndMethod(startOfMonth, endOfMonth, PaymentMethod.CASH);

        // 2. Doanh thu toàn kỳ (All-time)
        BigDecimal totalPaidRevenue = paymentRepository.calculateTotalPaidRevenue();
        BigDecimal bankTransferRevenue = paymentRepository.calculatePaidRevenueByMethod(PaymentMethod.BANK_TRANSFER);
        BigDecimal cashRevenue = paymentRepository.calculatePaidRevenueByMethod(PaymentMethod.CASH);
        long unpaidPaymentsCount = paymentRepository.countByStatus(PaymentStatus.UNPAID);

        long totalMembers = userRepository.countByRole(Role.MEMBER);
        long activeMembers = userRepository.countByRoleAndStatus(Role.MEMBER, UserStatus.ACTIVE);
        long lockedMembers = userRepository.countByRoleAndStatus(Role.MEMBER, UserStatus.LOCKED);
        long membersWithActivePackage = memberMembershipRepository.countDistinctActiveMemberships(Instant.now());

        long totalSessions = sessionRepository.count();
        long scheduledSessions = sessionRepository.countByStatus(SessionStatus.SCHEDULED);
        long completedSessions = sessionRepository.countByStatus(SessionStatus.COMPLETED);
        long canceledSessions = sessionRepository.countByStatus(SessionStatus.CANCELED);
        long totalConfirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);

        return AdminDashboardResponse.builder()
                .selectedMonth(queryMonth)
                .selectedYear(queryYear)
                .monthlyPaidRevenue(monthlyPaidRevenue != null ? monthlyPaidRevenue : BigDecimal.ZERO)
                .monthlyBankTransferRevenue(monthlyBankTransferRevenue != null ? monthlyBankTransferRevenue : BigDecimal.ZERO)
                .monthlyCashRevenue(monthlyCashRevenue != null ? monthlyCashRevenue : BigDecimal.ZERO)
                .totalPaidRevenue(totalPaidRevenue != null ? totalPaidRevenue : BigDecimal.ZERO)
                .bankTransferRevenue(bankTransferRevenue != null ? bankTransferRevenue : BigDecimal.ZERO)
                .cashRevenue(cashRevenue != null ? cashRevenue : BigDecimal.ZERO)
                .unpaidPaymentsCount(unpaidPaymentsCount)
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .lockedMembers(lockedMembers)
                .membersWithActivePackage(membersWithActivePackage)
                .totalSessions(totalSessions)
                .scheduledSessions(scheduledSessions)
                .completedSessions(completedSessions)
                .canceledSessions(canceledSessions)
                .totalConfirmedBookings(totalConfirmedBookings)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboardStats() {
        return getAdminDashboardStats(null, null);
    }
}

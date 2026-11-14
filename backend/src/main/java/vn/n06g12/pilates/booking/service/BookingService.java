package vn.n06g12.pilates.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.n06g12.pilates.booking.dto.BookingResponse;
import vn.n06g12.pilates.booking.entity.Booking;
import vn.n06g12.pilates.booking.entity.BookingStatus;
import vn.n06g12.pilates.booking.repository.BookingRepository;
import vn.n06g12.pilates.common.exception.BusinessException;
import vn.n06g12.pilates.common.exception.ResourceNotFoundException;
import vn.n06g12.pilates.membership.entity.MemberMembership;
import vn.n06g12.pilates.membership.entity.MembershipStatus;
import vn.n06g12.pilates.membership.repository.MemberMembershipRepository;
import vn.n06g12.pilates.session.entity.Session;
import vn.n06g12.pilates.session.entity.SessionStatus;
import vn.n06g12.pilates.session.repository.SessionRepository;
import vn.n06g12.pilates.user.entity.User;
import vn.n06g12.pilates.user.repository.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SessionRepository sessionRepository;
    private final MemberMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    /**
     * UC4.1: Đăng ký buổi tập Pilates
     * Ràng buộc:
     * - Buổi tập SCHEDULED, chưa bắt đầu
     * - Còn sức chứa (bookedCount < capacity), khóa bi quan chống race condition
     * - Hội viên có gói ACTIVE, còn lượt (remainingSessions > 0) và còn hạn tại thời điểm buổi tập
     * - Không đăng ký trùng cùng buổi
     * - Không trùng khung thời gian với buổi khác đã đăng ký
     * - Thao tác nguyên tử: tạo Booking, trừ 1 lượt, tăng bookedCount
     */
    @Transactional
    public BookingResponse createBooking(UUID memberId, Long sessionId) {
        Instant now = Instant.now();

        // 1. Khóa buổi tập với PESSIMISTIC_WRITE để chống race condition khi đồng thời đặt chỗ
        Session session = sessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tập với ID: " + sessionId));

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new BusinessException("Buổi tập hiện không ở trạng thái mở đăng ký (trạng thái: " + session.getStatus() + ")");
        }

        if (session.getStartTime().isBefore(now)) {
            throw new BusinessException("Buổi tập đã bắt đầu hoặc đã kết thúc, không thể đăng ký!");
        }

        if (session.getBookedCount() >= session.getCapacity()) {
            throw new BusinessException("Buổi tập đã hết chỗ trống (" + session.getBookedCount() + "/" + session.getCapacity() + ")");
        }

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin hội viên"));

        // 2. Khóa bi quan (PESSIMISTIC_WRITE) MemberMembership TRƯỚC KHI kiểm tra xung đột lịch và số lượt
        // Tuần tự hóa tuyệt đối mọi thao tác đặt lịch của cùng một hội viên, chống race condition đặt trùng giờ
        MemberMembership membership = membershipRepository.findActiveMembershipWithLock(memberId, MembershipStatus.ACTIVE, now)
                .orElseThrow(() -> new BusinessException("Bạn không có gói tập nào đang hoạt động hoặc gói đã hết lượt. Vui lòng mua hoặc gia hạn gói tập!"));

        if (membership.getRemainingSessions() <= 0) {
            membership.setStatus(MembershipStatus.EXHAUSTED);
            membershipRepository.save(membership);
            throw new BusinessException("Gói tập của bạn đã hết số buổi tập khả dụng!");
        }

        if (membership.getEndDate().isBefore(session.getStartTime())) {
            throw new BusinessException(
                    "Gói tập của bạn sẽ hết hạn trước thời điểm buổi tập diễn ra " +
                    "(Hạn gói: " + membership.getEndDate() + ", Buổi tập: " + session.getStartTime() + "). " +
                    "Vui lòng gia hạn gói tập để đặt lịch!"
            );
        }

        // 3. Sau khi đã giữ khóa Session và khóa Membership, kiểm tra không đăng ký trùng cùng một buổi tập
        if (bookingRepository.existsByMemberIdAndSessionIdAndStatus(memberId, sessionId, BookingStatus.CONFIRMED)) {
            throw new BusinessException("Bạn đã đăng ký buổi tập này rồi!");
        }

        // 4. Kiểm tra không trùng khung thời gian với buổi tập khác mà hội viên đã đăng ký
        long conflictingCount = bookingRepository.countConflictingBookings(
                memberId,
                sessionId,
                session.getStartTime(),
                session.getEndTime()
        );
        if (conflictingCount > 0) {
            throw new BusinessException("Bạn đã có một lịch tập khác trùng khung giờ với buổi tập này!");
        }

        // 5. Thao tác nguyên tử: Trừ 1 lượt gói tập
        membership.setRemainingSessions(membership.getRemainingSessions() - 1);
        if (membership.getRemainingSessions() == 0) {
            membership.setStatus(MembershipStatus.EXHAUSTED);
        }
        membershipRepository.save(membership);

        // 6. Tăng số lượng đã đặt của buổi tập
        session.setBookedCount(session.getBookedCount() + 1);
        sessionRepository.save(session);

        // 7. Tạo bản ghi Booking
        Booking booking = Booking.builder()
                .member(member)
                .session(session)
                .membership(membership)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Đặt chỗ thành công buổi tập {} cho hội viên {}. Lượt gói còn lại: {}",
                sessionId, member.getEmail(), membership.getRemainingSessions());

        return BookingResponse.from(saved);
    }

    /**
     * UC4.2: Hội viên hủy đăng ký buổi tập (Áp dụng quy tắc biên 2 giờ)
     * - Chỉ được hủy trước khi buổi tập bắt đầu
     * - Hủy trước >= 2 giờ (120 phút): hoàn 1 lượt về đúng gói tập đã trừ
     * - Hủy trong vòng 2 giờ trước giờ tập: hủy chỗ nhưng KHÔNG hoàn lượt
     */
    @Transactional
    public BookingResponse cancelBooking(UUID memberId, Long bookingId, String reason) {
        // Lấy Session ID và Membership ID của booking (chỉ đọc giá trị khóa ngoại, không giữ khóa)
        java.util.List<Object[]> refs = bookingRepository.findSessionAndMembershipIdByBookingId(bookingId);
        if (refs.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin đăng ký với ID: " + bookingId);
        }
        Long sessionId = (Long) refs.get(0)[0];
        Long membershipId = (Long) refs.get(0)[1];

        // 1. Thống nhất thứ tự khóa hệ thống (Session -> Membership -> Booking): Khóa Session trước với PESSIMISTIC_WRITE
        Session session = sessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tập với ID: " + sessionId));

        Instant now = Instant.now();
        if (now.isAfter(session.getStartTime())) {
            throw new BusinessException("Buổi tập đã bắt đầu diễn ra, không thể hủy đăng ký!");
        }

        if (session.getStatus() == SessionStatus.CANCELED) {
            throw new BusinessException("Buổi tập đã bị hủy bởi quản trị viên!");
        }

        // 2. Tiếp theo khóa Membership với PESSIMISTIC_WRITE
        MemberMembership membership = membershipRepository.findByIdWithLock(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin gói tập của lượt đăng ký"));

        // 3. Tiếp theo khóa Booking với PESSIMISTIC_WRITE
        Booking booking = bookingRepository.findByIdWithLock(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin đăng ký với ID: " + bookingId));

        if (!booking.getMember().getId().equals(memberId)) {
            throw new BusinessException("Bạn không có quyền thao tác trên lượt đăng ký này!");
        }

        // Kiểm tra lại trạng thái sau khi đã giữ các khóa (chống race condition với Admin hủy session)
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Lượt đăng ký không ở trạng thái có thể hủy (trạng thái: " + booking.getStatus() + ")");
        }

        // 4. Kiểm tra quy tắc biên 2 giờ (120 phút)
        long minutesUntilStart = Duration.between(now, session.getStartTime()).toMinutes();
        boolean isRefundable = minutesUntilStart >= 120;

        if (isRefundable) {
            membership.setRemainingSessions(membership.getRemainingSessions() + 1);

            // KIỂM SOÁT KHÔI PHỤC ACTIVE:
            // Chỉ khôi phục ACTIVE cho gói EXHAUSTED nếu gói còn hạn sử dụng VÀ Member chưa có gói nào khác đang ACTIVE
            if (membership.getStatus() == MembershipStatus.EXHAUSTED && membership.getEndDate().isAfter(now)) {
                // Khóa gói ACTIVE với PESSIMISTIC_WRITE để chống mất cập nhật khi thao tác đồng thời (Lỗi A)
                Optional<MemberMembership> existingActiveOpt = membershipRepository.findCurrentActiveMembershipWithLock(
                        membership.getMember().getId(),
                        MembershipStatus.ACTIVE,
                        now
                );

                if (existingActiveOpt.isPresent() && !existingActiveOpt.get().getId().equals(membership.getId())) {
                    MemberMembership existingActive = existingActiveOpt.get();
                    if (existingActive.getMembershipPackage().getId().equals(membership.getMembershipPackage().getId())) {
                        // Cùng loại gói: Gộp 1 lượt hoàn vào gói ACTIVE hiện tại (đã khóa với PESSIMISTIC_WRITE)
                        existingActive.setRemainingSessions(existingActive.getRemainingSessions() + 1);
                        membershipRepository.save(existingActive);
                        membership.setRemainingSessions(membership.getRemainingSessions() - 1);
                        log.info("Hội viên {} đã có gói ACTIVE cùng loại ({}), gộp lượt hoàn vào gói ACTIVE hiện tại",
                                booking.getMember().getEmail(), existingActive.getId());
                    } else {
                        // Khác loại gói: Không kích hoạt ACTIVE cho gói cũ để tránh vi phạm quy tắc cùng lúc có 2 gói ACTIVE
                        log.warn("Hội viên {} đang có gói khác ACTIVE ({}), giữ trạng thái gói cũ để tránh xuất hiện 2 gói ACTIVE đồng thời",
                                booking.getMember().getEmail(), existingActive.getId());
                    }
                } else {
                    membership.setStatus(MembershipStatus.ACTIVE);
                }
            }
            membershipRepository.save(membership);
            booking.setRefunded(true);
            log.info("Hủy trước >= 2h ({} phút). Hoàn trả 1 lượt về gói {} cho hội viên {}",
                    minutesUntilStart, membership.getId(), booking.getMember().getEmail());
        } else {
            booking.setRefunded(false);
            log.info("Hủy sát giờ trong vòng 2h ({} phút). KHÔNG hoàn lượt tập cho hội viên {}",
                    minutesUntilStart, booking.getMember().getEmail());
        }

        // 5. Giảm số người đã đặt của buổi tập
        session.setBookedCount(Math.max(0, session.getBookedCount() - 1));
        sessionRepository.save(session);

        // 6. Cập nhật Booking thành CANCELED
        booking.setStatus(BookingStatus.CANCELED);
        booking.setCanceledAt(now);
        booking.setCancelReason(reason != null && !reason.isBlank() ? reason : (isRefundable ? "Hội viên tự hủy (Hoàn 1 lượt)" : "Hội viên tự hủy sát giờ (Không hoàn lượt)"));

        Booking updated = bookingRepository.save(booking);
        return BookingResponse.from(updated);
    }

    /**
     * UC2.5 & UC4.2: Admin hủy buổi tập chưa bắt đầu
     * Hủy tất cả booking CONFIRMED và HOÀN TRẢ 1 LƯỢT cho mỗi booking về đúng gói đã trừ.
     */
    @Transactional
    public void cancelAllBookingsForSession(Long sessionId, String reason) {
        List<Booking> confirmedBookings = bookingRepository.findBySessionIdAndStatus(sessionId, BookingStatus.CONFIRMED);
        Instant now = Instant.now();

        for (Booking b : confirmedBookings) {
            MemberMembership membership = b.getMembership();
            membership.setRemainingSessions(membership.getRemainingSessions() + 1);
            if (membership.getStatus() == MembershipStatus.EXHAUSTED && membership.getEndDate().isAfter(now)) {
                Optional<MemberMembership> existingActiveOpt = membershipRepository.findActiveMembership(
                        membership.getMember().getId(),
                        MembershipStatus.ACTIVE,
                        now
                );
                if (existingActiveOpt.isPresent() && !existingActiveOpt.get().getId().equals(membership.getId())) {
                    MemberMembership existingActive = existingActiveOpt.get();
                    if (existingActive.getMembershipPackage().getId().equals(membership.getMembershipPackage().getId())) {
                        existingActive.setRemainingSessions(existingActive.getRemainingSessions() + 1);
                        membershipRepository.save(existingActive);
                        membership.setRemainingSessions(membership.getRemainingSessions() - 1);
                    }
                } else {
                    membership.setStatus(MembershipStatus.ACTIVE);
                }
            }
            membershipRepository.save(membership);

            b.setRefunded(true);
            b.setStatus(BookingStatus.CANCELED);
            b.setCanceledAt(now);
            b.setCancelReason("Buổi tập bị Admin hủy: " + reason);
            bookingRepository.save(b);
        }
        log.info("Đã hủy và hoàn lượt cho {} học viên của buổi tập {}", confirmedBookings.size(), sessionId);
    }

    @Transactional
    public Page<BookingResponse> getMyBookings(UUID memberId, Pageable pageable) {
        Page<Booking> page = bookingRepository.findByMemberIdWithDetails(memberId, pageable);
        Instant now = Instant.now();
        for (Booking b : page.getContent()) {
            if (b.getStatus() == BookingStatus.CONFIRMED &&
                    b.getSession() != null &&
                    b.getSession().getEndTime() != null &&
                    b.getSession().getEndTime().isBefore(now)) {
                b.setStatus(BookingStatus.ATTENDED);
                bookingRepository.save(b);
            }
        }
        return page.map(BookingResponse::from);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getTrainerSessionBookings(UUID trainerId, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tập"));

        if (!session.getTrainer().getId().equals(trainerId)) {
            throw new BusinessException("Bạn chỉ có thể xem danh sách học viên của buổi tập do chính mình phụ trách!");
        }

        return bookingRepository.findBySessionIdWithMember(sessionId)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAdminSessionBookings(Long sessionId) {
        return bookingRepository.findBySessionIdWithMember(sessionId)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }
}

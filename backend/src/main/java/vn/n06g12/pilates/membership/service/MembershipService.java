package vn.n06g12.pilates.membership.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.n06g12.pilates.booking.repository.BookingRepository;
import vn.n06g12.pilates.common.exception.BusinessException;
import vn.n06g12.pilates.membership.dto.MemberMembershipResponse;
import vn.n06g12.pilates.membership.entity.MemberMembership;
import vn.n06g12.pilates.membership.entity.MembershipPackage;
import vn.n06g12.pilates.membership.entity.MembershipStatus;
import vn.n06g12.pilates.membership.entity.Payment;
import vn.n06g12.pilates.membership.repository.MemberMembershipRepository;
import vn.n06g12.pilates.user.entity.User;
import vn.n06g12.pilates.user.repository.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MemberMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public Optional<MemberMembershipResponse> getActiveMembership(UUID memberId) {
        Instant now = Instant.now();
        Optional<MemberMembership> activeOpt = membershipRepository.findActiveMembership(memberId, MembershipStatus.ACTIVE, now);

        if (activeOpt.isPresent()) {
            return Optional.of(MemberMembershipResponse.from(activeOpt.get()));
        }

        // Tự động quét và cập nhật trạng thái các gói đã hết hạn hoặc hết lượt tập
        membershipRepository.findByMemberIdOrderByCreatedAtDesc(memberId).forEach(m -> {
            if (m.getStatus() == MembershipStatus.ACTIVE) {
                if (m.getRemainingSessions() <= 0) {
                    m.setStatus(MembershipStatus.EXHAUSTED);
                    membershipRepository.save(m);
                } else if (m.getEndDate().isBefore(now)) {
                    m.setStatus(MembershipStatus.EXPIRED);
                    membershipRepository.save(m);
                }
            }
        });

        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Page<MemberMembershipResponse> getMembershipHistory(UUID memberId, Pageable pageable) {
        return membershipRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(MemberMembershipResponse::from);
    }

    /**
     * UC3.3: Kích hoạt hoặc gia hạn gói tập khi giao dịch thanh toán thành công (PAID).
     * Bọc trong transaction nguyên tử, kiểm tra chặt chẽ:
     * - Một Member chỉ tối đa 1 gói active
     * - Khi đang active: chỉ cho phép gia hạn nếu CÙNG LOẠI GÓI
     * - Chống lặp (Idempotent): tối đa 1 lần kích hoạt / gia hạn cho mỗi giao dịch
     */
    @Transactional
    public MemberMembership activateOrRenewMembership(Payment payment) {
        if (payment.getMembership() != null) {
            log.info("Giao dịch {} đã được kích hoạt/gia hạn gói trước đó (idempotent)", payment.getTransactionCode());
            return payment.getMembership();
        }

        // Khóa bi quan (PESSIMISTIC_WRITE) Member để tuần tự hóa các yêu cầu kích hoạt / gia hạn đồng thời của cùng một hội viên
        User member = userRepository.findByIdWithLock(payment.getMember().getId())
                .orElse(payment.getMember());
        MembershipPackage packageToActivate = payment.getMembershipPackage();
        Instant now = Instant.now();

        // Khóa bi quan (PESSIMISTIC_WRITE) MemberMembership:
        // Tìm các gói tập hiện có có thể gia hạn (bao gồm cả ACTIVE và EXHAUSTED còn hạn sử dụng)
        List<MemberMembership> renewableList = membershipRepository.findRenewableMembershipsWithLock(
                member.getId(),
                now
        );

        MemberMembership membership;

        if (!renewableList.isEmpty()) {
            MemberMembership currentMembership = renewableList.get(0);

            // 1. Cùng loại gói: Luôn luôn gia hạn gói hiện tại, cộng dồn số buổi và kéo dài hạn,
            // TUYỆT ĐỐI KHÔNG TẠO GÓI MỚI (ngăn chặn xuất hiện 2 gói ACTIVE khi gói cũ EXHAUSTED còn hạn hoặc hoàn lượt)
            if (currentMembership.getMembershipPackage().getId().equals(packageToActivate.getId())) {
                currentMembership.setRemainingSessions(currentMembership.getRemainingSessions() + packageToActivate.getTotalSessions());
                Instant baseDate = currentMembership.getEndDate().isAfter(now) ? currentMembership.getEndDate() : now;
                currentMembership.setEndDate(baseDate.plus(packageToActivate.getDurationDays(), ChronoUnit.DAYS));
                currentMembership.setStatus(MembershipStatus.ACTIVE);
                membership = membershipRepository.save(currentMembership);
                log.info("Gia hạn thành công gói {} (trạng thái trước: {}) cho hội viên {}. Tổng lượt mới: {}, Hạn mới: {}",
                        currentMembership.getId(), currentMembership.getStatus(), member.getEmail(), membership.getRemainingSessions(), membership.getEndDate());
            } else {
                // 2. Khác loại gói:
                // Nếu gói hiện tại đang ACTIVE và CÒN LƯỢT (> 0) -> Chặn theo quy định
                if (currentMembership.getStatus() == MembershipStatus.ACTIVE && currentMembership.getRemainingSessions() > 0) {
                    throw new BusinessException(
                            "Hội viên hiện đang có gói tập '" + currentMembership.getMembershipPackage().getName() +
                            "' còn hiệu lực (" + currentMembership.getRemainingSessions() + " lượt). " +
                            "Quy định chỉ cho phép gia hạn cùng loại gói tập khi đang có gói active!"
                    );
                }

                // Lỗi C: Nếu gói cũ khác loại còn các booking tương lai chưa diễn ra có khả năng phát sinh hoàn lượt -> Chặn chuyển gói
                long pendingBookings = bookingRepository.countPendingConfirmedBookings(currentMembership.getId(), now);
                if (pendingBookings > 0) {
                    throw new BusinessException(
                            "Hội viên vẫn còn " + pendingBookings + " buổi tập đã đặt thuộc gói cũ ('" +
                            currentMembership.getMembershipPackage().getName() + "') chưa diễn ra có khả năng phát sinh hoàn lượt. " +
                            "Vui lòng hoàn thành các buổi tập hoặc hủy đặt chỗ trước khi chuyển đổi sang loại gói tập mới!"
                    );
                }

                // Nếu gói cũ khác loại đã hết lượt (EXHAUSTED) và không còn booking dở dang -> Cho phép kích hoạt gói mới khác loại
                Instant startDate = now;
                Instant endDate = startDate.plus(packageToActivate.getDurationDays(), ChronoUnit.DAYS);

                MemberMembership newMembership = MemberMembership.builder()
                        .member(member)
                        .membershipPackage(packageToActivate)
                        .remainingSessions(packageToActivate.getTotalSessions())
                        .startDate(startDate)
                        .endDate(endDate)
                        .status(MembershipStatus.ACTIVE)
                        .build();

                membership = membershipRepository.save(newMembership);
                log.info("Hội viên {} chuyển từ gói hết lượt sang gói mới {}. Số lượt: {}, Hạn đến: {}",
                        member.getEmail(), membership.getId(), membership.getRemainingSessions(), membership.getEndDate());
            }
        } else {
            // Kích hoạt gói mới khi chưa có gói nào còn hạn
            Instant startDate = now;
            Instant endDate = startDate.plus(packageToActivate.getDurationDays(), ChronoUnit.DAYS);

            MemberMembership newMembership = MemberMembership.builder()
                    .member(member)
                    .membershipPackage(packageToActivate)
                    .remainingSessions(packageToActivate.getTotalSessions())
                    .startDate(startDate)
                    .endDate(endDate)
                    .status(MembershipStatus.ACTIVE)
                    .build();

            membership = membershipRepository.save(newMembership);
            log.info("Kích hoạt mới gói {} cho hội viên {}. Số lượt: {}, Hạn đến: {}",
                    membership.getId(), member.getEmail(), membership.getRemainingSessions(), membership.getEndDate());
        }

        payment.setMembership(membership);
        return membership;
    }
}

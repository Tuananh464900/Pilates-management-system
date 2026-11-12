package vn.n06g12.pilates.membership.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.n06g12.pilates.booking.repository.BookingRepository;
import vn.n06g12.pilates.common.exception.BusinessException;
import vn.n06g12.pilates.common.exception.ResourceNotFoundException;
import vn.n06g12.pilates.membership.dto.ConfirmPaymentRequest;
import vn.n06g12.pilates.membership.dto.CreatePaymentRequest;
import vn.n06g12.pilates.membership.dto.PaymentResponse;
import vn.n06g12.pilates.membership.entity.*;
import vn.n06g12.pilates.membership.repository.MemberMembershipRepository;
import vn.n06g12.pilates.membership.repository.MembershipPackageRepository;
import vn.n06g12.pilates.membership.repository.PaymentRepository;
import vn.n06g12.pilates.user.entity.User;
import vn.n06g12.pilates.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MembershipPackageRepository packageRepository;
    private final MemberMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final MembershipService membershipService;
    private final BookingRepository bookingRepository;

    @Transactional
    public PaymentResponse createPayment(UUID memberId, CreatePaymentRequest request) {
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin hội viên"));

        MembershipPackage pkg = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói tập với ID: " + request.getPackageId()));

        if (pkg.getStatus() != PackageStatus.ACTIVE) {
            throw new BusinessException("Gói tập '" + pkg.getName() + "' hiện không hoạt động");
        }

        // Kiểm tra quy tắc: Nếu đang có gói active khác loại thì không cho tạo đơn mua gói mới
        Optional<MemberMembership> activeOpt = membershipRepository.findActiveMembership(memberId, MembershipStatus.ACTIVE, Instant.now());
        if (activeOpt.isPresent()) {
            MemberMembership current = activeOpt.get();
            if (!current.getMembershipPackage().getId().equals(pkg.getId())) {
                throw new BusinessException(
                        "Bạn đang có gói tập '" + current.getMembershipPackage().getName() +
                        "' còn hiệu lực (" + current.getRemainingSessions() + " lượt). " +
                        "Quy định chỉ cho phép gia hạn cùng loại gói khi gói hiện tại đang hoạt động!"
                );
            }
        }

        // Lỗi C: Kiểm tra nếu hội viên có bất kỳ gói cũ nào (kể cả EXHAUSTED) còn booking tương lai chưa diễn ra
        List<MemberMembership> renewableList = membershipRepository.findRenewableMembershipsWithLock(memberId, Instant.now());
        for (MemberMembership oldMem : renewableList) {
            if (!oldMem.getMembershipPackage().getId().equals(pkg.getId())) {
                long pending = bookingRepository.countPendingConfirmedBookings(oldMem.getId(), Instant.now());
                if (pending > 0) {
                    throw new BusinessException(
                            "Bạn vẫn còn " + pending + " buổi tập đã đặt thuộc gói cũ ('" +
                            oldMem.getMembershipPackage().getName() + "') chưa diễn ra có khả năng phát sinh hoàn lượt. " +
                            "Vui lòng hoàn thành buổi tập hoặc hủy đặt chỗ trước khi mua loại gói tập mới!"
                    );
                }
            }
        }

        // Tạo mã giao dịch duy nhất dạng TXN-yyyyMMddHHmmss-XXXX
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomSuffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        String transactionCode = "TXN-" + timeStr + "-" + randomSuffix;

        Payment payment = Payment.builder()
                .transactionCode(transactionCode)
                .member(member)
                .membershipPackage(pkg)
                .amount(pkg.getPrice())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.UNPAID)
                .note(request.getNote())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Tạo giao dịch thanh toán {} cho hội viên {} gói {}", transactionCode, member.getEmail(), pkg.getName());
        return PaymentResponse.from(saved);
    }

    /**
     * UC3.2 & UC3.3: Admin xác nhận đã thanh toán (PAID) và tự động kích hoạt/gia hạn gói nguyên tử
     */
    @Transactional
    public PaymentResponse confirmPayment(Long paymentId, ConfirmPaymentRequest request) {
        // UC3.2: Khóa bi quan bản ghi Payment để chống xác nhận trùng lặp đồng thời (Idempotent confirmation)
        Payment payment = paymentRepository.findByIdWithLock(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch với ID: " + paymentId));

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BusinessException("Giao dịch này đã được xác nhận thanh toán trước đó");
        }
        if (payment.getStatus() == PaymentStatus.CANCELED) {
            throw new BusinessException("Không thể xác nhận giao dịch đã bị hủy");
        }

        if (request != null && request.getPaymentMethod() != null) {
            payment.setPaymentMethod(request.getPaymentMethod());
        }
        if (request != null && request.getNote() != null && !request.getNote().isBlank()) {
            payment.setNote((payment.getNote() != null ? payment.getNote() + " | " : "") + request.getNote());
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(Instant.now());

        // Kích hoạt hoặc gia hạn gói tập nguyên tử
        membershipService.activateOrRenewMembership(payment);

        Payment updated = paymentRepository.save(payment);
        log.info("Xác nhận thanh toán thành công giao dịch {}, mã: {}", payment.getId(), payment.getTransactionCode());
        return PaymentResponse.from(updated);
    }

    @Transactional
    public PaymentResponse cancelPayment(Long paymentId, String reason) {
        // UC3.2: Khóa bi quan bản ghi Payment để chống xác nhận trùng lặp đồng thời (Idempotent confirmation)
        Payment payment = paymentRepository.findByIdWithLock(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch với ID: " + paymentId));

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BusinessException("Giao dịch đã thanh toán thành công, không thể hủy!");
        }

        payment.setStatus(PaymentStatus.CANCELED);
        if (reason != null && !reason.isBlank()) {
            payment.setNote((payment.getNote() != null ? payment.getNote() + " | Lý do hủy: " : "Lý do hủy: ") + reason);
        }

        Payment updated = paymentRepository.save(payment);
        return PaymentResponse.from(updated);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getMyPayments(UUID memberId, Pageable pageable) {
        return paymentRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(PaymentResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> adminGetPayments(PaymentStatus status, Pageable pageable) {
        if (status != null) {
            return paymentRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                    .map(PaymentResponse::from);
        }
        return paymentRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(PaymentResponse::from);
    }

    @Transactional(readOnly = true)
    public BigDecimal adminGetTotalRevenue() {
        return paymentRepository.calculateTotalPaidRevenue();
    }
}

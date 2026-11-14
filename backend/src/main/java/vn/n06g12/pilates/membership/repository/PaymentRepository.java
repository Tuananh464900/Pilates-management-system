package vn.n06g12.pilates.membership.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.n06g12.pilates.membership.entity.Payment;
import vn.n06g12.pilates.membership.entity.PaymentMethod;
import vn.n06g12.pilates.membership.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionCode(String transactionCode);

    /**
     * UC3.2: Khóa bi quan bản ghi Payment để chống xác nhận trùng lặp đồng thời (Idempotent confirmation)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdWithLock(@Param("id") Long id);

    Page<Payment> findByMemberIdOrderByCreatedAtDesc(UUID memberId, Pageable pageable);

    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);

    Page<Payment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID'")
    BigDecimal calculateTotalPaidRevenue();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.paymentMethod = :method")
    BigDecimal calculatePaidRevenueByMethod(@Param("method") PaymentMethod method);

    /**
     * UC5.4 & R5.5: Thống kê doanh thu theo khoảng thời gian / tháng
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND COALESCE(p.paidAt, p.createdAt) >= :from AND COALESCE(p.paidAt, p.createdAt) <= :to")
    BigDecimal calculatePaidRevenueBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.paymentMethod = :method AND COALESCE(p.paidAt, p.createdAt) >= :from AND COALESCE(p.paidAt, p.createdAt) <= :to")
    BigDecimal calculatePaidRevenueBetweenAndMethod(@Param("from") Instant from, @Param("to") Instant to, @Param("method") PaymentMethod method);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.membership.id = :membershipId AND p.status = 'PAID'")
    long countPaidPaymentsByMembership(@Param("membershipId") Long membershipId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.member.id = :memberId AND p.membershipPackage.id = :packageId AND p.status = 'PAID'")
    long countPaidPaymentsByMemberAndPackage(@Param("memberId") UUID memberId, @Param("packageId") Long packageId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") PaymentStatus status);
}

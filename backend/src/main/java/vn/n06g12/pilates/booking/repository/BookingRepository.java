package vn.n06g12.pilates.booking.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.n06g12.pilates.booking.entity.Booking;
import vn.n06g12.pilates.booking.entity.BookingStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT b.session.id, b.membership.id FROM Booking b WHERE b.id = :bookingId")
    List<Object[]> findSessionAndMembershipIdByBookingId(@Param("bookingId") Long bookingId);

    @Query("SELECT b.session.id FROM Booking b WHERE b.id = :bookingId")
    Optional<Long> findSessionIdByBookingId(@Param("bookingId") Long bookingId);

    boolean existsByMemberIdAndSessionIdAndStatus(UUID memberId, Long sessionId, BookingStatus status);

    Optional<Booking> findFirstByMemberIdAndSessionIdAndStatus(UUID memberId, Long sessionId, BookingStatus status);

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.session s " +
           "JOIN FETCH s.pilatesClass " +
           "JOIN FETCH s.room " +
           "JOIN FETCH s.trainer " +
           "WHERE b.member.id = :memberId " +
           "ORDER BY b.createdAt DESC")
    Page<Booking> findByMemberIdWithDetails(@Param("memberId") UUID memberId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.member m " +
           "LEFT JOIN FETCH m.memberProfile " +
           "WHERE b.session.id = :sessionId AND b.status = :status " +
           "ORDER BY b.createdAt ASC")
    List<Booking> findBySessionIdAndStatus(@Param("sessionId") Long sessionId, @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.member m " +
           "LEFT JOIN FETCH m.memberProfile " +
           "WHERE b.session.id = :sessionId " +
           "ORDER BY b.createdAt ASC")
    List<Booking> findBySessionIdWithMember(@Param("sessionId") Long sessionId);

    /**
     * Kiểm tra xem Member đã có booking nào khác ở trạng thái CONFIRMED
     * trùng khung thời gian với buổi tập mới hay không.
     */
    @Query("SELECT COUNT(b) FROM Booking b " +
           "JOIN b.session s " +
           "WHERE b.member.id = :memberId " +
           "AND b.status = vn.n06g12.pilates.booking.entity.BookingStatus.CONFIRMED " +
           "AND s.id <> :excludeSessionId " +
           "AND s.status = vn.n06g12.pilates.session.entity.SessionStatus.SCHEDULED " +
           "AND s.startTime < :newEndTime AND s.endTime > :newStartTime")
    long countConflictingBookings(@Param("memberId") UUID memberId,
                                  @Param("excludeSessionId") Long excludeSessionId,
                                  @Param("newStartTime") Instant newStartTime,
                                  @Param("newEndTime") Instant newEndTime);

    long countByStatus(BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b JOIN b.session s WHERE s.trainer.id = :trainerId AND b.status = :status")
    long countTrainerBookingsByStatus(@Param("trainerId") UUID trainerId, @Param("status") BookingStatus status);

    /**
     * Đếm số booking CONFIRMED của một gói tập cho các buổi tập chưa diễn ra (trong tương lai)
     */
    @Query("SELECT COUNT(b) FROM Booking b JOIN b.session s " +
           "WHERE b.membership.id = :membershipId " +
           "AND b.status = vn.n06g12.pilates.booking.entity.BookingStatus.CONFIRMED " +
           "AND s.startTime > :now")
    long countPendingConfirmedBookings(@Param("membershipId") Long membershipId, @Param("now") Instant now);

    /**
     * Lấy danh sách booking CONFIRMED theo session sắp xếp theo ID tăng dần (thống nhất thứ tự khóa)
     */
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.member m " +
           "LEFT JOIN FETCH m.memberProfile " +
           "WHERE b.session.id = :sessionId AND b.status = :status " +
           "ORDER BY b.id ASC")
    List<Booking> findBySessionIdAndStatusOrderByIdAsc(@Param("sessionId") Long sessionId, @Param("status") BookingStatus status);
}

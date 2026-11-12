package vn.n06g12.pilates.membership.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.n06g12.pilates.membership.entity.MemberMembership;
import vn.n06g12.pilates.membership.entity.MembershipStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberMembershipRepository extends JpaRepository<MemberMembership, Long> {

    @Query("SELECT m FROM MemberMembership m " +
           "JOIN FETCH m.membershipPackage " +
           "WHERE m.member.id = :memberId AND m.status = :status AND m.endDate > :now AND m.remainingSessions > 0")
    Optional<MemberMembership> findActiveMembership(@Param("memberId") UUID memberId,
                                                   @Param("status") MembershipStatus status,
                                                   @Param("now") Instant now);

    /**
     * UC4.2: Khóa bi quan gói tập của Member để chống race condition khi hội viên đặt nhiều buổi đồng thời
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MemberMembership m " +
           "WHERE m.member.id = :memberId AND m.status = :status AND m.endDate > :now AND m.remainingSessions > 0")
    Optional<MemberMembership> findActiveMembershipWithLock(@Param("memberId") UUID memberId,
                                                           @Param("status") MembershipStatus status,
                                                           @Param("now") Instant now);

    /**
     * Khóa bi quan gói tập đang ACTIVE của Member (bất kể remainingSessions) để cộng lượt hoàn an toàn
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MemberMembership m " +
           "JOIN FETCH m.membershipPackage " +
           "WHERE m.member.id = :memberId AND m.status = :status AND m.endDate > :now")
    Optional<MemberMembership> findCurrentActiveMembershipWithLock(@Param("memberId") UUID memberId,
                                                                   @Param("status") MembershipStatus status,
                                                                   @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MemberMembership m WHERE m.id = :id")
    Optional<MemberMembership> findByIdWithLock(@Param("id") Long id);

    /**
     * Tìm các gói tập hiện có của Member có thể áp dụng gia hạn (ACTIVE hoặc EXHAUSTED còn hạn) với khóa bi quan
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MemberMembership m " +
           "JOIN FETCH m.membershipPackage " +
           "WHERE m.member.id = :memberId AND m.endDate > :now AND (m.status = 'ACTIVE' OR m.status = 'EXHAUSTED') " +
           "ORDER BY m.status ASC, m.createdAt DESC")
    List<MemberMembership> findRenewableMembershipsWithLock(@Param("memberId") UUID memberId,
                                                          @Param("now") Instant now);

    List<MemberMembership> findByMemberIdOrderByCreatedAtDesc(UUID memberId);

    Page<MemberMembership> findByMemberIdOrderByCreatedAtDesc(UUID memberId, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT m.member.id) FROM MemberMembership m WHERE m.status = 'ACTIVE' AND m.endDate > :now AND m.remainingSessions > 0")
    long countDistinctActiveMemberships(@Param("now") Instant now);
}

package vn.n06g12.pilates.session.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.n06g12.pilates.session.entity.Session;
import vn.n06g12.pilates.session.entity.SessionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Session s WHERE s.id = :id")
    Optional<Session> findByIdWithLock(@Param("id") Long id);

    // Kiểm tra xung đột phòng: phòng đã có buổi khác chưa bị hủy trong khung giờ
    @Query("SELECT s FROM Session s WHERE s.room.id = :roomId " +
           "AND s.status != 'CANCELED' " +
           "AND (:excludeSessionId IS NULL OR s.id != :excludeSessionId) " +
           "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<Session> findConflictingRoomSessions(@Param("roomId") Long roomId,
                                             @Param("startTime") Instant startTime,
                                             @Param("endTime") Instant endTime,
                                             @Param("excludeSessionId") Long excludeSessionId);

    // Kiểm tra xung đột HLV: HLV đã có buổi dạy khác chưa bị hủy trong khung giờ
    @Query("SELECT s FROM Session s WHERE s.trainer.id = :trainerId " +
           "AND s.status != 'CANCELED' " +
           "AND (:excludeSessionId IS NULL OR s.id != :excludeSessionId) " +
           "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<Session> findConflictingTrainerSessions(@Param("trainerId") UUID trainerId,
                                                @Param("startTime") Instant startTime,
                                                @Param("endTime") Instant endTime,
                                                @Param("excludeSessionId") Long excludeSessionId);

    // Lấy thời khóa biểu theo khoảng thời gian
    @Query("SELECT s FROM Session s " +
           "JOIN FETCH s.pilatesClass " +
           "JOIN FETCH s.trainer " +
           "LEFT JOIN FETCH s.trainer.trainerProfile " +
           "JOIN FETCH s.room " +
           "WHERE s.startTime >= :from AND s.endTime <= :to " +
           "AND (:trainerId IS NULL OR s.trainer.id = :trainerId) " +
           "AND (:classId IS NULL OR s.pilatesClass.id = :classId) " +
           "ORDER BY s.startTime ASC")
    List<Session> findSchedule(@Param("from") Instant from,
                              @Param("to") Instant to,
                              @Param("trainerId") UUID trainerId,
                              @Param("classId") Long classId);

    // Admin tìm kiếm và phân trang
    @Query("SELECT s FROM Session s " +
           "JOIN FETCH s.pilatesClass " +
           "JOIN FETCH s.trainer " +
           "JOIN FETCH s.room " +
           "WHERE (:status IS NULL OR s.status = :status) " +
           "AND (:classId IS NULL OR s.pilatesClass.id = :classId) " +
           "AND (:trainerId IS NULL OR s.trainer.id = :trainerId) " +
           "ORDER BY s.startTime DESC")
    Page<Session> searchSessionsAdmin(@Param("status") SessionStatus status,
                                      @Param("classId") Long classId,
                                      @Param("trainerId") UUID trainerId,
                                      Pageable pageable);

    long countByTrainerIdAndStatus(UUID trainerId, SessionStatus status);

    long countByStatus(SessionStatus status);

    /**
     * Kiểm tra số buổi tập trong tương lai tại phòng có sức chứa hoặc số người đặt vượt quá sức chứa mới
     */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.room.id = :roomId " +
           "AND s.status = vn.n06g12.pilates.session.entity.SessionStatus.SCHEDULED " +
           "AND s.startTime > :now " +
           "AND (s.capacity > :newCapacity OR s.bookedCount > :newCapacity)")
    long countFutureSessionsExceedingCapacity(@Param("roomId") Long roomId,
                                             @Param("newCapacity") int newCapacity,
                                             @Param("now") Instant now);
}

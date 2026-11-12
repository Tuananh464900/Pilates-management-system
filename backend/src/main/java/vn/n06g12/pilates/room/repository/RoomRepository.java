package vn.n06g12.pilates.room.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.n06g12.pilates.room.entity.Room;
import vn.n06g12.pilates.room.entity.RoomStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdWithLock(@Param("id") Long id);

    Optional<Room> findByName(String name);

    boolean existsByName(String name);

    List<Room> findByStatus(RoomStatus status);

    Page<Room> findByStatus(RoomStatus status, Pageable pageable);

    @Query("SELECT r FROM Room r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:search IS NULL OR LOWER(r.name) LIKE :search)")
    Page<Room> searchRooms(@Param("status") RoomStatus status,
                           @Param("search") String search,
                           Pageable pageable);
}

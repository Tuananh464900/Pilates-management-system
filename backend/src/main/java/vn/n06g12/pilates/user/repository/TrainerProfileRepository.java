package vn.n06g12.pilates.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.n06g12.pilates.user.entity.TrainerProfile;

import java.util.UUID;

@Repository
public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, UUID> {

    @Query("SELECT t FROM TrainerProfile t WHERE " +
           "(:search IS NULL OR LOWER(t.fullName) LIKE :search " +
           "OR t.phone LIKE :search " +
           "OR LOWER(t.specialization) LIKE :search)")
    Page<TrainerProfile> searchTrainers(@Param("search") String search, Pageable pageable);
}

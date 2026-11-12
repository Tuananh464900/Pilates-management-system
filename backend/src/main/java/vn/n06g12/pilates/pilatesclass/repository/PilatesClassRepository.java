package vn.n06g12.pilates.pilatesclass.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.n06g12.pilates.pilatesclass.entity.ClassStatus;
import vn.n06g12.pilates.pilatesclass.entity.PilatesClass;

import java.util.List;
import java.util.Optional;

@Repository
public interface PilatesClassRepository extends JpaRepository<PilatesClass, Long> {

    Optional<PilatesClass> findByName(String name);

    boolean existsByName(String name);

    List<PilatesClass> findByStatus(ClassStatus status);

    Page<PilatesClass> findByStatus(ClassStatus status, Pageable pageable);

    @Query("SELECT c FROM PilatesClass c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:search IS NULL OR LOWER(c.name) LIKE :search)")
    Page<PilatesClass> searchClasses(@Param("status") ClassStatus status,
                                     @Param("search") String search,
                                     Pageable pageable);
}

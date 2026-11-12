package vn.n06g12.pilates.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.n06g12.pilates.user.entity.MemberProfile;

import java.util.UUID;

@Repository
public interface MemberProfileRepository extends JpaRepository<MemberProfile, UUID> {

    @Query("SELECT m FROM MemberProfile m WHERE " +
           "(:search IS NULL OR LOWER(m.fullName) LIKE :search " +
           "OR m.phone LIKE :search)")
    Page<MemberProfile> searchMembers(@Param("search") String search, Pageable pageable);
}

package vn.n06g12.pilates.membership.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.n06g12.pilates.membership.entity.MembershipPackage;
import vn.n06g12.pilates.membership.entity.PackageStatus;

import java.util.List;

@Repository
public interface MembershipPackageRepository extends JpaRepository<MembershipPackage, Long> {
    List<MembershipPackage> findByStatusOrderByIdAsc(PackageStatus status);
    Page<MembershipPackage> findByStatus(PackageStatus status, Pageable pageable);
    boolean existsByNameIgnoreCase(String name);
}

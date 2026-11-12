package vn.n06g12.pilates.membership.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.n06g12.pilates.common.exception.BusinessException;
import vn.n06g12.pilates.common.exception.ResourceNotFoundException;
import vn.n06g12.pilates.membership.dto.PackageRequest;
import vn.n06g12.pilates.membership.dto.PackageResponse;
import vn.n06g12.pilates.membership.entity.MembershipPackage;
import vn.n06g12.pilates.membership.entity.PackageStatus;
import vn.n06g12.pilates.membership.repository.MembershipPackageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipPackageService {

    private final MembershipPackageRepository packageRepository;

    @Transactional(readOnly = true)
    public List<PackageResponse> getAllActivePackages() {
        return packageRepository.findByStatusOrderByIdAsc(PackageStatus.ACTIVE)
                .stream()
                .map(PackageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PackageResponse> adminGetAllPackages(Pageable pageable) {
        return packageRepository.findAll(pageable)
                .map(PackageResponse::from);
    }

    @Transactional(readOnly = true)
    public PackageResponse getPackageById(Long id) {
        MembershipPackage p = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói tập với ID: " + id));
        return PackageResponse.from(p);
    }

    @Transactional
    public PackageResponse createPackage(PackageRequest request) {
        if (packageRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BusinessException("Gói tập với tên '" + request.getName() + "' đã tồn tại");
        }

        MembershipPackage p = MembershipPackage.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .totalSessions(request.getTotalSessions())
                .durationDays(request.getDurationDays())
                .status(request.getStatus() != null ? request.getStatus() : PackageStatus.ACTIVE)
                .build();

        MembershipPackage saved = packageRepository.save(p);
        return PackageResponse.from(saved);
    }

    @Transactional
    public PackageResponse updatePackage(Long id, PackageRequest request) {
        MembershipPackage p = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói tập với ID: " + id));

        p.setName(request.getName().trim());
        p.setDescription(request.getDescription());
        p.setPrice(request.getPrice());
        p.setTotalSessions(request.getTotalSessions());
        p.setDurationDays(request.getDurationDays());
        if (request.getStatus() != null) {
            p.setStatus(request.getStatus());
        }

        MembershipPackage updated = packageRepository.save(p);
        return PackageResponse.from(updated);
    }

    @Transactional
    public PackageResponse togglePackageStatus(Long id) {
        MembershipPackage p = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói tập với ID: " + id));

        p.setStatus(p.getStatus() == PackageStatus.ACTIVE ? PackageStatus.INACTIVE : PackageStatus.ACTIVE);
        MembershipPackage saved = packageRepository.save(p);
        return PackageResponse.from(saved);
    }
}

package vn.n06g12.pilates.pilatesclass.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.n06g12.pilates.common.exception.BusinessException;
import vn.n06g12.pilates.common.exception.ResourceNotFoundException;
import vn.n06g12.pilates.pilatesclass.dto.ClassResponse;
import vn.n06g12.pilates.pilatesclass.dto.CreateClassRequest;
import vn.n06g12.pilates.pilatesclass.dto.UpdateClassRequest;
import vn.n06g12.pilates.pilatesclass.entity.ClassStatus;
import vn.n06g12.pilates.pilatesclass.entity.PilatesClass;
import vn.n06g12.pilates.pilatesclass.repository.PilatesClassRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PilatesClassService {

    private final PilatesClassRepository classRepository;

    @Transactional(readOnly = true)
    public List<ClassResponse> getActiveClasses() {
        return classRepository.findByStatus(ClassStatus.ACTIVE).stream()
                .map(ClassResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ClassResponse> searchClasses(ClassStatus status, String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return classRepository.searchClasses(status, pattern, pageable)
                    .map(ClassResponse::fromEntity);
        }
        if (status != null) {
            return classRepository.findByStatus(status, pageable)
                    .map(ClassResponse::fromEntity);
        }
        return classRepository.findAll(pageable)
                .map(ClassResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ClassResponse getClassById(Long id) {
        PilatesClass pilatesClass = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp Pilates với ID: " + id));
        return ClassResponse.fromEntity(pilatesClass);
    }

    @Transactional
    public ClassResponse createClass(CreateClassRequest request) {
        if (classRepository.existsByName(request.getName().trim())) {
            throw new BusinessException("Tên lớp Pilates đã tồn tại: " + request.getName());
        }

        PilatesClass pilatesClass = PilatesClass.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .defaultCapacity(request.getDefaultCapacity())
                .level(request.getLevel())
                .status(ClassStatus.ACTIVE)
                .build();

        PilatesClass saved = classRepository.save(pilatesClass);
        log.info("Admin đã tạo lớp Pilates mới: {} (ID: {})", saved.getName(), saved.getId());
        return ClassResponse.fromEntity(saved);
    }

    @Transactional
    public ClassResponse updateClass(Long id, UpdateClassRequest request) {
        PilatesClass pilatesClass = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp Pilates với ID: " + id));

        // Kiểm tra tên trùng với lớp khác
        classRepository.findByName(request.getName().trim()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("Tên lớp Pilates đã được sử dụng bởi lớp khác: " + request.getName());
            }
        });

        pilatesClass.setName(request.getName().trim());
        pilatesClass.setDescription(request.getDescription());
        pilatesClass.setDefaultCapacity(request.getDefaultCapacity());
        pilatesClass.setLevel(request.getLevel());
        if (request.getStatus() != null) {
            pilatesClass.setStatus(request.getStatus());
        }

        PilatesClass saved = classRepository.save(pilatesClass);
        log.info("Admin đã cập nhật lớp Pilates: {} (ID: {})", saved.getName(), saved.getId());
        return ClassResponse.fromEntity(saved);
    }

    @Transactional
    public ClassResponse updateClassStatus(Long id, ClassStatus status) {
        PilatesClass pilatesClass = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp Pilates với ID: " + id));

        pilatesClass.setStatus(status);
        PilatesClass saved = classRepository.save(pilatesClass);
        log.info("Admin đã đổi trạng thái lớp Pilates {} thành {}", saved.getName(), status);
        return ClassResponse.fromEntity(saved);
    }
}

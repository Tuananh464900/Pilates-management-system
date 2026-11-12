package vn.n06g12.pilates.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.n06g12.pilates.common.exception.BusinessException;
import vn.n06g12.pilates.common.exception.ResourceNotFoundException;
import vn.n06g12.pilates.user.dto.MemberProfileResponse;
import vn.n06g12.pilates.user.dto.PublicTrainerResponse;
import vn.n06g12.pilates.user.dto.TrainerProfileResponse;
import vn.n06g12.pilates.user.dto.UpdateProfileRequest;
import vn.n06g12.pilates.user.entity.MemberProfile;
import vn.n06g12.pilates.user.entity.Role;
import vn.n06g12.pilates.user.entity.TrainerProfile;
import vn.n06g12.pilates.user.entity.User;
import vn.n06g12.pilates.user.repository.MemberProfileRepository;
import vn.n06g12.pilates.user.repository.TrainerProfileRepository;
import vn.n06g12.pilates.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final TrainerProfileRepository trainerProfileRepository;

    /**
     * UC1.3: Xem thông tin cá nhân của người dùng hiện tại
     */
    @Transactional(readOnly = true)
    public Object getMyProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));

        if (user.getRole() == Role.MEMBER) {
            MemberProfile profile = memberProfileRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ học viên"));
            return MemberProfileResponse.fromEntity(profile);
        } else if (user.getRole() == Role.TRAINER) {
            TrainerProfile profile = trainerProfileRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ huấn luyện viên"));
            return TrainerProfileResponse.fromEntity(profile);
        } else {
            // ADMIN
            return MemberProfileResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName("Quản trị viên")
                    .status(user.getStatus())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }

    /**
     * UC1.3: Cập nhật thông tin cá nhân.
     * Quy tắc: Người dùng KHÔNG được tự thay đổi role hay status.
     */
    @Transactional
    public Object updateMyProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản"));

        if (user.getRole() == Role.MEMBER) {
            MemberProfile profile = memberProfileRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ học viên"));
            
            profile.setFullName(request.getFullName());
            profile.setPhone(request.getPhone());
            if (request.getGender() != null) profile.setGender(request.getGender());
            if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
            if (request.getAddress() != null) profile.setAddress(request.getAddress());

            memberProfileRepository.save(profile);
            log.info("Học viên {} đã cập nhật hồ sơ cá nhân", user.getEmail());
            return MemberProfileResponse.fromEntity(profile);
        } else if (user.getRole() == Role.TRAINER) {
            TrainerProfile profile = trainerProfileRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ huấn luyện viên"));

            profile.setFullName(request.getFullName());
            profile.setPhone(request.getPhone());
            if (request.getSpecialization() != null) profile.setSpecialization(request.getSpecialization());
            if (request.getBio() != null) profile.setBio(request.getBio());

            trainerProfileRepository.save(profile);
            log.info("HLV {} đã cập nhật hồ sơ cá nhân", user.getEmail());
            return TrainerProfileResponse.fromEntity(profile);
        } else {
            throw new BusinessException("Hồ sơ quản trị viên không thể chỉnh sửa qua giao diện này");
        }
    }

    /**
     * UC1.6: Admin tra cứu danh sách học viên
     */
    @Transactional(readOnly = true)
    public Page<MemberProfileResponse> getAllMembers(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return memberProfileRepository.searchMembers(pattern, pageable)
                    .map(MemberProfileResponse::fromEntity);
        }
        return memberProfileRepository.findAll(pageable)
                .map(MemberProfileResponse::fromEntity);
    }

    /**
     * UC1.6: Admin xem chi tiết hồ sơ học viên
     */
    @Transactional(readOnly = true)
    public MemberProfileResponse getMemberById(UUID memberId) {
        MemberProfile profile = memberProfileRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ học viên với ID: " + memberId));
        return MemberProfileResponse.fromEntity(profile);
    }

    /**
     * UC1.6: Admin cập nhật thông tin hồ sơ học viên
     */
    @Transactional
    public MemberProfileResponse updateMemberByAdmin(UUID memberId, UpdateProfileRequest request) {
        MemberProfile profile = memberProfileRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ học viên"));

        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());

        memberProfileRepository.save(profile);
        return MemberProfileResponse.fromEntity(profile);
    }

    /**
     * UC1.6: Admin tra cứu danh sách HLV
     */
    @Transactional(readOnly = true)
    public Page<TrainerProfileResponse> getAllTrainers(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return trainerProfileRepository.searchTrainers(pattern, pageable)
                    .map(TrainerProfileResponse::fromEntity);
        }
        return trainerProfileRepository.findAll(pageable)
                .map(TrainerProfileResponse::fromEntity);
    }

    /**
     * UC1.6: Admin cập nhật hồ sơ HLV và chuyên môn
     */
    @Transactional
    public TrainerProfileResponse updateTrainerByAdmin(UUID trainerId, UpdateProfileRequest request) {
        TrainerProfile profile = trainerProfileRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ HLV"));

        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        if (request.getSpecialization() != null) profile.setSpecialization(request.getSpecialization());
        if (request.getBio() != null) profile.setBio(request.getBio());

        trainerProfileRepository.save(profile);
        return TrainerProfileResponse.fromEntity(profile);
    }

    /**
     * Lấy danh sách Huấn luyện viên công khai phục vụ xem thời khóa biểu (ẩn email và số điện thoại)
     */
    @Transactional(readOnly = true)
    public List<PublicTrainerResponse> getActiveTrainers() {
        return trainerProfileRepository.findAll().stream()
                .map(PublicTrainerResponse::fromEntity)
                .collect(Collectors.toList());
    }
}

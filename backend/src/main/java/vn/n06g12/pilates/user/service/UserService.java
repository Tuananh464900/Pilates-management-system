package vn.n06g12.pilates.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.n06g12.pilates.common.exception.BusinessException;
import vn.n06g12.pilates.common.exception.ResourceNotFoundException;
import vn.n06g12.pilates.user.dto.UserResponse;
import vn.n06g12.pilates.user.entity.Role;
import vn.n06g12.pilates.user.entity.User;
import vn.n06g12.pilates.user.entity.UserStatus;
import vn.n06g12.pilates.user.repository.UserRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserDetails(UUID id) {
        User user = findById(id);
        return UserResponse.fromUser(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Role role, UserStatus status, String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return userRepository.searchUsers(role, status, pattern, pageable)
                    .map(UserResponse::fromUser);
        }
        if (role != null && status != null) {
            return userRepository.findByRoleAndStatus(role, status, pageable)
                    .map(UserResponse::fromUser);
        }
        if (role != null) {
            return userRepository.findByRole(role, pageable)
                    .map(UserResponse::fromUser);
        }
        if (status != null) {
            return userRepository.findByStatus(status, pageable)
                    .map(UserResponse::fromUser);
        }
        return userRepository.findAll(pageable)
                .map(UserResponse::fromUser);
    }

    @Transactional
    public UserResponse updateUserStatus(UUID currentAdminId, UUID targetUserId, UserStatus newStatus) {
        if (currentAdminId.equals(targetUserId)) {
            throw new BusinessException("Quản trị viên không thể tự khóa hoặc thay đổi trạng thái của chính mình");
        }

        User targetUser = findById(targetUserId);
        targetUser.setStatus(newStatus);
        User saved = userRepository.save(targetUser);

        log.info("Admin {} đã cập nhật trạng thái user {} thành {}", currentAdminId, targetUserId, newStatus);
        return UserResponse.fromUser(saved);
    }
}

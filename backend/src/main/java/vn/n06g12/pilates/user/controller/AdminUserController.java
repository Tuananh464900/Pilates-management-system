package vn.n06g12.pilates.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.n06g12.pilates.common.response.ApiResponse;
import vn.n06g12.pilates.common.response.PageResponse;
import vn.n06g12.pilates.security.UserPrincipal;
import vn.n06g12.pilates.user.dto.*;
import vn.n06g12.pilates.user.entity.Role;
import vn.n06g12.pilates.user.entity.UserStatus;
import vn.n06g12.pilates.user.service.AuthService;
import vn.n06g12.pilates.user.service.ProfileService;
import vn.n06g12.pilates.user.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Quản trị Tài khoản & Hồ sơ (UC1.5, UC1.6)", description = "Quản lý tài khoản người dùng, tạo HLV, khóa/mở khóa tài khoản, quản lý hồ sơ")
public class AdminUserController {

    private final UserService userService;
    private final AuthService authService;
    private final ProfileService profileService;

    // UC1.5: Danh sách tài khoản
    @GetMapping("/users")
    @Operation(summary = "UC1.5 - Danh sách tài khoản người dùng", description = "Admin xem và tìm kiếm danh sách tài khoản, lọc theo role, status")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<UserResponse> page = PageResponse.from(userService.getAllUsers(role, status, search, pageable));
        return ResponseEntity.ok(ApiResponse.ok(page, "Lấy danh sách người dùng thành công"));
    }

    // UC1.5: Admin tạo tài khoản Trainer
    @PostMapping("/users/trainers")
    @Operation(summary = "UC1.5 - Tạo tài khoản Huấn luyện viên", description = "Admin tạo tài khoản cho Huấn luyện viên")
    public ResponseEntity<ApiResponse<TrainerProfileResponse>> createTrainer(@Valid @RequestBody CreateTrainerRequest request) {
        TrainerProfileResponse response = authService.createTrainer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Tạo tài khoản Huấn luyện viên thành công"));
    }

    // UC1.5: Khóa / mở khóa tài khoản
    @PatchMapping("/users/{id}/status")
    @Operation(summary = "UC1.5 - Khóa / Mở khóa tài khoản", description = "Cập nhật trạng thái ACTIVE hoặc LOCKED cho tài khoản")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        UserResponse response = userService.updateUserStatus(principal.getId(), id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật trạng thái tài khoản thành công"));
    }

    // UC1.6: Danh sách hồ sơ Học viên
    @GetMapping("/members")
    @Operation(summary = "UC1.6 - Tra cứu hồ sơ Học viên", description = "Tìm kiếm hồ sơ học viên theo tên hoặc số điện thoại")
    public ResponseEntity<ApiResponse<PageResponse<MemberProfileResponse>>> getAllMembers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<MemberProfileResponse> page = PageResponse.from(profileService.getAllMembers(search, pageable));
        return ResponseEntity.ok(ApiResponse.ok(page, "Lấy danh sách hồ sơ Học viên thành công"));
    }

    // UC1.6: Xem chi tiết hồ sơ Học viên
    @GetMapping("/members/{id}")
    @Operation(summary = "UC1.6 - Xem chi tiết hồ sơ Học viên", description = "Xem chi tiết thông tin hồ sơ của một học viên")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMemberById(@PathVariable UUID id) {
        MemberProfileResponse response = profileService.getMemberById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy thông tin hồ sơ Học viên thành công"));
    }

    // UC1.6: Cập nhật hồ sơ Học viên
    @PutMapping("/members/{id}")
    @Operation(summary = "UC1.6 - Cập nhật hồ sơ Học viên", description = "Admin cập nhật thông tin hồ sơ học viên")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateMember(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequest request) {
        MemberProfileResponse response = profileService.updateMemberByAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật hồ sơ Học viên thành công"));
    }

    // UC1.6: Danh sách hồ sơ HLV
    @GetMapping("/trainers")
    @Operation(summary = "UC1.6 - Tra cứu hồ sơ Huấn luyện viên", description = "Tìm kiếm hồ sơ HLV theo tên, SĐT hoặc chuyên môn")
    public ResponseEntity<ApiResponse<PageResponse<TrainerProfileResponse>>> getAllTrainers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<TrainerProfileResponse> page = PageResponse.from(profileService.getAllTrainers(search, pageable));
        return ResponseEntity.ok(ApiResponse.ok(page, "Lấy danh sách hồ sơ Huấn luyện viên thành công"));
    }

    // UC1.6: Cập nhật hồ sơ HLV
    @PutMapping("/trainers/{id}")
    @Operation(summary = "UC1.6 - Cập nhật hồ sơ Huấn luyện viên", description = "Admin cập nhật thông tin và chuyên môn của HLV")
    public ResponseEntity<ApiResponse<TrainerProfileResponse>> updateTrainer(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequest request) {
        TrainerProfileResponse response = profileService.updateTrainerByAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật hồ sơ Huấn luyện viên thành công"));
    }
}

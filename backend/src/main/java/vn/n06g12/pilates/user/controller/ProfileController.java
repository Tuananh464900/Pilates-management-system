package vn.n06g12.pilates.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.n06g12.pilates.common.response.ApiResponse;
import vn.n06g12.pilates.security.UserPrincipal;
import vn.n06g12.pilates.user.dto.UpdateProfileRequest;
import vn.n06g12.pilates.user.service.ProfileService;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Hồ sơ cá nhân (UC1.3)", description = "Xem và chỉnh sửa hồ sơ cá nhân của Học viên và Huấn luyện viên")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    @Operation(summary = "UC1.3 - Xem hồ sơ cá nhân", description = "Người dùng xem hồ sơ cá nhân của chính mình")
    public ResponseEntity<ApiResponse<Object>> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        Object profile = profileService.getMyProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(profile, "Lấy hồ sơ cá nhân thành công"));
    }

    @PutMapping("/me")
    @Operation(summary = "UC1.3 - Cập nhật hồ sơ cá nhân", description = "Người dùng cập nhật thông tin được phép (không được tự sửa role/status)")
    public ResponseEntity<ApiResponse<Object>> updateMyProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                               @Valid @RequestBody UpdateProfileRequest request) {
        Object updatedProfile = profileService.updateMyProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(updatedProfile, "Cập nhật hồ sơ cá nhân thành công"));
    }
}

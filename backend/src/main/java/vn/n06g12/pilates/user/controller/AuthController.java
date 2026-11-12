package vn.n06g12.pilates.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.n06g12.pilates.common.response.ApiResponse;
import vn.n06g12.pilates.security.UserPrincipal;
import vn.n06g12.pilates.user.dto.ChangePasswordRequest;
import vn.n06g12.pilates.user.dto.PublicTrainerResponse;
import vn.n06g12.pilates.user.dto.RegisterMemberRequest;
import vn.n06g12.pilates.user.dto.UserResponse;
import vn.n06g12.pilates.user.service.AuthService;
import vn.n06g12.pilates.user.service.ProfileService;
import vn.n06g12.pilates.user.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Xác thực & Tài khoản (UC1)", description = "Các API xác thực, đăng ký học viên, đổi mật khẩu và phiên đăng nhập")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final ProfileService profileService;

    public AuthController(AuthService authService, UserService userService, ProfileService profileService) {
        this.authService = authService;
        this.userService = userService;
        this.profileService = profileService;
    }

    @PostMapping("/register-member")
    @Operation(summary = "UC1.1 - Đăng ký Học viên", description = "Đăng ký tài khoản công khai, luôn luôn gán vai trò MEMBER")
    public ResponseEntity<ApiResponse<UserResponse>> registerMember(@Valid @RequestBody RegisterMemberRequest request) {
        UserResponse response = authService.registerMember(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Đăng ký tài khoản Học viên thành công"));
    }

    @GetMapping("/me")
    @Operation(summary = "UC1.2 - Lấy thông tin tài khoản hiện tại", description = "Lấy thông tin phiên làm việc, role và hồ sơ cá nhân")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse user = userService.getUserDetails(principal.getId());
        Object profile = profileService.getMyProfile(principal.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("profile", profile);

        return ResponseEntity.ok(ApiResponse.ok(data, "Lấy thông tin tài khoản thành công"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "UC1.4 - Đổi mật khẩu", description = "Thay đổi mật khẩu cho tài khoản đang đăng nhập")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                                            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Đổi mật khẩu thành công"));
    }

    @PostMapping("/login-mock")
    @Operation(summary = "Hỗ trợ Test / Chạy Local - Lấy Token đăng nhập", description = "Sinh token JWT hợp lệ để chạy thử nghiệm và kiểm thử")
    public ResponseEntity<ApiResponse<Map<String, String>>> loginMock(@RequestParam String email) {
        if (!authService.isMockLoginAllowed()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Tính năng login-mock đã bị vô hiệu hóa hoàn toàn trên môi trường production vì lý do an toàn bảo mật"));
        }
        String token = authService.generateTestTokenForUser(email);
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("tokenType", "Bearer");
        return ResponseEntity.ok(ApiResponse.ok(data, "Đăng nhập thành công"));
    }

    @GetMapping("/trainers")
    @Operation(summary = "Danh sách Huấn luyện viên công khai", description = "Lấy danh sách HLV phục vụ xem thời khóa biểu (ẩn email và số điện thoại)")
    public ResponseEntity<ApiResponse<List<PublicTrainerResponse>>> getPublicTrainers() {
        List<PublicTrainerResponse> list = profileService.getActiveTrainers();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách HLV thành công"));
    }
}

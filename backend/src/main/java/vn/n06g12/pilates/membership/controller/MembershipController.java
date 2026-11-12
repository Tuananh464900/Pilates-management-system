package vn.n06g12.pilates.membership.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.n06g12.pilates.common.response.ApiResponse;
import vn.n06g12.pilates.common.response.PageResponse;
import vn.n06g12.pilates.membership.dto.MemberMembershipResponse;
import vn.n06g12.pilates.membership.service.MembershipService;
import vn.n06g12.pilates.security.UserPrincipal;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
@Tag(name = "Gói tập hội viên (UC3.3 & UC3.4)", description = "Xem gói tập hiện tại và lịch sử gói tập")
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/my-active")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "UC3.4 - Xem gói tập hiện tại & Cảnh báo", description = "Hội viên xem gói tập đang ACTIVE (kèm cờ cảnh báo còn <= 7 ngày hoặc <= 2 lượt)")
    public ResponseEntity<ApiResponse<MemberMembershipResponse>> getMyActiveMembership(
            @AuthenticationPrincipal UserPrincipal principal) {
        Optional<MemberMembershipResponse> response = membershipService.getActiveMembership(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(
                response.orElse(null),
                response.isPresent() ? "Lấy thông tin gói tập hiện tại thành công" : "Hội viên chưa có gói tập nào đang hoạt động"
        ));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "UC3.4 - Lịch sử gói tập của hội viên", description = "Hội viên xem danh sách các gói tập đã từng kích hoạt")
    public ResponseEntity<ApiResponse<PageResponse<MemberMembershipResponse>>> getMyMembershipHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<MemberMembershipResponse> page = PageResponse.from(membershipService.getMembershipHistory(principal.getId(), pageable));
        return ResponseEntity.ok(ApiResponse.ok(page, "Lấy lịch sử gói tập thành công"));
    }
}

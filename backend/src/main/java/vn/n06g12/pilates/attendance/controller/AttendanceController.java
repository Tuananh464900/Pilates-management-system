package vn.n06g12.pilates.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.n06g12.pilates.attendance.dto.AttendanceRecordResponse;
import vn.n06g12.pilates.attendance.dto.MarkAttendanceRequest;
import vn.n06g12.pilates.attendance.service.AttendanceService;
import vn.n06g12.pilates.common.response.ApiResponse;
import vn.n06g12.pilates.security.UserPrincipal;
import vn.n06g12.pilates.user.entity.Role;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Điểm danh buổi tập (UC5.1)", description = "Trainer và Admin điểm danh học viên có mặt/vắng mặt")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @Operation(summary = "UC5.1 - Lấy danh sách điểm danh buổi tập", description = "Xem danh sách học viên và trạng thái điểm danh của buổi tập")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getSessionAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long sessionId) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        List<AttendanceRecordResponse> records = attendanceService.getSessionAttendance(principal.getId(), isAdmin, sessionId);
        return ResponseEntity.ok(ApiResponse.ok(records, "Lấy danh sách điểm danh thành công"));
    }

    @PostMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @Operation(summary = "UC5.1 - Điểm danh học viên", description = "Ghi nhận Có mặt (ATTENDED) hoặc Vắng mặt (ABSENT - không hoàn lượt) từ lúc buổi tập bắt đầu")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> markAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long sessionId,
            @Valid @RequestBody MarkAttendanceRequest request) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        List<AttendanceRecordResponse> records = attendanceService.markAttendance(principal.getId(), isAdmin, sessionId, request);
        return ResponseEntity.ok(ApiResponse.ok(records, "Ghi nhận điểm danh thành công"));
    }
}

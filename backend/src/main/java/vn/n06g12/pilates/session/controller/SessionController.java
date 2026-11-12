package vn.n06g12.pilates.session.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.n06g12.pilates.common.response.ApiResponse;
import vn.n06g12.pilates.common.response.PageResponse;
import vn.n06g12.pilates.session.dto.CancelSessionRequest;
import vn.n06g12.pilates.session.dto.CreateSessionRequest;
import vn.n06g12.pilates.session.dto.SessionResponse;
import vn.n06g12.pilates.session.dto.UpdateSessionRequest;
import vn.n06g12.pilates.session.entity.SessionStatus;
import vn.n06g12.pilates.session.service.SessionService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Buổi tập & Lịch (UC2.3 - UC2.5)", description = "Quản lý buổi tập, xem thời khóa biểu và hủy buổi tập")
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/api/v1/sessions/schedule")
    @Operation(summary = "UC2.4 - Xem lịch tập / Lịch dạy", description = "Xem thời khóa biểu các buổi tập theo khoảng thời gian, hỗ trợ lọc theo lớp và HLV")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(required = false) UUID trainerId,
            @RequestParam(required = false) Long classId) {

        Instant from = fromDate != null ? fromDate : Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant to = toDate != null ? toDate : from.plus(7, ChronoUnit.DAYS);

        List<SessionResponse> schedule = sessionService.getSchedule(from, to, trainerId, classId);
        return ResponseEntity.ok(ApiResponse.ok(schedule, "Lấy thời khóa biểu thành công"));
    }

    @GetMapping("/api/v1/sessions/{id}")
    @Operation(summary = "UC2.4 - Chi tiết buổi tập", description = "Xem chi tiết một buổi tập theo ID")
    public ResponseEntity<ApiResponse<SessionResponse>> getSessionById(@PathVariable Long id) {
        SessionResponse response = sessionService.getSessionById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy thông tin buổi tập thành công"));
    }

    @GetMapping("/api/v1/admin/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC2.3 - Admin danh sách buổi tập", description = "Admin tra cứu danh sách toàn bộ buổi tập có phân trang")
    public ResponseEntity<ApiResponse<PageResponse<SessionResponse>>> adminSearchSessions(
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) UUID trainerId,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<SessionResponse> page = PageResponse.from(sessionService.adminSearchSessions(status, classId, trainerId, pageable));
        return ResponseEntity.ok(ApiResponse.ok(page, "Lấy danh sách buổi tập thành công"));
    }

    @PostMapping("/api/v1/admin/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC2.3 - Tạo buổi tập mới", description = "Admin tạo buổi tập mới với lớp, phòng, HLV, ngày giờ (kiểm tra chống trùng lịch)")
    public ResponseEntity<ApiResponse<SessionResponse>> createSession(@Valid @RequestBody CreateSessionRequest request) {
        SessionResponse response = sessionService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Tạo buổi tập mới thành công"));
    }

    @PutMapping("/api/v1/admin/sessions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC2.3 - Cập nhật buổi tập", description = "Admin cập nhật buổi tập (từ chối sửa nếu buổi tập đã có người đăng ký)")
    public ResponseEntity<ApiResponse<SessionResponse>> updateSession(@PathVariable Long id,
                                                                     @Valid @RequestBody UpdateSessionRequest request) {
        SessionResponse response = sessionService.updateSession(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật buổi tập thành công"));
    }

    @PostMapping("/api/v1/admin/sessions/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC2.5 - Hủy buổi tập", description = "Admin hủy buổi tập chưa bắt đầu (hủy booking còn hiệu lực và hoàn trả lượt)")
    public ResponseEntity<ApiResponse<SessionResponse>> cancelSession(@PathVariable Long id,
                                                                     @Valid @RequestBody CancelSessionRequest request) {
        SessionResponse response = sessionService.cancelSession(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Hủy buổi tập thành công"));
    }
}

package vn.n06g12.pilates.booking.controller;

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
import vn.n06g12.pilates.booking.dto.BookingResponse;
import vn.n06g12.pilates.booking.dto.CancelBookingRequest;
import vn.n06g12.pilates.booking.dto.CreateBookingRequest;
import vn.n06g12.pilates.booking.service.BookingService;
import vn.n06g12.pilates.common.response.ApiResponse;
import vn.n06g12.pilates.common.response.PageResponse;
import vn.n06g12.pilates.security.UserPrincipal;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Đặt chỗ & Hủy buổi (UC4.1 - UC4.3)", description = "Đăng ký buổi tập, hủy đăng ký với quy tắc biên 2 giờ và danh sách học viên")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/api/v1/bookings")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "UC4.1 - Đăng ký buổi tập", description = "Member đặt chỗ buổi tập, kiểm tra sức chứa, gói active và trừ 1 lượt nguyên tử")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(principal.getId(), request.getSessionId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Đặt chỗ buổi tập thành công"));
    }

    @PostMapping("/api/v1/bookings/{id}/cancel")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "UC4.2 - Hủy đăng ký buổi tập", description = "Member hủy booking: Trước >= 2h hoàn 1 lượt về đúng gói, trong 2h không hoàn lượt, sau giờ tập không được hủy")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest request) {
        String reason = request != null ? request.getReason() : null;
        BookingResponse response = bookingService.cancelBooking(principal.getId(), id, reason);
        return ResponseEntity.ok(ApiResponse.ok(response, response.getRefunded() 
                ? "Hủy buổi tập thành công và đã hoàn trả 1 lượt về gói tập của bạn" 
                : "Hủy buổi tập thành công (Hủy trong vòng 2 giờ trước giờ tập nên không được hoàn lượt)"));
    }

    @GetMapping("/api/v1/bookings/my-history")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "UC4.3 - Lịch sử đặt chỗ của hội viên", description = "Member xem danh sách các buổi tập đã đăng ký (phân trang)")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<BookingResponse> page = PageResponse.from(bookingService.getMyBookings(principal.getId(), pageable));
        return ResponseEntity.ok(ApiResponse.ok(page, "Lấy lịch sử đăng ký thành công"));
    }

    @GetMapping("/api/v1/trainer/sessions/{sessionId}/bookings")
    @PreAuthorize("hasRole('TRAINER')")
    @Operation(summary = "UC4.3 - Trainer xem học viên buổi tập", description = "Huấn luyện viên xem danh sách học viên đăng ký của buổi do mình phụ trách")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getTrainerSessionBookings(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long sessionId) {
        List<BookingResponse> list = bookingService.getTrainerSessionBookings(principal.getId(), sessionId);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách học viên thành công"));
    }

    @GetMapping("/api/v1/admin/sessions/{sessionId}/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC4.3 - Admin xem học viên buổi tập", description = "Admin xem danh sách học viên đăng ký của buổi tập bất kỳ")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAdminSessionBookings(
            @PathVariable Long sessionId) {
        List<BookingResponse> list = bookingService.getAdminSessionBookings(sessionId);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách học viên thành công"));
    }
}

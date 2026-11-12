package vn.n06g12.pilates.membership.controller;

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
import vn.n06g12.pilates.membership.dto.ConfirmPaymentRequest;
import vn.n06g12.pilates.membership.dto.CreatePaymentRequest;
import vn.n06g12.pilates.membership.dto.PaymentResponse;
import vn.n06g12.pilates.membership.entity.PaymentStatus;
import vn.n06g12.pilates.membership.service.PaymentService;
import vn.n06g12.pilates.security.UserPrincipal;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Thanh toán (UC3.2 & UC3.5)", description = "Tạo đơn mua gói, xác nhận thanh toán và lịch sử giao dịch")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/v1/payments")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "UC3.2 - Tạo giao dịch thanh toán", description = "Member tạo đơn mua gói tập ở trạng thái UNPAID")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Tạo đơn thanh toán thành công"));
    }

    @GetMapping("/api/v1/payments/my-history")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "UC3.5 - Lịch sử thanh toán của hội viên", description = "Hội viên xem danh sách các giao dịch thanh toán của chính mình")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getMyPayments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<PaymentResponse> page = PageResponse.from(paymentService.getMyPayments(principal.getId(), pageable));
        return ResponseEntity.ok(ApiResponse.ok(page, "Lấy lịch sử thanh toán thành công"));
    }

    @GetMapping("/api/v1/admin/payments")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC3.5 - Admin tra cứu giao dịch thanh toán", description = "Admin xem toàn bộ giao dịch có lọc theo trạng thái và phân trang")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> adminGetPayments(
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<PaymentResponse> page = PageResponse.from(paymentService.adminGetPayments(status, pageable));
        return ResponseEntity.ok(ApiResponse.ok(page, "Lấy danh sách giao dịch thành công"));
    }

    @PostMapping("/api/v1/admin/payments/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC3.2 & UC3.3 - Admin xác nhận thanh toán", description = "Admin xác nhận đã nhận tiền (PAID) và tự động kích hoạt/gia hạn gói tập nguyên tử")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(
            @PathVariable Long id,
            @RequestBody(required = false) ConfirmPaymentRequest request) {
        PaymentResponse response = paymentService.confirmPayment(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Xác nhận thanh toán và kích hoạt gói thành công"));
    }

    @PostMapping("/api/v1/admin/payments/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC3.2 - Admin hủy giao dịch", description = "Admin hủy giao dịch UNPAID kèm lý do")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        PaymentResponse response = paymentService.cancelPayment(id, reason);
        return ResponseEntity.ok(ApiResponse.ok(response, "Hủy giao dịch thành công"));
    }

    @GetMapping("/api/v1/admin/payments/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC3.5 - Tổng doanh thu đã thanh toán", description = "Tính tổng doanh thu thực tế từ các giao dịch PAID")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> adminGetRevenue() {
        BigDecimal total = paymentService.adminGetTotalRevenue();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("totalRevenue", total), "Lấy tổng doanh thu thành công"));
    }
}

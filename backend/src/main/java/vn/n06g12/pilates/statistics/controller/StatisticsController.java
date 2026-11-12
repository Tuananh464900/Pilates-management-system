package vn.n06g12.pilates.statistics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.n06g12.pilates.common.response.ApiResponse;
import vn.n06g12.pilates.security.UserPrincipal;
import vn.n06g12.pilates.statistics.dto.AdminDashboardResponse;
import vn.n06g12.pilates.statistics.dto.TrainerStatsResponse;
import vn.n06g12.pilates.statistics.service.StatisticsService;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Thống kê & Báo cáo (UC5.2, UC5.3, UC5.4)", description = "Thống kê giảng dạy của Huấn luyện viên và Báo cáo Dashboard quản trị viên")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/trainer/my-stats")
    @PreAuthorize("hasRole('TRAINER')")
    @Operation(summary = "UC5.2 - Thống kê giảng dạy HLV", description = "Huấn luyện viên xem số buổi dạy, sắp dạy, học viên tham gia, tỷ lệ chuyên cần")
    public ResponseEntity<ApiResponse<TrainerStatsResponse>> getTrainerStats(@AuthenticationPrincipal UserPrincipal principal) {
        TrainerStatsResponse stats = statisticsService.getTrainerStats(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(stats, "Lấy thống kê giảng dạy thành công"));
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC5.3 & UC5.4 - Báo cáo Dashboard Quản trị viên", description = "Admin xem tổng quan KPI doanh thu theo tháng/năm và toàn kỳ, hội viên, buổi tập")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard(
            @Parameter(description = "Tháng cần lọc (1 - 12), mặc định là tháng hiện tại")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Năm cần lọc (>= 2020), mặc định là năm hiện tại")
            @RequestParam(required = false) Integer year
    ) {
        AdminDashboardResponse stats = statisticsService.getAdminDashboardStats(month, year);
        return ResponseEntity.ok(ApiResponse.ok(stats, "Lấy dữ liệu Dashboard thành công"));
    }
}

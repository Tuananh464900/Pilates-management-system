package vn.n06g12.pilates.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    // UC5.4 & R5.5: Thống kê doanh thu theo Tháng/Năm được chọn
    private Integer selectedMonth;
    private Integer selectedYear;
    private BigDecimal monthlyPaidRevenue;
    private BigDecimal monthlyBankTransferRevenue;
    private BigDecimal monthlyCashRevenue;

    // Doanh thu toàn kỳ (Chỉ tính PAID theo quy định AGENTS.md)
    private BigDecimal totalPaidRevenue;
    private BigDecimal bankTransferRevenue;
    private BigDecimal cashRevenue;
    private long unpaidPaymentsCount;

    // Hội viên
    private long totalMembers;
    private long activeMembers;
    private long lockedMembers;
    private long membersWithActivePackage;

    // Buổi tập & Đặt chỗ
    private long totalSessions;
    private long scheduledSessions;
    private long completedSessions;
    private long canceledSessions;
    private long totalConfirmedBookings;
}

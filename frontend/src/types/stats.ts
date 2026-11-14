export interface TrainerStatsResponse {
  totalSessionsTaught: number;
  totalUpcomingSessions: number;
  totalStudentsAttended: number;
  totalStudentsAbsent: number;
  attendanceRate: number; // Phần trăm (%)
}

export interface AdminDashboardResponse {
  // UC5.4 & R5.5: Thống kê doanh thu theo Tháng/Năm
  selectedMonth?: number;
  selectedYear?: number;
  monthlyPaidRevenue?: number;
  monthlyBankTransferRevenue?: number;
  monthlyCashRevenue?: number;

  // Doanh thu toàn kỳ (Chỉ tính PAID)
  totalPaidRevenue: number;
  bankTransferRevenue: number;
  cashRevenue: number;
  unpaidPaymentsCount: number;

  // Hội viên & Huấn luyện viên (R5.5)
  totalMembers: number;
  activeMembers: number;
  lockedMembers: number;
  membersWithActivePackage: number;
  totalTrainers: number;

  // Lớp học & Buổi tập (R5.5)
  activeClasses: number;
  totalSessions: number;
  scheduledSessions: number;
  completedSessions: number;
  canceledSessions: number;
  totalConfirmedBookings: number;
}

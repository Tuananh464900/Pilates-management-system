package vn.n06g12.pilates.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerStatsResponse {

    private long totalSessionsTaught;
    private long totalUpcomingSessions;
    private long totalStudentsAttended;
    private long totalStudentsAbsent;
    private double attendanceRate; // Phần trăm (%)
}

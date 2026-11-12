package vn.n06g12.pilates.attendance.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkAttendanceRequest {

    @NotEmpty(message = "Danh sách điểm danh không được để trống")
    @Valid
    private List<AttendanceItemRequest> items;

    @Builder.Default
    private boolean completeSession = false; // Đánh dấu buổi tập hoàn thành
}

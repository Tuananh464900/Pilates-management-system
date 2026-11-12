package vn.n06g12.pilates.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.booking.entity.BookingStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceItemRequest {

    @NotNull(message = "ID lượt đặt chỗ không được để trống")
    private Long bookingId;

    @NotNull(message = "Trạng thái điểm danh không được để trống")
    private BookingStatus status; // ATTENDED hoặc ABSENT
}

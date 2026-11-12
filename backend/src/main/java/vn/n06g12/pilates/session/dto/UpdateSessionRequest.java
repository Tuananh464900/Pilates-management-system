package vn.n06g12.pilates.session.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSessionRequest {

    @NotNull(message = "Lớp Pilates không được để trống")
    private Long classId;

    @NotNull(message = "Huấn luyện viên không được để trống")
    private UUID trainerId;

    @NotNull(message = "Phòng tập không được để trống")
    private Long roomId;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private Instant startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private Instant endTime;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa buổi tập phải lớn hơn 0")
    private Integer capacity;
}

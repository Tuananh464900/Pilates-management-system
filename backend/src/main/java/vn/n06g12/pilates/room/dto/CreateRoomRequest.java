package vn.n06g12.pilates.room.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

    @NotBlank(message = "Tên phòng tập không được để trống")
    private String name;

    @NotNull(message = "Sức chứa phòng không được để trống")
    @Min(value = 1, message = "Sức chứa phòng phải lớn hơn 0")
    private Integer capacity;

    private String description;
}

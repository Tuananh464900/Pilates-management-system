package vn.n06g12.pilates.pilatesclass.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.pilatesclass.entity.ClassLevel;
import vn.n06g12.pilates.pilatesclass.entity.ClassStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClassRequest {

    @NotBlank(message = "Tên lớp Pilates không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Sức chứa mặc định không được để trống")
    @Min(value = 1, message = "Sức chứa mặc định phải lớn hơn 0")
    private Integer defaultCapacity;

    @NotNull(message = "Cấp độ lớp học không được để trống")
    private ClassLevel level;

    private ClassStatus status;
}

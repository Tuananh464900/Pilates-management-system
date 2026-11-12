package vn.n06g12.pilates.membership.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.membership.entity.PackageStatus;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageRequest {

    @NotBlank(message = "Tên gói tập không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá gói tập không được để trống")
    @PositiveOrZero(message = "Giá gói tập phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    @NotNull(message = "Tổng số buổi tập không được để trống")
    @Min(value = 1, message = "Tổng số buổi tập tối thiểu là 1")
    private Integer totalSessions;

    @NotNull(message = "Thời hạn sử dụng không được để trống")
    @Min(value = 1, message = "Thời hạn sử dụng tối thiểu là 1 ngày")
    private Integer durationDays;

    @Builder.Default
    private PackageStatus status = PackageStatus.ACTIVE;
}

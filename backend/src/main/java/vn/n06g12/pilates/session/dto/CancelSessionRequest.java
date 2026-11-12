package vn.n06g12.pilates.session.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelSessionRequest {

    @NotBlank(message = "Lý do hủy buổi tập không được để trống")
    private String reason;
}

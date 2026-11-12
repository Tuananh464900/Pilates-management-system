package vn.n06g12.pilates.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.user.entity.TrainerProfile;

import java.util.UUID;

/**
 * DTO trả về cho danh sách Huấn luyện viên công khai (/api/v1/auth/trainers).
 * Bảo vệ thông tin cá nhân: ẨN hoàn toàn email, số điện thoại, trạng thái tài khoản.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicTrainerResponse {
    private UUID userId;
    private String fullName;
    private String specialization;
    private String bio;

    public static PublicTrainerResponse fromEntity(TrainerProfile profile) {
        return PublicTrainerResponse.builder()
                .userId(profile.getUserId())
                .fullName(profile.getFullName())
                .specialization(profile.getSpecialization())
                .bio(profile.getBio())
                .build();
    }
}


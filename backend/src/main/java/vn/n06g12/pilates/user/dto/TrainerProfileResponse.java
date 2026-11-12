package vn.n06g12.pilates.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.user.entity.TrainerProfile;
import vn.n06g12.pilates.user.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerProfileResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private String phone;
    private String specialization;
    private String bio;
    private UserStatus status;
    private Instant createdAt;

    public static TrainerProfileResponse fromEntity(TrainerProfile profile) {
        return TrainerProfileResponse.builder()
                .userId(profile.getUserId())
                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
                .fullName(profile.getFullName())
                .phone(profile.getPhone())
                .specialization(profile.getSpecialization())
                .bio(profile.getBio())
                .status(profile.getUser() != null ? profile.getUser().getStatus() : null)
                .createdAt(profile.getCreatedAt())
                .build();
    }
}

package vn.n06g12.pilates.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.user.entity.Gender;
import vn.n06g12.pilates.user.entity.MemberProfile;
import vn.n06g12.pilates.user.entity.UserStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private String phone;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String address;
    private UserStatus status;
    private Instant createdAt;

    public static MemberProfileResponse fromEntity(MemberProfile profile) {
        return MemberProfileResponse.builder()
                .userId(profile.getUserId())
                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
                .fullName(profile.getFullName())
                .phone(profile.getPhone())
                .gender(profile.getGender())
                .dateOfBirth(profile.getDateOfBirth())
                .address(profile.getAddress())
                .status(profile.getUser() != null ? profile.getUser().getStatus() : null)
                .createdAt(profile.getCreatedAt())
                .build();
    }
}

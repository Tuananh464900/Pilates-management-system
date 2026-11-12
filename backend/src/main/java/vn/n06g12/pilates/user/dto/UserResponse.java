package vn.n06g12.pilates.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.user.entity.Role;
import vn.n06g12.pilates.user.entity.User;
import vn.n06g12.pilates.user.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private Role role;
    private UserStatus status;
    private String fullName;
    private String phone;
    private Instant createdAt;

    public static UserResponse fromUser(User user) {
        String name = "";
        String phone = "";
        if (user.getMemberProfile() != null) {
            name = user.getMemberProfile().getFullName();
            phone = user.getMemberProfile().getPhone();
        } else if (user.getTrainerProfile() != null) {
            name = user.getTrainerProfile().getFullName();
            phone = user.getTrainerProfile().getPhone();
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .fullName(name)
                .phone(phone)
                .createdAt(user.getCreatedAt())
                .build();
    }
}

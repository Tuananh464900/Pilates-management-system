package vn.n06g12.pilates.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.user.entity.Gender;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterMemberRequest {

    // Bắt buộc xác thực với Supabase Auth trước, userId được gửi kèm (UID Supabase)
    private UUID userId;

    // Token JWT Supabase gửi kèm (nếu có) để đối chiếu xác minh quyền sở hữu chính chủ
    private String token;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private Gender gender;

    private LocalDate dateOfBirth;

    private String address;
}

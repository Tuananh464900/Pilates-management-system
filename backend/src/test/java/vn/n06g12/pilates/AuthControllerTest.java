package vn.n06g12.pilates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.n06g12.pilates.booking.repository.BookingRepository;
import vn.n06g12.pilates.common.exception.BusinessException;
import vn.n06g12.pilates.membership.repository.MemberMembershipRepository;
import vn.n06g12.pilates.membership.repository.PaymentRepository;
import vn.n06g12.pilates.security.JwtTokenValidator;
import vn.n06g12.pilates.session.repository.SessionRepository;
import vn.n06g12.pilates.user.dto.ChangePasswordRequest;
import vn.n06g12.pilates.user.dto.RegisterMemberRequest;
import vn.n06g12.pilates.user.entity.Gender;
import vn.n06g12.pilates.user.entity.Role;
import vn.n06g12.pilates.user.entity.TrainerProfile;
import vn.n06g12.pilates.user.entity.User;
import vn.n06g12.pilates.user.entity.UserStatus;
import vn.n06g12.pilates.user.repository.TrainerProfileRepository;
import vn.n06g12.pilates.user.repository.UserRepository;
import vn.n06g12.pilates.user.service.SupabaseAuthService;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MemberMembershipRepository membershipRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TrainerProfileRepository trainerProfileRepository;

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    @MockBean
    private SupabaseAuthService supabaseAuthService;

    @BeforeEach
    void setUp() {
        when(supabaseAuthService.verifyAuthUserEmail(any(), any())).thenReturn(true);
        bookingRepository.deleteAll();
        paymentRepository.deleteAll();
        membershipRepository.deleteAll();
        sessionRepository.deleteAll();
        trainerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("UC1.1 - Đăng ký học viên công khai luôn được gán vai trò MEMBER")
    void registerMember_Success() throws Exception {
        RegisterMemberRequest request = RegisterMemberRequest.builder()
                .userId(UUID.randomUUID())
                .email("hocvien@pilates.com")
                .password("Password123!")
                .fullName("Nguyễn Thị Hoa")
                .phone("0987654321")
                .gender(Gender.NU)
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .address("Hà Nội")
                .build();

        mockMvc.perform(post("/api/v1/auth/register-member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.email", is("hocvien@pilates.com")))
                .andExpect(jsonPath("$.data.role", is("MEMBER")))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("UC1.1 - Đăng ký trùng email phải trả về lỗi 400 Bad Request")
    void registerMember_DuplicateEmail_ReturnsBadRequest() throws Exception {
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("duplicate@pilates.com")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(existingUser);

        RegisterMemberRequest request = RegisterMemberRequest.builder()
                .email("duplicate@pilates.com")
                .password("Password123!")
                .fullName("Trần Văn Nam")
                .phone("0912345678")
                .build();

        mockMvc.perform(post("/api/v1/auth/register-member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message").value("Email này đã được sử dụng: duplicate@pilates.com"));
    }

    @Test
    @DisplayName("UC1.1 - Đăng ký thất bại khi Supabase UID không khớp quyền sở hữu email")
    void registerMember_InvalidUidOwnership_ReturnsBadRequest() throws Exception {
        when(supabaseAuthService.verifyAuthUserEmail(any(), any())).thenReturn(false);

        RegisterMemberRequest request = RegisterMemberRequest.builder()
                .userId(UUID.randomUUID())
                .email("fake.owner@pilates.com")
                .password("Password123!")
                .fullName("Kẻ Giả Mạo")
                .phone("0988888888")
                .build();

        mockMvc.perform(post("/api/v1/auth/register-member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message").value("Mã xác thực người dùng (UID) không khớp với email đăng ký hoặc tài khoản chưa tồn tại trên Supabase Auth!"));
    }

    @Test
    @DisplayName("UC1.1 - Đăng ký thất bại khi thiếu Supabase UID (tuyệt đối không cho phép tự sinh UUID ngẫu nhiên)")
    void registerMember_MissingUserId_ReturnsBadRequest() throws Exception {
        RegisterMemberRequest request = RegisterMemberRequest.builder()
                .email("missing.uid@pilates.com")
                .password("Password123!")
                .fullName("Thiếu UID")
                .phone("0912345678")
                .build();

        mockMvc.perform(post("/api/v1/auth/register-member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message").value("Mã xác thực người dùng (UID Supabase) là bắt buộc. Vui lòng xác thực qua Supabase Auth trước khi tạo hồ sơ!"));
    }

    @Test
    @DisplayName("UC1.4 - Đổi mật khẩu thành công khi xác minh mật khẩu cũ chính xác qua Supabase Auth")
    void changePassword_Success_WhenOldPasswordVerified() throws Exception {
        User user = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("member.change@pilates.com")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .build());

        String token = jwtTokenValidator.generateTokenForTest(user.getId(), user.getEmail(), 3600000L);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPassword123!")
                .newPassword("NewPassword456!")
                .build();

        // Giả lập Supabase Auth: Xác minh mật khẩu cũ thành công
        when(supabaseAuthService.verifyPassword(user.getEmail(), "OldPassword123!")).thenReturn(true);
        doNothing().when(supabaseAuthService).updateUserPassword(user.getId(), "NewPassword456!");

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message").value("Đổi mật khẩu thành công"));

        verify(supabaseAuthService, times(1)).verifyPassword(user.getEmail(), "OldPassword123!");
        verify(supabaseAuthService, times(1)).updateUserPassword(user.getId(), "NewPassword456!");
    }

    @Test
    @DisplayName("UC1.4 - Đổi mật khẩu thất bại khi mật khẩu hiện tại (oldPassword) không chính xác")
    void changePassword_WrongOldPassword_ReturnsBadRequest() throws Exception {
        User user = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("member.wrongpass@pilates.com")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .build());

        String token = jwtTokenValidator.generateTokenForTest(user.getId(), user.getEmail(), 3600000L);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("SaiMatKhau123!")
                .newPassword("NewPassword456!")
                .build();

        // Giả lập Supabase Auth: Xác minh mật khẩu sai -> trả về false
        when(supabaseAuthService.verifyPassword(user.getEmail(), "SaiMatKhau123!")).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message").value("Mật khẩu hiện tại không chính xác. Vui lòng kiểm tra lại."));

        // Tuyệt đối không được cập nhật mật khẩu nếu mật khẩu cũ không đúng
        verify(supabaseAuthService, times(1)).verifyPassword(user.getEmail(), "SaiMatKhau123!");
        verify(supabaseAuthService, never()).updateUserPassword(any(), any());
    }

    @Test
    @DisplayName("UC1.4 - Đổi mật khẩu thất bại khi máy chủ Supabase Auth trả về lỗi cập nhật")
    void changePassword_SupabaseUpdateFails_ReturnsBadRequest() throws Exception {
        User user = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("member.error@pilates.com")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .build());

        String token = jwtTokenValidator.generateTokenForTest(user.getId(), user.getEmail(), 3600000L);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPassword123!")
                .newPassword("NewPassword456!")
                .build();

        when(supabaseAuthService.verifyPassword(user.getEmail(), "OldPassword123!")).thenReturn(true);
        doThrow(new BusinessException("Lỗi cập nhật mật khẩu trên Supabase Auth (HTTP 500)"))
                .when(supabaseAuthService).updateUserPassword(user.getId(), "NewPassword456!");

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message").value("Lỗi cập nhật mật khẩu trên Supabase Auth (HTTP 500)"));
    }

    @Test
    @DisplayName("UC1.4 - Đổi mật khẩu thất bại khi chưa đăng nhập (không có token)")
    void changePassword_Unauthenticated_ReturnsUnauthorized() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPassword123!")
                .newPassword("NewPassword456!")
                .build();

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("UC1.4 - Đổi mật khẩu thất bại khi mật khẩu mới dưới 6 ký tự")
    void changePassword_ShortPassword_ReturnsBadRequest() throws Exception {
        User user = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("short.pass@pilates.com")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .build());

        String token = jwtTokenValidator.generateTokenForTest(user.getId(), user.getEmail(), 3600000L);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPassword123!")
                .newPassword("12345") // < 6 chars
                .build();

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("UC1.4 - Đổi mật khẩu thất bại khi mật khẩu mới trùng mật khẩu cũ")
    void changePassword_SamePassword_ReturnsBadRequest() throws Exception {
        User user = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("same.pass@pilates.com")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .build());

        String token = jwtTokenValidator.generateTokenForTest(user.getId(), user.getEmail(), 3600000L);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("Password123!")
                .newPassword("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message").value("Mật khẩu mới không được trùng với mật khẩu cũ"));
    }

    @Test
    @DisplayName("UC1.2 - Mock Login thành công khi cấu hình cho phép trong môi trường test/dev")
    void loginMock_Success_WhenEnabled() throws Exception {
        User user = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("mock.test@pilates.com")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .build());

        mockMvc.perform(post("/api/v1/auth/login-mock")
                        .param("email", user.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")));
    }

    @Test
    @DisplayName("UC1.2 - Mock Login thất bại khi email không tồn tại trong hệ thống")
    void loginMock_NonExistentEmail_ReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login-mock")
                        .param("email", "notfound@pilates.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Lỗi E - API Trainer công khai (/api/v1/auth/trainers) không cần token và ẨN email/số điện thoại bảo vệ thông tin cá nhân")
    void getPublicTrainers_Success_HidesEmailAndPhone() throws Exception {
        User trainerUser = User.builder()
                .id(UUID.randomUUID())
                .email("trainer.private@pilates.vn")
                .role(Role.TRAINER)
                .status(UserStatus.ACTIVE)
                .build();

        TrainerProfile profile = TrainerProfile.builder()
                .userId(trainerUser.getId())
                .user(trainerUser)
                .fullName("Huấn Luyện Viên Bảo Mật")
                .phone("0987654321")
                .specialization("Reformer & Tower Master")
                .bio("HLV 10 năm kinh nghiệm")
                .build();

        trainerUser.setTrainerProfile(profile);
        userRepository.save(trainerUser);

        mockMvc.perform(get("/api/v1/auth/trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[0].fullName", is("Huấn Luyện Viên Bảo Mật")))
                .andExpect(jsonPath("$.data[0].specialization", is("Reformer & Tower Master")))
                .andExpect(jsonPath("$.data[0].bio", is("HLV 10 năm kinh nghiệm")))
                .andExpect(jsonPath("$.data[0].email").doesNotExist())
                .andExpect(jsonPath("$.data[0].phone").doesNotExist())
                .andExpect(jsonPath("$.data[0].status").doesNotExist());
    }
}

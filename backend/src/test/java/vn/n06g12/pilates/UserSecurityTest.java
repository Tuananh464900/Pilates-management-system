package vn.n06g12.pilates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.n06g12.pilates.security.JwtTokenValidator;
import vn.n06g12.pilates.user.dto.CreateTrainerRequest;
import vn.n06g12.pilates.user.dto.UpdateUserStatusRequest;
import vn.n06g12.pilates.user.entity.Role;
import vn.n06g12.pilates.user.entity.User;
import vn.n06g12.pilates.user.entity.UserStatus;
import vn.n06g12.pilates.user.repository.UserRepository;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private vn.n06g12.pilates.session.repository.SessionRepository sessionRepository;

    @Autowired
    private vn.n06g12.pilates.membership.repository.PaymentRepository paymentRepository;

    @Autowired
    private vn.n06g12.pilates.membership.repository.MemberMembershipRepository membershipRepository;

    @Autowired
    private vn.n06g12.pilates.booking.repository.BookingRepository bookingRepository;

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    private User adminUser;
    private User memberUser;
    private User lockedUser;
    private String adminToken;
    private String memberToken;
    private String lockedToken;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        paymentRepository.deleteAll();
        membershipRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Tạo Admin
        adminUser = User.builder()
                .id(UUID.randomUUID())
                .email("admin@pilates.com")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(adminUser);
        adminToken = jwtTokenValidator.generateTokenForTest(adminUser.getId(), adminUser.getEmail(), 3600000L);

        // 2. Tạo Member
        memberUser = User.builder()
                .id(UUID.randomUUID())
                .email("member@pilates.com")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(memberUser);
        memberToken = jwtTokenValidator.generateTokenForTest(memberUser.getId(), memberUser.getEmail(), 3600000L);

        // 3. Tạo User bị LOCKED
        lockedUser = User.builder()
                .id(UUID.randomUUID())
                .email("locked@pilates.com")
                .role(Role.MEMBER)
                .status(UserStatus.LOCKED)
                .build();
        userRepository.save(lockedUser);
        lockedToken = jwtTokenValidator.generateTokenForTest(lockedUser.getId(), lockedUser.getEmail(), 3600000L);
    }

    @Test
    @DisplayName("UC1.2 - Tài khoản bị LOCKED bị từ chối truy cập 403 Forbidden")
    void lockedUser_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + lockedToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message").value("Tài khoản của bạn đã bị khóa bởi Quản trị viên"));
    }

    @Test
    @DisplayName("UC1.5 - Học viên không có quyền gọi API quản trị Admin (403 Forbidden)")
    void memberCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("UC1.5 - Admin có quyền xem danh sách người dùng")
    void adminCanAccessUserList() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("UC1.5 - Admin tạo tài khoản Trainer thành công")
    void adminCanCreateTrainer() throws Exception {
        CreateTrainerRequest request = CreateTrainerRequest.builder()
                .email("trainer.lan@pilates.com")
                .password("Trainer123!")
                .fullName("Lê Ngọc Lan")
                .phone("0934567890")
                .specialization("Reformer Pilates")
                .bio("5 năm kinh nghiệm giảng dạy")
                .build();

        mockMvc.perform(post("/api/v1/admin/users/trainers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.email", is("trainer.lan@pilates.com")))
                .andExpect(jsonPath("$.data.specialization", is("Reformer Pilates")));
    }

    @Test
    @DisplayName("UC1.5 - Admin không thể tự khóa chính mình")
    void adminCannotLockThemselves() throws Exception {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.LOCKED)
                .build();

        mockMvc.perform(patch("/api/v1/admin/users/" + adminUser.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message").value("Quản trị viên không thể tự khóa hoặc thay đổi trạng thái của chính mình"));
    }

    @Test
    @DisplayName("Bảo mật - Người dùng không có token không được truy cập API bảo vệ")
    void unauthenticatedUser_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message").value("Yêu cầu xác thực. Vui lòng đăng nhập."));
    }

    @Test
    @DisplayName("Bảo mật - Token giả mạo hoặc không hợp lệ bị từ chối truy cập")
    void invalidToken_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer invalid.fake.token.here"))
                .andExpect(status().isUnauthorized());
    }
}

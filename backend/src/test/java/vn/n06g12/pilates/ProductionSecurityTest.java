package vn.n06g12.pilates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import vn.n06g12.pilates.user.dto.RegisterMemberRequest;
import vn.n06g12.pilates.user.service.SupabaseAuthService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:pilates_prod_test_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=false",
        "supabase.jwt-secret=a-secure-production-test-jwt-secret-key-at-least-32-bytes!",
        "app.security.mock-login-enabled=true" // Cố ý cấu hình true để kiểm chứng cơ chế bảo vệ theo profile luôn ưu tiên chặn đứng
})
public class ProductionSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SupabaseAuthService supabaseAuthService;

    @Autowired
    private org.springframework.core.env.Environment environment;

    @Test
    @DisplayName("Bảo mật Production: Ứng dụng từ chối khởi động nếu thiếu SUPABASE_JWT_SECRET hoặc dùng secret mặc định")
    void testProduction_RejectsDefaultOrMissingJwtSecret() {
        // Kiểm tra logic xác thực trong JwtTokenValidator
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            new vn.n06g12.pilates.security.JwtTokenValidator(null, environment);
        }, "Phải từ chối khi secret null");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            new vn.n06g12.pilates.security.JwtTokenValidator("", environment);
        }, "Phải từ chối khi secret rỗng");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            new vn.n06g12.pilates.security.JwtTokenValidator(vn.n06g12.pilates.security.JwtTokenValidator.INSECURE_DEFAULT_SECRET, environment);
        }, "Phải từ chối khi dùng secret mặc định đã biết");
    }

    @Test
    @DisplayName("Bảo mật Production: Mock Login tuyệt đối bị chặn (401/403) ngay cả khi cố tình bật mock-login-enabled=true")
    void testMockLogin_AlwaysForbidden_InProductionProfile() throws Exception {
        // Trên profile prod, Spring Security filter và AuthService chặn đứng hoàn toàn
        mockMvc.perform(post("/api/v1/auth/login-mock")
                .param("email", "admin@pilates.com"))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    assertTrue(statusCode == 403 || statusCode == 401,
                            "Endpoint mock login trên production phải bị từ chối với mã 401 hoặc 403, mã thực tế: " + statusCode);
                });
    }

    @Test
    @DisplayName("Bảo mật Production: API Quản trị /api/v1/admin/** bắt buộc phải có xác thực hợp lệ")
    void testAdminEndpoints_RequireAuthentication_InProduction() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Bảo mật Production (Lỗi 1): Bắt buộc JWT token chứng minh sở hữu khi đăng ký Member, từ chối gửi UID/email đơn thuần")
    void testRegisterMember_RequiresToken_InProduction() throws Exception {
        RegisterMemberRequest req = RegisterMemberRequest.builder()
                .userId(UUID.randomUUID())
                .email("prod.unauthorized@pilates.com")
                .fullName("Attacker")
                .phone("0999999999")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register-member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bắt buộc phải có token xác thực Supabase (JWT) hợp lệ để chứng minh quyền sở hữu tài khoản. Không chấp nhận gửi UID/email đơn thuần!"));
    }

    @Test
    @DisplayName("Bảo mật Production (Lỗi 2): isMockAllowed() luôn là false trên Production kể cả khi bật mock-login-enabled=true")
    void testIsMockAllowed_AlwaysFalse_InProduction() {
        assertFalse(supabaseAuthService.isMockAllowed(),
                "isMockAllowed() phải luôn trả về FALSE trên profile prod/production!");
    }
}


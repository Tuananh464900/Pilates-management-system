package vn.n06g12.pilates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:pilates_default_test_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=false",
        "supabase.jwt-secret=a-secure-production-test-jwt-secret-key-at-least-32-bytes!",
        "app.security.mock-login-enabled=true" // Cố ý bật true trên profile mặc định
})
public class DefaultProfileSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Bảo mật Profile Mặc định: Khi không chỉ định profile dev, Mock Login luôn bị chặn 401/403 dù bật mock-login-enabled")
    void testMockLogin_AlwaysForbidden_InDefaultProfile() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login-mock")
                .param("email", "admin@pilates.com"))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    assertTrue(statusCode == 403 || statusCode == 401,
                            "Endpoint mock login trên profile mặc định phải bị từ chối với mã 401 hoặc 403, mã thực tế: " + statusCode);
                });
    }
}


package vn.n06g12.pilates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"prod", "dev"})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:pilates_multi_profile_test_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=false",
        "supabase.jwt-secret=a-secure-production-test-jwt-secret-key-at-least-32-bytes!",
        "app.security.mock-login-enabled=true" // Co tinh bat true khi chay ca prod va dev
})
public class MultiProfileSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Bao mat da Profile: Khi cau hinh nham ca prod va dev dong thoi, profile prod luon co quyen uu tien tuyet doi khoa Mock Login")
    void testMockLogin_AlwaysForbidden_WhenProdAndDevActiveTogether() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login-mock")
                .param("email", "admin@pilates.com"))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    assertTrue(statusCode == 403 || statusCode == 401,
                            "Khi profile prod kich hoat cung dev, mock login phai bi tu choi 401/403, ma thuc te: " + statusCode);
                });
    }
}


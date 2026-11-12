package vn.n06g12.pilates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FlywayMigrationTest {

    @Test
    @DisplayName("Flyway - Kiem tra tinh toan ven va quy chuan dat ten file migration V1 den V7")
    void testFlywayMigrationFiles_IntegrityAndNaming() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:db/migration/V*.sql");

        assertTrue(resources.length >= 7, "He thong phai co it nhat 7 file migration");

        List<String> fileNames = new ArrayList<>();
        for (Resource res : resources) {
            String name = res.getFilename();
            assertNotNull(name);
            fileNames.add(name);
            assertTrue(name.matches("^V[0-9]+__.*\\.sql$"), "Ten file phai chuan Flyway: " + name);
        }

        assertTrue(fileNames.stream().anyMatch(n -> n.startsWith("V1__")), "Phai co migration V1");
        assertTrue(fileNames.stream().anyMatch(n -> n.startsWith("V2__")), "Phai co migration V2");
        assertTrue(fileNames.stream().anyMatch(n -> n.startsWith("V3__")), "Phai co migration V3");
        assertTrue(fileNames.stream().anyMatch(n -> n.startsWith("V4__")), "Phai co migration V4");
        assertTrue(fileNames.stream().anyMatch(n -> n.startsWith("V5__")), "Phai co migration V5");
        assertTrue(fileNames.stream().anyMatch(n -> n.startsWith("V6__")), "Phai co migration V6");
        assertTrue(fileNames.stream().anyMatch(n -> n.startsWith("V7__")), "Phai co migration V7");
        assertTrue(fileNames.stream().anyMatch(n -> n.startsWith("V8__")), "Phai co migration V8");
    }

    @Test
    @DisplayName("Flyway V1-V7 - Kiem tra toan bo chuoi migration khong co lenh xoa du lieu hang loat")
    void testAllMigrations_NoDestructiveCommands() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:db/migration/V*.sql");

        for (Resource res : resources) {
            String fileName = res.getFilename();
            try (InputStream is = res.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                String rawContent = new String(bytes, StandardCharsets.UTF_8).toUpperCase();

                String executableSql = rawContent
                        .replaceAll("(?s)/\\*.*?\\*/", " ")
                        .replaceAll("--.*?(\r?\n|$)", " ");

                assertFalse(executableSql.contains("DELETE FROM BOOKINGS"),
                        "File " + fileName + " khong duoc xoa bookings");
                assertFalse(executableSql.contains("DELETE FROM SESSIONS"),
                        "File " + fileName + " khong duoc xoa sessions");
                assertFalse(executableSql.contains("TRUNCATE"),
                        "File " + fileName + " khong duoc su dung TRUNCATE");
                assertFalse(executableSql.contains("DROP TABLE BOOKINGS"),
                        "File " + fileName + " khong duoc DROP bookings");
                assertFalse(executableSql.contains("DROP TABLE SESSIONS"),
                        "File " + fileName + " khong duoc DROP sessions");
            }
        }
    }

    @Test
    @DisplayName("Flyway V6 - Kiem tra da tach du lieu demo khoi migration production")
    void testFlywayV6_DecoupledFromDemoSeed() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:db/migration/V6__*.sql");
        assertEquals(1, resources.length, "Phai co dung 1 file migration V6");

        Resource v6 = resources[0];
        try (InputStream is = v6.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String rawContent = new String(bytes, StandardCharsets.UTF_8).toUpperCase();
            // Khong chua cac cau insert lich tap demo co dinh phu thuoc khoa ngoai
            assertFalse(rawContent.contains("INSERT INTO SESSIONS (CLASS_ID, TRAINER_ID"),
                    "V6 khong duoc chen lich demo co dinh gay loi khoa ngoai tren database moi");
            // Phai co chi muc an toan cho sessions
            assertTrue(rawContent.contains("CREATE INDEX IF NOT EXISTS IDX_SESSIONS_START_TIME"),
                    "V6 phai tao chi muc hieu nang cho start_time");
        }
    }

    @Test
    @DisplayName("Flyway V7 - Kiem tra chinh sach Forward-Only va bang audit log")
    void testFlywayV7_ForwardOnlyPolicy() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:db/migration/V7__*.sql");
        assertEquals(1, resources.length, "Phai co dung 1 file migration V7");

        Resource v7 = resources[0];
        try (InputStream is = v7.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String rawContent = new String(bytes, StandardCharsets.UTF_8).toUpperCase();
            assertTrue(rawContent.contains("SYSTEM_AUDIT_LOGS"), "V7 phai co bang audit log");
            assertTrue(rawContent.contains("APPLY_FORWARD_ONLY_POLICY"), "V7 phai ghi nhan chinh sach Forward-only");
        }
    }

    @Test
    @DisplayName("Flyway V8 - Kiem tra kich hoat Row-Level Security (RLS) va nhat ky bao mat")
    void testFlywayV8_EnableRowLevelSecurity() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:db/migration/V8__*.sql");
        assertEquals(1, resources.length, "Phai co dung 1 file migration V8");

        Resource v8 = resources[0];
        try (InputStream is = v8.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String rawContent = new String(bytes, StandardCharsets.UTF_8).toUpperCase();
            assertTrue(rawContent.contains("ENABLE ROW LEVEL SECURITY"), "V8 phai chua lenh bat RLS");
            assertTrue(rawContent.contains("USERS ENABLE ROW LEVEL SECURITY"), "V8 phai bat RLS cho users");
            assertTrue(rawContent.contains("ENABLE_ROW_LEVEL_SECURITY"), "V8 phai ghi log kiem toan");
        }
    }

    @Test
    @DisplayName("Flyway Production Config - Kiem tra Flyway luon bat trong application.yml")
    void testFlyway_EnabledInProductionConfiguration() throws Exception {
        ClassPathResource resource = new ClassPathResource("application.yml");
        assertTrue(resource.exists(), "File application.yml phai ton tai");

        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            assertTrue(content.contains("flyway:"), "Phai co muc flyway");
            assertTrue(content.contains("enabled: true"), "Flyway phai bat enabled=true");
            assertTrue(content.contains("baseline-on-migrate: true"), "Flyway phai bat baseline-on-migrate=true");
            assertTrue(content.contains("validate-on-migrate: true"), "Flyway phai bat validate-on-migrate=true de kiem tra tinh toan ven checksum");
            assertTrue(content.contains("locations: classpath:db/migration"), "Flyway phai dung thu muc migration");
        }
    }
}

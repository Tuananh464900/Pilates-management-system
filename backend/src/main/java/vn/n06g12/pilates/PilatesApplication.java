package vn.n06g12.pilates;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@SpringBootApplication
@EnableJpaAuditing
public class PilatesApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(PilatesApplication.class, args);
    }

    @org.springframework.context.annotation.Profile("!test")
    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner enableFlywayRlsRunner(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE IF EXISTS flyway_schema_history ENABLE ROW LEVEL SECURITY;");
                System.out.println(">> Đã kích hoạt Row-Level Security cho bảng flyway_schema_history theo khuyến nghị Supabase Security Advisor.");
            } catch (Exception e) {
                // Trên môi trường H2 test hoặc database không hỗ trợ, bỏ qua an toàn
            }
        };
    }

    /**
     * Tự động nạp file .env vào System Properties khi chạy ứng dụng
     */
    private static void loadDotEnv() {
        File[] candidateFiles = new File[] {
                new File(".env"),
                new File("backend/.env"),
                new File("../backend/.env")
        };

        for (File envFile : candidateFiles) {
            if (envFile.exists() && envFile.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        int equalIndex = line.indexOf('=');
                        if (equalIndex > 0) {
                            String key = line.substring(0, equalIndex).trim();
                            String value = line.substring(equalIndex + 1).trim();
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, value);
                            }
                        }
                    }
                    System.out.println(">> Đã tự động tải cấu hình môi trường từ: " + envFile.getAbsolutePath());
                    break;
                } catch (IOException e) {
                    System.err.println("Không thể đọc file .env: " + e.getMessage());
                }
            }
        }
    }
}

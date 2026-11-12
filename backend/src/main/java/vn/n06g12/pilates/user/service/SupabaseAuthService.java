package vn.n06g12.pilates.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.n06g12.pilates.common.exception.BusinessException;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
public class SupabaseAuthService {

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key:}")
    private String serviceRoleKey;

    @Value("${supabase.anon-key:}")
    private String anonKey;

    @Value("${app.security.mock-login-enabled:false}")
    private boolean mockLoginEnabled;

    @Autowired(required = false)
    private org.springframework.core.env.Environment environment;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    public SupabaseAuthService() {
        // Constructor rỗng an toàn, không khởi tạo loopback NIO pipe
    }

    /**
     * Chỉ cho phép nhánh mock hoạt động khi vừa bật mock vừa xác nhận đang ở môi trường dev/test.
     * Tuyệt đối không cho phép trong production kể cả khi cờ mock bị bật nhầm.
     */
    public boolean isMockAllowed() {
        if (!mockLoginEnabled || environment == null) {
            return false;
        }
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles == null || activeProfiles.length == 0) {
            return false;
        }
        for (String profile : activeProfiles) {
            if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                return false;
            }
        }
        for (String profile : activeProfiles) {
            if ("dev".equalsIgnoreCase(profile) || "development".equalsIgnoreCase(profile) || "test".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Xác minh mật khẩu hiện tại (oldPassword) của người dùng qua Supabase Auth REST API
     * Endpoint: POST /auth/v1/token?grant_type=password
     * Header: apikey = serviceRoleKey hoặc anonKey
     * Body: {"email": email, "password": oldPassword}
     *
     * @return true nếu mật khẩu hợp lệ (HTTP 200), false nếu mật khẩu sai (HTTP 400)
     */
    public boolean verifyPassword(String email, String oldPassword) {
        validateConfig();

        try {
            String targetUrl = supabaseUrl.replaceAll("/+$", "") + "/auth/v1/token?grant_type=password";
            String jsonBody = String.format("{\"email\":\"%s\",\"password\":\"%s\"}",
                    escapeJson(email), escapeJson(oldPassword));

            String apiKey = StringUtils.hasText(serviceRoleKey) ? serviceRoleKey : anonKey;

            URL url = URI.create(targetUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("apikey", apiKey);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = conn.getResponseCode();
            if (statusCode == 200) {
                return true;
            } else if (statusCode == 400) {
                log.warn("Supabase xác minh mật khẩu không thành công cho email: {} (HTTP 400)", email);
                return false;
            } else {
                log.error("Supabase Auth API trả về mã lỗi không mong muốn: HTTP {}", statusCode);
                return false;
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("Lỗi khi kết nối xác minh mật khẩu qua Supabase Auth", e);
            throw new BusinessException("Không thể kết nối đến máy chủ xác thực Supabase: " + e.getMessage());
        }
    }

    /**
     * Cập nhật mật khẩu mới cho user qua Supabase Admin API hoặc trực tiếp qua database auth.users
     */
    public void updateUserPassword(UUID userId, String newPassword) {
        validateConfig();

        if (StringUtils.hasText(serviceRoleKey)) {
            try {
                String targetUrl = supabaseUrl.replaceAll("/+$", "") + "/auth/v1/admin/users/" + userId;
                String jsonBody = String.format("{\"password\":\"%s\"}", escapeJson(newPassword));

                URL url = URI.create(targetUrl).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("apikey", serviceRoleKey);
                conn.setRequestProperty("Authorization", "Bearer " + serviceRoleKey);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }

                int statusCode = conn.getResponseCode();
                if (statusCode >= 200 && statusCode < 300) {
                    log.info("Cập nhật mật khẩu thành công trên Supabase Auth cho user ID: {}", userId);
                    return;
                } else {
                    log.warn("Supabase Admin API trả về mã lỗi: HTTP {}, chuyển sang cập nhật database", statusCode);
                }
            } catch (Exception e) {
                log.warn("Lỗi khi gọi Supabase Admin API, chuyển sang cập nhật database: {}", e.getMessage());
            }
        }

        // Cập nhật an toàn qua PostgreSQL auth.users
        if (jdbcTemplate != null) {
            try {
                String sql = "UPDATE auth.users SET " +
                        "encrypted_password = extensions.crypt(?, extensions.gen_salt('bf', 10)), " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?::uuid";
                int rows = jdbcTemplate.update(sql, newPassword, userId.toString());
                if (rows > 0) {
                    log.info("Cập nhật mật khẩu thành công trong auth.users cho user ID: {}", userId);
                    return;
                }
            } catch (Exception e) {
                log.error("Lỗi cập nhật mật khẩu qua database: {}", e.getMessage(), e);
                throw new BusinessException("Không thể cập nhật mật khẩu mới: " + e.getMessage());
            }
        }

        throw new BusinessException("Hệ thống chưa thể cập nhật mật khẩu mới.");
    }

    /**
     * Tạo tài khoản xác thực mới trên Supabase Auth (auth.users và auth.identities)
     * Thường dùng khi Admin tạo tài khoản Huấn luyện viên
     *
     * @param userId UUID mong muốn (nếu null sẽ tự sinh)
     * @param email email đăng nhập
     * @param password mật khẩu khởi tạo (tối thiểu 6 ký tự)
     * @param fullName họ và tên hiển thị
     * @return UUID của tài khoản được tạo
     */
    public UUID createAuthUser(UUID userId, String email, String password, String fullName) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException("Email không được để trống");
        }
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new BusinessException("Mật khẩu ban đầu phải có ít nhất 6 ký tự");
        }

        UUID targetId = userId != null ? userId : UUID.randomUUID();
        String trimmedEmail = email.toLowerCase().trim();

        // 1. Thử gọi Supabase Admin API nếu có serviceRoleKey hợp lệ
        if (StringUtils.hasText(serviceRoleKey) && !serviceRoleKey.contains("placeholder") && !serviceRoleKey.contains("your-supabase")) {
            try {
                String targetUrl = supabaseUrl.replaceAll("/+$", "") + "/auth/v1/admin/users";
                String jsonBody = String.format(
                        "{\"email\":\"%s\",\"password\":\"%s\",\"email_confirm\":true,\"user_metadata\":{\"full_name\":\"%s\"}}",
                        escapeJson(trimmedEmail), escapeJson(password), escapeJson(fullName != null ? fullName : "")
                );

                URL url = URI.create(targetUrl).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("apikey", serviceRoleKey);
                conn.setRequestProperty("Authorization", "Bearer " + serviceRoleKey);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }

                int statusCode = conn.getResponseCode();
                if (statusCode >= 200 && statusCode < 300) {
                    try (var is = conn.getInputStream()) {
                        String respStr = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        var matcher = java.util.regex.Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"").matcher(respStr);
                        if (matcher.find()) {
                            UUID realId = UUID.fromString(matcher.group(1));
                            log.info("Tạo tài khoản thành công qua Supabase Admin API cho email: {}, ID thực tế: {}", trimmedEmail, realId);
                            return realId;
                        }
                    } catch (Exception parseEx) {
                        log.warn("Không thể trích xuất ID từ phản hồi Supabase: {}", parseEx.getMessage());
                    }
                    // Nếu không đọc được ID từ phản hồi API, tra cứu lại từ auth.users để đảm bảo đúng UID thực tế
                    if (jdbcTemplate != null) {
                        try {
                            String foundIdStr = jdbcTemplate.queryForObject(
                                    "SELECT id::text FROM auth.users WHERE LOWER(email) = LOWER(?)",
                                    String.class,
                                    trimmedEmail
                            );
                            if (foundIdStr != null) {
                                UUID foundId = UUID.fromString(foundIdStr);
                                log.info("Đã tra cứu lại UID thực tế từ auth.users cho email {}: {}", trimmedEmail, foundId);
                                return foundId;
                            }
                        } catch (Exception dbEx) {
                            log.warn("Không thể tra cứu ID từ auth.users: {}", dbEx.getMessage());
                        }
                    }
                    throw new BusinessException("Tạo tài khoản trên Supabase Admin API thành công nhưng không thể xác định được ID người dùng thực tế. Tuyệt đối không sử dụng ID giả định!");
                } else {
                    log.warn("Supabase Admin API trả về HTTP {}, chuyển sang ghi nhận trực tiếp qua database", statusCode);
                }
            } catch (BusinessException be) {
                throw be;
            } catch (Exception e) {
                log.warn("Không thể gọi Supabase Admin API: {}, chuyển sang ghi nhận trực tiếp qua database", e.getMessage());
            }
        }

        // 2. Tạo trực tiếp qua PostgreSQL auth.users và auth.identities
        if (jdbcTemplate != null) {
            try {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM auth.users WHERE LOWER(email) = LOWER(?)",
                        Integer.class,
                        trimmedEmail
                );
                if (count != null && count > 0) {
                    throw new BusinessException("Email này đã tồn tại trên hệ thống xác thực Supabase: " + trimmedEmail);
                }

                String idStr = targetId.toString();
                String rawUserMetaData = String.format(
                        "{\"sub\": \"%s\", \"email\": \"%s\", \"email_verified\": true, \"phone_verified\": false, \"full_name\": \"%s\"}",
                        idStr, trimmedEmail, escapeJson(fullName != null ? fullName : "")
                );

                String insertUserSql =
                        "INSERT INTO auth.users (" +
                        "  id, instance_id, aud, role, email, encrypted_password, email_confirmed_at, " +
                        "  raw_app_meta_data, raw_user_meta_data, created_at, updated_at" +
                        ") VALUES (" +
                        "  ?::uuid, '00000000-0000-0000-0000-000000000000'::uuid, 'authenticated', 'authenticated', ?, " +
                        "  extensions.crypt(?, extensions.gen_salt('bf', 10)), CURRENT_TIMESTAMP, " +
                        "  '{\"provider\": \"email\", \"providers\": [\"email\"]}'::jsonb, " +
                        "  ?::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP" +
                        ")";

                jdbcTemplate.update(insertUserSql, idStr, trimmedEmail, password, rawUserMetaData);

                String insertIdentitySql =
                        "INSERT INTO auth.identities (" +
                        "  id, user_id, identity_data, provider, provider_id, last_sign_in_at, created_at, updated_at" +
                        ") VALUES (" +
                        "  ?::uuid, ?::uuid, ?::jsonb, 'email', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP" +
                        ")";

                jdbcTemplate.update(insertIdentitySql, idStr, idStr, rawUserMetaData, idStr);

                log.info("Đã tạo tài khoản Supabase Auth thành công trong database cho: {} (ID: {})", trimmedEmail, idStr);
                return targetId;
            } catch (BusinessException be) {
                throw be;
            } catch (Exception e) {
                log.error("Không thể ghi vào auth.users: {}", e.getMessage());
                if (isMockAllowed()) {
                    return targetId;
                }
                throw new BusinessException("Không thể tạo tài khoản trên hệ thống xác thực Supabase Auth: " + e.getMessage());
            }
        }

        if (isMockAllowed()) {
            return targetId;
        }

        throw new BusinessException("Hệ thống xác thực Supabase Auth hiện không khả dụng. Không thể tạo tài khoản!");
    }

    /**
     * Xác minh quyền sở hữu: Kiểm tra xem userId (UID) do client gửi lên có thực sự
     * tương ứng với email đã đăng ký trên Supabase Auth (auth.users) hay không.
     * Ngăn chặn việc gửi UUID của tài khoản khác. Bắt buộc từ chối nếu không xác minh được.
     */
    public boolean verifyAuthUserEmail(UUID userId, String email) {
        if (userId == null || !StringUtils.hasText(email)) return false;
        if (isMockAllowed()) {
            return true; // Chỉ cho phép khi vừa bật mock vừa xác nhận ở môi trường dev/test
        }
        try {
            if (jdbcTemplate != null) {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM auth.users WHERE id = ?::uuid AND LOWER(email) = LOWER(?)",
                        Integer.class,
                        userId.toString(),
                        email.trim()
                );
                return count != null && count > 0;
            }
        } catch (Exception e) {
            log.warn("Không thể kiểm tra auth.users hoặc truy vấn thất bại: {}", e.getMessage());
            return false; // Tuyệt đối không cho qua nếu xác minh thất bại
        }
        return false; // Tuyệt đối không cho qua nếu không có dữ liệu khớp
    }

    /**
     * Thao tác bù (Compensating Transaction) cho UC1.1:
     * Xóa tài khoản trong auth.users và auth.identities nếu việc tạo hồ sơ trong backend thất bại.
     */
    public void deleteAuthUser(UUID userId, String email) {
        if (userId == null) return;
        try {
            if (jdbcTemplate != null) {
                jdbcTemplate.update("DELETE FROM auth.identities WHERE user_id = ?::uuid", userId.toString());
                jdbcTemplate.update("DELETE FROM auth.users WHERE id = ?::uuid", userId.toString());
                log.info("Đã thực hiện thao tác bù (compensating rollback): Xóa tài khoản auth.users cho {}", email);
            }
        } catch (Exception e) {
            log.warn("Không thể thực hiện thao tác bù xóa auth user (có thể là môi trường test): {}", e.getMessage());
        }
    }

    private void validateConfig() {
        if (!StringUtils.hasText(supabaseUrl)) {
            throw new BusinessException("Chưa cấu hình Supabase URL trên hệ thống");
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

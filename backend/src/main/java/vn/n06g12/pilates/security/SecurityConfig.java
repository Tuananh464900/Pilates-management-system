package vn.n06g12.pilates.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.n06g12.pilates.common.response.ApiResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    private boolean isMockLoginAllowedInEnvironment() {
        if (environment == null) return false;
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles == null || activeProfiles.length == 0) {
            return false; // Mặc định không có profile -> Coi như production, KHÓA
        }

        // QUY TẮC ƯU TIÊN TUYỆT ĐỐI CHO PRODUCTION:
        // Nếu có bất kỳ profile nào là prod hoặc production, quyền khóa mock của production
        // luôn có ưu tiên cao nhất, kể cả khi cấu hình nhầm nhiều profile đồng thời (vd: prod,dev).
        for (String profile : activeProfiles) {
            if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                return false;
            }
        }

        // Chỉ khi KHÔNG có prod/production và có profile dev/development/test rõ ràng:
        for (String profile : activeProfiles) {
            if ("dev".equalsIgnoreCase(profile) || "development".equalsIgnoreCase(profile) || "test".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean allowMock = isMockLoginAllowedInEnvironment();

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(org.springframework.security.config.Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    ApiResponse<Void> apiResponse = ApiResponse.error("Yêu cầu xác thực. Vui lòng đăng nhập.");
                    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    ApiResponse<Void> apiResponse = ApiResponse.error("Bạn không có quyền thực hiện thao tác này");
                    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                })
            )
            .authorizeHttpRequests(auth -> {
                // Public endpoints
                auth.requestMatchers("/api/v1/auth/register-member", "/api/v1/auth/trainers").permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/classes/**", "/api/v1/packages/**").permitAll();

                // Kiểm soát chặt chẽ endpoint login-mock:
                // Chỉ cho phép truy cập khi chạy trên profile dev/development/test.
                // Trên Production hoặc bất kỳ profile mặc định nào: Khóa hoàn toàn ở tầng Security Filter Chain (denyAll -> 403 Forbidden).
                if (allowMock) {
                    auth.requestMatchers("/api/v1/auth/login-mock").permitAll();
                } else {
                    auth.requestMatchers("/api/v1/auth/login-mock").denyAll();
                }

                // Role-specific endpoints
                auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/trainer/**").hasRole("TRAINER")
                    .requestMatchers("/api/v1/member/**").hasRole("MEMBER")
                    // All other requests require authentication
                    .anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

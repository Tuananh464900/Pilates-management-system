package vn.n06g12.pilates.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.n06g12.pilates.common.exception.BusinessException;
import vn.n06g12.pilates.common.exception.ResourceNotFoundException;
import vn.n06g12.pilates.security.JwtTokenValidator;
import vn.n06g12.pilates.user.dto.ChangePasswordRequest;
import vn.n06g12.pilates.user.dto.CreateTrainerRequest;
import vn.n06g12.pilates.user.dto.RegisterMemberRequest;
import vn.n06g12.pilates.user.dto.TrainerProfileResponse;
import vn.n06g12.pilates.user.dto.UserResponse;
import vn.n06g12.pilates.user.entity.Gender;
import vn.n06g12.pilates.user.entity.MemberProfile;
import vn.n06g12.pilates.user.entity.Role;
import vn.n06g12.pilates.user.entity.TrainerProfile;
import vn.n06g12.pilates.user.entity.User;
import vn.n06g12.pilates.user.entity.UserStatus;
import vn.n06g12.pilates.user.repository.UserRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenValidator jwtTokenValidator;
    private final SupabaseAuthService supabaseAuthService;
    private final Environment environment;

    @Value("${app.security.mock-login-enabled:false}")
    private boolean mockLoginEnabled;

    /**
     * Kiểm tra xem Mock Login có được phép hoạt động hay không.
     * Quy tắc an toàn cao nhất: Chỉ duy nhất môi trường dev/development/test được
     * cấu hình tường minh
     * mới có thể bật mock login.
     * Môi trường production hoặc profile mặc định TUYỆT ĐỐI BỊ KHÓA,
     * bất kể cờ cấu hình app.security.mock-login-enabled có bị bật hay không.
     */
    public boolean isMockLoginAllowed() {
        if (!mockLoginEnabled) {
            return false;
        }
        return isExplicitDevOrTestEnvironment();
    }

    public boolean isExplicitDevOrTestEnvironment() {
        if (environment == null)
            return false;
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles == null || activeProfiles.length == 0) {
            return false;
        }

        // QUY TẮC ƯU TIÊN TUYỆT ĐỐI CHO PRODUCTION:
        // Nếu bất kỳ profile nào là prod hoặc production, quyền khóa mock của
        // production
        // luôn có ưu tiên cao nhất, kể cả khi cấu hình nhầm nhiều profile đồng thời
        // (vd: prod,dev).
        for (String profile : activeProfiles) {
            if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                return false;
            }
        }

        for (String profile : activeProfiles) {
            if ("dev".equalsIgnoreCase(profile) || "development".equalsIgnoreCase(profile)
                    || "test".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    /**
     * UC1.1: Đăng ký Học viên công khai.
     * Quy tắc: Tài khoản đăng ký công khai LUÔN LUÔN có vai trò MEMBER.
     */
    @Transactional
    public UserResponse registerMember(RegisterMemberRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email này đã được sử dụng: " + email);
        }

        UUID userId = request.getUserId();

        // 1. Bắt buộc chứng minh quyền sở hữu bằng token JWT Supabase hợp lệ (đối chiếu
        // UID và email trong token)
        // Tuyệt đối không chấp nhận gửi UID/email đơn thuần mà không có bằng chứng sở
        // hữu token
        if (org.springframework.util.StringUtils.hasText(request.getToken())) {
            if (!jwtTokenValidator.validateToken(request.getToken())) {
                throw new BusinessException("Mã token xác thực Supabase không hợp lệ hoặc đã hết hạn!");
            }
            UUID tokenUserId = jwtTokenValidator.extractUserId(request.getToken());
            if (tokenUserId == null) {
                throw new BusinessException("Không thể trích xuất UID từ mã token xác thực Supabase!");
            }
            if (userId != null && !userId.equals(tokenUserId)) {
                throw new BusinessException("UID gửi lên không khớp với UID trong mã token xác thực Supabase!");
            }
            String tokenEmail = jwtTokenValidator.extractEmail(request.getToken());
            if (tokenEmail != null && !tokenEmail.trim().equalsIgnoreCase(email)) {
                throw new BusinessException("Email trong token xác thực (" + tokenEmail
                        + ") không khớp với email đăng ký (" + email + ")!");
            }
            userId = tokenUserId;
        } else if (!isMockLoginAllowed()) {
            // Trong môi trường production / vận hành thật, bắt buộc phải có token JWT Supabase chứng minh quyền sở hữu
            throw new BusinessException(
                    "Bắt buộc phải có token xác thực Supabase (JWT) hợp lệ để chứng minh quyền sở hữu tài khoản. Không chấp nhận gửi UID/email đơn thuần!");
        } else if (!org.springframework.util.StringUtils.hasText(request.getPassword()) && !org.springframework.util.StringUtils.hasText(request.getToken())) {
            // Trong môi trường dev/test, cũng bắt buộc phải chứng minh quyền sở hữu (bằng token hoặc mật khẩu), không chấp nhận gửi UID/email trần
            throw new BusinessException(
                    "Bắt buộc phải chứng minh quyền sở hữu tài khoản bằng token xác thực Supabase (JWT) hoặc thông tin hợp lệ. Không chấp nhận gửi UID/email đơn thuần!");
        }

        // 2. Bắt buộc phải có userId (từ Supabase Auth), tuyệt đối không tự sinh UUID
        // ngẫu nhiên
        if (userId == null) {
            throw new BusinessException(
                    "Mã xác thực người dùng (UID Supabase) là bắt buộc. Vui lòng xác thực qua Supabase Auth trước khi tạo hồ sơ!");
        }

        // 3. Bắt buộc xác minh quyền sở hữu: UID phải khớp với email đã xác thực trên
        // Supabase Auth
        boolean isValidOwner = supabaseAuthService.verifyAuthUserEmail(userId, email);
        if (!isValidOwner) {
            throw new BusinessException(
                    "Mã xác thực người dùng (UID) không khớp với email đăng ký hoặc tài khoản chưa tồn tại trên Supabase Auth!");
        }

        try {
            // ÉP BUỘC vai trò MEMBER theo đúng quy tắc AGENTS.md
            User user = User.builder()
                    .id(userId)
                    .email(email)
                    .role(Role.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();

            MemberProfile profile = MemberProfile.builder()
                    .userId(userId)
                    .user(user)
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .gender(request.getGender() != null ? request.getGender() : Gender.NAM)
                    .dateOfBirth(request.getDateOfBirth())
                    .address(request.getAddress())
                    .build();

            user.setMemberProfile(profile);
            User savedUser = userRepository.save(user);

            log.info("Đã đăng ký tài khoản Học viên mới thành công: {} ({})", savedUser.getEmail(), savedUser.getId());
            return UserResponse.fromUser(savedUser);
        } catch (Exception e) {
            log.error("Lỗi khi lưu hồ sơ học viên vào CSDL: {}", e.getMessage());
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException("Đăng ký tài khoản học viên thất bại: " + e.getMessage());
        }
    }

    /**
     * UC1.5: Admin tạo tài khoản Huấn luyện viên.
     * Quy tắc: Chỉ Admin mới có quyền tạo Trainer.
     */
    @Transactional
    public TrainerProfileResponse createTrainer(CreateTrainerRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email này đã được sử dụng: " + email);
        }

        if (!org.springframework.util.StringUtils.hasText(request.getPassword())
                || request.getPassword().length() < 6) {
            throw new BusinessException("Mật khẩu ban đầu của Huấn luyện viên bắt buộc phải có ít nhất 6 ký tự!");
        }

        UUID initialId = request.getUserId() != null ? request.getUserId() : UUID.randomUUID();

        // 1. Đồng bộ tạo tài khoản trên Supabase Auth (cả email và mật khẩu)
        // Chỉ khi tài khoản Supabase Auth được tạo thành công mới tiến hành lưu vào
        // database
        UUID userId = supabaseAuthService.createAuthUser(initialId, email, request.getPassword(),
                request.getFullName());

        // 2. Tạo bản ghi User và TrainerProfile trong database nghiệp vụ với cùng UUID
        User user = User.builder()
                .id(userId)
                .email(email)
                .role(Role.TRAINER)
                .status(UserStatus.ACTIVE)
                .build();

        TrainerProfile profile = TrainerProfile.builder()
                .userId(userId)
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .specialization(request.getSpecialization())
                .bio(request.getBio())
                .build();

        user.setTrainerProfile(profile);
        userRepository.save(user);

        log.info("Admin đã tạo tài khoản Huấn luyện viên: {} ({})", user.getEmail(), user.getId());
        return TrainerProfileResponse.fromEntity(profile);
    }

    /**
     * UC1.4: Đổi mật khẩu tài khoản đang đăng nhập.
     * Ràng buộc:
     * - Chỉ người dùng đang đăng nhập mới được đổi mật khẩu chính mình (userId lấy từ SecurityContext)
     * - Bắt buộc xác minh mật khẩu hiện tại (oldPassword) với hệ thống Supabase Auth
     * - Không chấp nhận mật khẩu mới < 6 ký tự hoặc trùng mật khẩu cũ
     * - Cập nhật mật khẩu mới qua Supabase Admin API
     * - Tuyệt đối không log mật khẩu hoặc token nhạy cảm
     */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        if (request == null) {
            throw new BusinessException("Yêu cầu đổi mật khẩu không hợp lệ");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản người dùng"));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BusinessException("Tài khoản của bạn đang bị khóa, không thể thực hiện đổi mật khẩu");
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().length() < 6) {
            throw new BusinessException("Mật khẩu mới phải có độ dài tối thiểu từ 6 ký tự");
        }

        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new BusinessException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }

        // 1. Xác minh mật khẩu hiện tại (oldPassword) qua Supabase Auth API
        boolean isOldPasswordValid = supabaseAuthService.verifyPassword(user.getEmail(), request.getOldPassword());
        if (!isOldPasswordValid) {
            throw new BusinessException("Mật khẩu hiện tại không chính xác. Vui lòng kiểm tra lại.");
        }

        // 2. Cập nhật mật khẩu mới trên Supabase Auth API
        supabaseAuthService.updateUserPassword(user.getId(), request.getNewPassword());

        log.info("Đổi mật khẩu thành công cho tài khoản người dùng: {}", user.getEmail());
    }

    /**
     * Mock Login / Local token exchange phục vụ kiểm thử và chạy local
     * Chỉ cho phép khi app.security.mock-login-enabled=true VÀ không ở Production
     */
    @Transactional(readOnly = true)
    public String generateTestTokenForUser(String email) {
        if (!isMockLoginAllowed()) {
            throw new BusinessException("Tính năng Mock Login đã bị vô hiệu hóa vì lý do bảo mật");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với email: " + email));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BusinessException("Tài khoản đã bị khóa, không thể đăng nhập");
        }

        // Sinh JWT token thời hạn 24 giờ
        return jwtTokenValidator.generateTokenForTest(user.getId(), user.getEmail(), 86400000L);
    }
}

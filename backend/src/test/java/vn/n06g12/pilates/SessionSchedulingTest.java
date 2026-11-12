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
import vn.n06g12.pilates.pilatesclass.entity.ClassLevel;
import vn.n06g12.pilates.pilatesclass.entity.ClassStatus;
import vn.n06g12.pilates.pilatesclass.entity.PilatesClass;
import vn.n06g12.pilates.pilatesclass.repository.PilatesClassRepository;
import vn.n06g12.pilates.room.entity.Room;
import vn.n06g12.pilates.room.entity.RoomStatus;
import vn.n06g12.pilates.room.repository.RoomRepository;
import vn.n06g12.pilates.security.JwtTokenValidator;
import vn.n06g12.pilates.session.dto.CancelSessionRequest;
import vn.n06g12.pilates.session.dto.CreateSessionRequest;
import vn.n06g12.pilates.session.dto.UpdateSessionRequest;
import vn.n06g12.pilates.session.entity.Session;
import vn.n06g12.pilates.session.entity.SessionStatus;
import vn.n06g12.pilates.session.repository.SessionRepository;
import vn.n06g12.pilates.user.entity.Role;
import vn.n06g12.pilates.user.entity.TrainerProfile;
import vn.n06g12.pilates.user.entity.User;
import vn.n06g12.pilates.user.entity.UserStatus;
import vn.n06g12.pilates.user.repository.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SessionSchedulingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PilatesClassRepository classRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private vn.n06g12.pilates.membership.repository.PaymentRepository paymentRepository;

    @Autowired
    private vn.n06g12.pilates.membership.repository.MemberMembershipRepository membershipRepository;

    @Autowired
    private vn.n06g12.pilates.booking.repository.BookingRepository bookingRepository;

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    private User admin;
    private User trainer1;
    private User trainer2;
    private PilatesClass activeClass;
    private PilatesClass inactiveClass;
    private Room room1;
    private Room room2;
    private String adminToken;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        paymentRepository.deleteAll();
        membershipRepository.deleteAll();
        sessionRepository.deleteAll();
        classRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Admin
        admin = User.builder()
                .id(UUID.randomUUID())
                .email("admin@pilates.com")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(admin);
        adminToken = jwtTokenValidator.generateTokenForTest(admin.getId(), admin.getEmail(), 3600000L);

        // 2. Trainers
        trainer1 = User.builder()
                .id(UUID.randomUUID())
                .email("trainer1@pilates.com")
                .role(Role.TRAINER)
                .status(UserStatus.ACTIVE)
                .build();
        TrainerProfile tp1 = TrainerProfile.builder()
                .userId(trainer1.getId())
                .user(trainer1)
                .fullName("Huấn luyện viên Mai")
                .phone("0901111222")
                .build();
        trainer1.setTrainerProfile(tp1);
        userRepository.save(trainer1);

        trainer2 = User.builder()
                .id(UUID.randomUUID())
                .email("trainer2@pilates.com")
                .role(Role.TRAINER)
                .status(UserStatus.ACTIVE)
                .build();
        TrainerProfile tp2 = TrainerProfile.builder()
                .userId(trainer2.getId())
                .user(trainer2)
                .fullName("Huấn luyện viên Hùng")
                .phone("0903333444")
                .build();
        trainer2.setTrainerProfile(tp2);
        userRepository.save(trainer2);

        // 3. Classes
        activeClass = PilatesClass.builder()
                .name("Reformer Cơ Bản")
                .defaultCapacity(10)
                .level(ClassLevel.BEGINNER)
                .status(ClassStatus.ACTIVE)
                .build();
        classRepository.save(activeClass);

        inactiveClass = PilatesClass.builder()
                .name("Cadillac Nâng Cao (Tạm dừng)")
                .defaultCapacity(5)
                .level(ClassLevel.ADVANCED)
                .status(ClassStatus.INACTIVE)
                .build();
        classRepository.save(inactiveClass);

        // 4. Rooms
        room1 = Room.builder()
                .name("Phòng Reformer 1")
                .capacity(10)
                .status(RoomStatus.ACTIVE)
                .build();
        roomRepository.save(room1);

        room2 = Room.builder()
                .name("Phòng Mat 2")
                .capacity(15)
                .status(RoomStatus.ACTIVE)
                .build();
        roomRepository.save(room2);
    }

    @Test
    @DisplayName("UC2.3 - Tạo buổi tập thành công khi phòng và HLV không bị trùng lịch")
    void createSession_Success() throws Exception {
        Instant start = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        CreateSessionRequest request = CreateSessionRequest.builder()
                .classId(activeClass.getId())
                .roomId(room1.getId())
                .trainerId(trainer1.getId())
                .startTime(start)
                .endTime(end)
                .capacity(10)
                .build();

        mockMvc.perform(post("/api/v1/admin/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.className", is("Reformer Cơ Bản")))
                .andExpect(jsonPath("$.data.status", is("SCHEDULED")));
    }

    @Test
    @DisplayName("UC2.3 - Không cho phép tạo buổi tập mới của lớp Pilates đang INACTIVE")
    void createSession_InactiveClass_ReturnsBadRequest() throws Exception {
        Instant start = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        CreateSessionRequest request = CreateSessionRequest.builder()
                .classId(inactiveClass.getId())
                .roomId(room1.getId())
                .trainerId(trainer1.getId())
                .startTime(start)
                .endTime(end)
                .build();

        mockMvc.perform(post("/api/v1/admin/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Lớp Pilates đang ngừng hoạt động, không thể tạo buổi tập mới"));
    }

    @Test
    @DisplayName("UC2.3 - Xung đột lịch phòng: Hai buổi tập trùng phòng trong cùng khung giờ bị từ chối")
    void createSession_RoomConflict_ReturnsBadRequest() throws Exception {
        Instant start = Instant.now().plus(3, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        // Buổi 1: room1, trainer1
        Session session1 = Session.builder()
                .pilatesClass(activeClass)
                .room(room1)
                .trainer(trainer1)
                .startTime(start)
                .endTime(end)
                .capacity(10)
                .bookedCount(0)
                .status(SessionStatus.SCHEDULED)
                .build();
        sessionRepository.save(session1);

        // Buổi 2: room1 (trùng phòng), trainer2
        CreateSessionRequest request2 = CreateSessionRequest.builder()
                .classId(activeClass.getId())
                .roomId(room1.getId())
                .trainerId(trainer2.getId())
                .startTime(start.plus(15, ChronoUnit.MINUTES)) // Gối lên buổi 1
                .endTime(end.plus(15, ChronoUnit.MINUTES))
                .build();

        mockMvc.perform(post("/api/v1/admin/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Phòng tập 'Phòng Reformer 1' đã có buổi tập khác trong khung giờ này (xung đột lịch phòng)"));
    }

    @Test
    @DisplayName("UC2.3 - Xung đột lịch HLV: Một Huấn luyện viên bị xếp trùng 2 buổi bị từ chối")
    void createSession_TrainerConflict_ReturnsBadRequest() throws Exception {
        Instant start = Instant.now().plus(4, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        // Buổi 1: room1, trainer1
        Session session1 = Session.builder()
                .pilatesClass(activeClass)
                .room(room1)
                .trainer(trainer1)
                .startTime(start)
                .endTime(end)
                .capacity(10)
                .bookedCount(0)
                .status(SessionStatus.SCHEDULED)
                .build();
        sessionRepository.save(session1);

        // Buổi 2: room2 (khác phòng), nhưng trainer1 (trùng HLV)
        CreateSessionRequest request2 = CreateSessionRequest.builder()
                .classId(activeClass.getId())
                .roomId(room2.getId())
                .trainerId(trainer1.getId())
                .startTime(start)
                .endTime(end)
                .build();

        mockMvc.perform(post("/api/v1/admin/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Huấn luyện viên đã có lịch dạy buổi khác trong khung giờ này (xung đột lịch HLV)"));
    }

    @Test
    @DisplayName("UC2.3 - Buổi tập đã có người đăng ký (bookedCount > 0) KHÔNG ĐƯỢC PHÉP sửa trực tiếp giờ/phòng/HLV")
    void updateSession_AlreadyBooked_ReturnsBadRequest() throws Exception {
        Instant start = Instant.now().plus(5, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        Session session = Session.builder()
                .pilatesClass(activeClass)
                .room(room1)
                .trainer(trainer1)
                .startTime(start)
                .endTime(end)
                .capacity(10)
                .bookedCount(2) // Đã có 2 học viên đặt chỗ
                .status(SessionStatus.SCHEDULED)
                .build();
        sessionRepository.save(session);

        UpdateSessionRequest updateRequest = UpdateSessionRequest.builder()
                .classId(activeClass.getId())
                .roomId(room2.getId())
                .trainerId(trainer2.getId())
                .startTime(start.plus(2, ChronoUnit.HOURS))
                .endTime(end.plus(2, ChronoUnit.HOURS))
                .capacity(10)
                .build();

        mockMvc.perform(put("/api/v1/admin/sessions/" + session.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Buổi tập đã có học viên đăng ký (2 học viên). Theo quy định, không thể sửa trực tiếp thời gian, phòng hoặc Huấn luyện viên! Vui lòng hủy buổi tập và tạo buổi mới."));
    }

    @Test
    @DisplayName("UC2.5 - Admin hủy buổi tập chưa bắt đầu thành công, chuyển sang CANCELED")
    void cancelSession_Success() throws Exception {
        Instant start = Instant.now().plus(6, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        Session session = Session.builder()
                .pilatesClass(activeClass)
                .room(room1)
                .trainer(trainer1)
                .startTime(start)
                .endTime(end)
                .capacity(10)
                .bookedCount(0)
                .status(SessionStatus.SCHEDULED)
                .build();
        sessionRepository.save(session);

        CancelSessionRequest cancelReq = CancelSessionRequest.builder()
                .reason("Phòng tập bảo trì máy Reformer đột xuất")
                .build();

        mockMvc.perform(post("/api/v1/admin/sessions/" + session.getId() + "/cancel")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("CANCELED")))
                .andExpect(jsonPath("$.data.cancelReason", is("Phòng tập bảo trì máy Reformer đột xuất")));
    }
}

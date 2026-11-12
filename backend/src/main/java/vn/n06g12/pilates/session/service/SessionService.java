package vn.n06g12.pilates.session.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.n06g12.pilates.booking.entity.Booking;
import vn.n06g12.pilates.booking.entity.BookingStatus;
import vn.n06g12.pilates.booking.repository.BookingRepository;
import vn.n06g12.pilates.common.exception.BusinessException;
import vn.n06g12.pilates.common.exception.ResourceNotFoundException;
import vn.n06g12.pilates.membership.entity.MemberMembership;
import vn.n06g12.pilates.membership.entity.MembershipStatus;
import vn.n06g12.pilates.membership.repository.MemberMembershipRepository;
import vn.n06g12.pilates.pilatesclass.entity.ClassStatus;
import vn.n06g12.pilates.pilatesclass.entity.PilatesClass;
import vn.n06g12.pilates.pilatesclass.repository.PilatesClassRepository;
import vn.n06g12.pilates.room.entity.Room;
import vn.n06g12.pilates.room.entity.RoomStatus;
import vn.n06g12.pilates.room.repository.RoomRepository;
import vn.n06g12.pilates.session.dto.CancelSessionRequest;
import vn.n06g12.pilates.session.dto.CreateSessionRequest;
import vn.n06g12.pilates.session.dto.SessionResponse;
import vn.n06g12.pilates.session.dto.UpdateSessionRequest;
import vn.n06g12.pilates.session.entity.Session;
import vn.n06g12.pilates.session.entity.SessionStatus;
import vn.n06g12.pilates.session.repository.SessionRepository;
import vn.n06g12.pilates.user.entity.Role;
import vn.n06g12.pilates.user.entity.User;
import vn.n06g12.pilates.user.entity.UserStatus;
import vn.n06g12.pilates.user.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final PilatesClassRepository classRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final MemberMembershipRepository membershipRepository;

    @Autowired
    public SessionService(
            SessionRepository sessionRepository,
            PilatesClassRepository classRepository,
            RoomRepository roomRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            MemberMembershipRepository membershipRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.classRepository = classRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.membershipRepository = membershipRepository;
    }

    /**
     * UC2.3: Tạo buổi tập Pilates mới.
     * Kiểm tra: Lớp ACTIVE, Phòng ACTIVE, Trainer hợp lệ, không trùng giờ HLV, không trùng phòng.
     */
    @Transactional
    public SessionResponse createSession(CreateSessionRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        if (request.getStartTime().isBefore(Instant.now())) {
            throw new BusinessException("Thời gian bắt đầu buổi tập phải ở tương lai");
        }

        // 1. Kiểm tra Lớp Pilates
        PilatesClass pilatesClass = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp Pilates"));
        if (pilatesClass.getStatus() == ClassStatus.INACTIVE) {
            throw new BusinessException("Lớp Pilates đang ngừng hoạt động, không thể tạo buổi tập mới");
        }

        // 2. Kiểm tra Phòng tập và khóa bi quan để chống race condition khi tạo lịch đồng thời tại cùng phòng
        Room room = roomRepository.findByIdWithLock(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng tập"));
        if (room.getStatus() == RoomStatus.INACTIVE) {
            throw new BusinessException("Phòng tập đang ngừng hoạt động, không thể chọn cho buổi mới");
        }

        // 3. Kiểm tra Huấn luyện viên và khóa bi quan để chống race condition khi tạo lịch đồng thời cho cùng HLV
        User trainer = userRepository.findByIdWithLock(request.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Huấn luyện viên"));
        if (trainer.getRole() != Role.TRAINER || trainer.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Người dùng được chọn không phải là Huấn luyện viên hợp lệ hoặc đang bị khóa");
        }

        // 4. Kiểm tra xung đột lịch Phòng tập
        List<Session> roomConflicts = sessionRepository.findConflictingRoomSessions(
                request.getRoomId(), request.getStartTime(), request.getEndTime(), null);
        if (!roomConflicts.isEmpty()) {
            throw new BusinessException("Phòng tập '" + room.getName() + "' đã có buổi tập khác trong khung giờ này (xung đột lịch phòng)");
        }

        // 5. Kiểm tra xung đột lịch Huấn luyện viên
        List<Session> trainerConflicts = sessionRepository.findConflictingTrainerSessions(
                request.getTrainerId(), request.getStartTime(), request.getEndTime(), null);
        if (!trainerConflicts.isEmpty()) {
            throw new BusinessException("Huấn luyện viên đã có lịch dạy buổi khác trong khung giờ này (xung đột lịch HLV)");
        }

        int capacity = request.getCapacity() != null ? request.getCapacity() : pilatesClass.getDefaultCapacity();

        // Kiểm tra sức chứa buổi tập không được vượt quá sức chứa tối đa của phòng tập
        if (capacity > room.getCapacity()) {
            throw new BusinessException("Sức chứa buổi tập (" + capacity + " người) không được vượt quá sức chứa tối đa của phòng tập '" + room.getName() + "' (" + room.getCapacity() + " người)");
        }

        Session session = Session.builder()
                .pilatesClass(pilatesClass)
                .trainer(trainer)
                .room(room)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .capacity(capacity)
                .bookedCount(0)
                .status(SessionStatus.SCHEDULED)
                .build();

        Session saved = sessionRepository.save(session);
        log.info("Admin đã tạo buổi tập mới ID: {} cho lớp '{}' tại phòng '{}'", saved.getId(), pilatesClass.getName(), room.getName());
        return SessionResponse.fromEntity(saved);
    }

    /**
     * UC2.3: Cập nhật buổi tập.
     * Quy tắc: Nếu buổi tập ĐÃ CÓ BOOKING -> TUYỆT ĐỐI KHÔNG CHO SỬA trực tiếp giờ/phòng/HLV!
     */
    @Transactional
    public SessionResponse updateSession(Long id, UpdateSessionRequest request) {
        Session session = sessionRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tập với ID: " + id));

        // RÀNG BUỘC CỐ ĐỊNH CỦA BÁO CÁO:
        if (session.getBookedCount() > 0) {
            throw new BusinessException("Buổi tập đã có học viên đăng ký (" + session.getBookedCount() + " học viên). Theo quy định, không thể sửa trực tiếp thời gian, phòng hoặc Huấn luyện viên! Vui lòng hủy buổi tập và tạo buổi mới.");
        }

        if (session.getStatus() == SessionStatus.CANCELED) {
            throw new BusinessException("Buổi tập đã bị hủy, không thể chỉnh sửa");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        PilatesClass pilatesClass = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp Pilates"));
        if (pilatesClass.getStatus() == ClassStatus.INACTIVE) {
            throw new BusinessException("Lớp Pilates đang ngừng hoạt động, không thể chọn");
        }

        Room room = roomRepository.findByIdWithLock(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng tập"));
        if (room.getStatus() == RoomStatus.INACTIVE) {
            throw new BusinessException("Phòng tập đang ngừng hoạt động, không thể chọn");
        }

        User trainer = userRepository.findByIdWithLock(request.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Huấn luyện viên"));
        if (trainer.getRole() != Role.TRAINER || trainer.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Người dùng được chọn không phải là Huấn luyện viên hợp lệ hoặc đang bị khóa");
        }

        // Kiểm tra xung đột loại trừ chính session này
        List<Session> roomConflicts = sessionRepository.findConflictingRoomSessions(
                request.getRoomId(), request.getStartTime(), request.getEndTime(), id);
        if (!roomConflicts.isEmpty()) {
            throw new BusinessException("Phòng tập '" + room.getName() + "' đã có buổi tập khác trong khung giờ này");
        }

        List<Session> trainerConflicts = sessionRepository.findConflictingTrainerSessions(
                request.getTrainerId(), request.getStartTime(), request.getEndTime(), id);
        if (!trainerConflicts.isEmpty()) {
            throw new BusinessException("Huấn luyện viên đã có lịch dạy buổi khác trong khung giờ này");
        }

        session.setPilatesClass(pilatesClass);
        session.setRoom(room);
        session.setTrainer(trainer);
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        if (request.getCapacity() != null) {
            if (request.getCapacity() > room.getCapacity()) {
                throw new BusinessException("Sức chứa buổi tập (" + request.getCapacity() + " người) không được vượt quá sức chứa tối đa của phòng tập '" + room.getName() + "' (" + room.getCapacity() + " người)");
            }
            if (request.getCapacity() < session.getBookedCount()) {
                throw new BusinessException("Không thể giảm sức chứa xuống " + request.getCapacity() + " vì buổi tập đã có " + session.getBookedCount() + " người đặt chỗ");
            }
            session.setCapacity(request.getCapacity());
        }

        Session saved = sessionRepository.save(session);
        log.info("Admin đã cập nhật buổi tập ID: {}", saved.getId());
        return SessionResponse.fromEntity(saved);
    }

    /**
     * UC2.5: Admin hủy buổi tập chưa bắt đầu.
     * Quy tắc: Hủy buổi, hủy booking hợp lệ và hoàn trả 1 lượt về đúng gói đã trừ.
     */
    @Transactional
    public SessionResponse cancelSession(Long id, CancelSessionRequest request) {
        // UC2.5: Khóa bi quan Session để chống race condition khi Admin hủy buổi trong lúc có người đặt chỗ
        Session session = sessionRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tập với ID: " + id));

        if (session.getStatus() == SessionStatus.CANCELED) {
            throw new BusinessException("Buổi tập này đã bị hủy trước đó");
        }

        if (session.getStartTime().isBefore(Instant.now())) {
            throw new BusinessException("Không thể hủy buổi tập đã bắt đầu hoặc đã diễn ra");
        }

        session.setStatus(SessionStatus.CANCELED);
        session.setCancelReason(request.getReason());
        session.setBookedCount(0);
        Session saved = sessionRepository.save(session);

        // UC2.5 & UC4.2: Tự động hủy toàn bộ booking còn hiệu lực và hoàn trả 1 lượt về đúng gói đã trừ
        // Sắp xếp booking theo ID tăng dần để thống nhất thứ tự khóa (Session -> Booking -> Membership), chống deadlock với Member hủy
        List<Booking> confirmedBookings = bookingRepository.findBySessionIdAndStatusOrderByIdAsc(id, BookingStatus.CONFIRMED);
        Instant now = Instant.now();

        for (Booking b : confirmedBookings) {
            MemberMembership membership = membershipRepository.findByIdWithLock(b.getMembership().getId())
                    .orElse(b.getMembership());
            membership.setRemainingSessions(membership.getRemainingSessions() + 1);

            // KIỂM SOÁT KHÔI PHỤC ACTIVE:
            // Chỉ khôi phục ACTIVE cho gói EXHAUSTED nếu gói còn hạn sử dụng VÀ Member chưa có gói nào khác đang ACTIVE
            if (membership.getStatus() == MembershipStatus.EXHAUSTED && membership.getEndDate().isAfter(now)) {
                // Khóa gói ACTIVE với PESSIMISTIC_WRITE để chống mất cập nhật khi thao tác đồng thời (Lỗi A)
                Optional<MemberMembership> existingActiveOpt = membershipRepository.findCurrentActiveMembershipWithLock(
                        membership.getMember().getId(),
                        MembershipStatus.ACTIVE,
                        now
                );

                if (existingActiveOpt.isPresent() && !existingActiveOpt.get().getId().equals(membership.getId())) {
                    MemberMembership existingActive = existingActiveOpt.get();
                    if (existingActive.getMembershipPackage().getId().equals(membership.getMembershipPackage().getId())) {
                        existingActive.setRemainingSessions(existingActive.getRemainingSessions() + 1);
                        membershipRepository.save(existingActive);
                        membership.setRemainingSessions(membership.getRemainingSessions() - 1);
                    }
                } else {
                    membership.setStatus(MembershipStatus.ACTIVE);
                }
            }
            membershipRepository.save(membership);

            b.setRefunded(true);
            b.setStatus(BookingStatus.CANCELED);
            b.setCanceledAt(now);
            b.setCancelReason("Buổi tập bị Admin hủy: " + request.getReason());
            bookingRepository.save(b);
        }

        log.info("Admin đã hủy buổi tập ID: {} với lý do: '{}'", saved.getId(), request.getReason());
        return SessionResponse.fromEntity(saved);
    }

    /**
     * UC2.4: Xem lịch tập / Lịch dạy
     */
    @Transactional(readOnly = true)
    public List<SessionResponse> getSchedule(Instant from, Instant to, UUID trainerId, Long classId) {
        return sessionRepository.findSchedule(from, to, trainerId, classId).stream()
                .map(SessionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SessionResponse> adminSearchSessions(SessionStatus status, Long classId, UUID trainerId, Pageable pageable) {
        return sessionRepository.searchSessionsAdmin(status, classId, trainerId, pageable)
                .map(SessionResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionById(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tập với ID: " + id));
        return SessionResponse.fromEntity(session);
    }
}

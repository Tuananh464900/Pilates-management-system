package vn.n06g12.pilates.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.n06g12.pilates.attendance.dto.AttendanceItemRequest;
import vn.n06g12.pilates.attendance.dto.AttendanceRecordResponse;
import vn.n06g12.pilates.attendance.dto.MarkAttendanceRequest;
import vn.n06g12.pilates.booking.entity.Booking;
import vn.n06g12.pilates.booking.entity.BookingStatus;
import vn.n06g12.pilates.booking.repository.BookingRepository;
import vn.n06g12.pilates.common.exception.BusinessException;
import vn.n06g12.pilates.common.exception.ResourceNotFoundException;
import vn.n06g12.pilates.session.entity.Session;
import vn.n06g12.pilates.session.entity.SessionStatus;
import vn.n06g12.pilates.session.repository.SessionRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final SessionRepository sessionRepository;
    private final BookingRepository bookingRepository;

    /**
     * UC5.1: Điểm danh buổi tập Pilates
     * Ràng buộc nghiệp vụ:
     * - Trainer chỉ điểm danh buổi do chính mình phụ trách (Admin có toàn quyền)
     * - Chỉ điểm danh từ lúc buổi tập bắt đầu trở đi (now >= session.startTime)
     * - Chỉ điểm danh các Member đã có booking
     * - Vắng mặt (ABSENT) tuyệt đối KHÔNG hoàn lượt tập
     */
    @Transactional
    public List<AttendanceRecordResponse> markAttendance(UUID currentUserId, boolean isAdmin, Long sessionId, MarkAttendanceRequest request) {
        Session session = sessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tập với ID: " + sessionId));

        // 1. Kiểm tra phân quyền: Trainer chỉ được điểm danh buổi của mình
        if (!isAdmin && !session.getTrainer().getId().equals(currentUserId)) {
            throw new BusinessException("Bạn chỉ được phép điểm danh buổi tập do chính mình phụ trách!");
        }

        // 2. Kiểm tra trạng thái Session: Nếu buổi tập đã bị hủy thì từ chối ngay lập tức (UC5.2)
        if (session.getStatus() == SessionStatus.CANCELED) {
            throw new BusinessException("Buổi tập này đã bị hủy, không thể thực hiện điểm danh!");
        }

        // 3. Kiểm tra thời điểm điểm danh: Từ lúc buổi bắt đầu trở đi
        Instant now = Instant.now();
        if (now.isBefore(session.getStartTime())) {
            throw new BusinessException("Chưa đến giờ diễn ra buổi tập (bắt đầu lúc " + session.getStartTime() + "), không thể điểm danh trước!");
        }

        // 3. Thực hiện điểm danh từng học viên
        for (AttendanceItemRequest item : request.getItems()) {
            Booking booking = bookingRepository.findById(item.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lượt đặt chỗ ID: " + item.getBookingId()));

            if (!booking.getSession().getId().equals(sessionId)) {
                throw new BusinessException("Lượt đặt chỗ #" + booking.getId() + " không thuộc về buổi tập này!");
            }

            if (booking.getStatus() == BookingStatus.CANCELED) {
                throw new BusinessException("Không thể điểm danh học viên đã hủy buổi tập (Booking #" + booking.getId() + ")!");
            }

            if (item.getStatus() != BookingStatus.ATTENDED && item.getStatus() != BookingStatus.ABSENT) {
                throw new BusinessException("Trạng thái điểm danh không hợp lệ. Chỉ chấp nhận ATTENDED (Có mặt) hoặc ABSENT (Vắng mặt)!");
            }

            booking.setStatus(item.getStatus());
            // Quy tắc: Vắng mặt tuyệt đối không hoàn lượt
            booking.setRefunded(false);
            bookingRepository.save(booking);
        }

        // 4. Nếu có yêu cầu hoàn tất buổi tập hoặc buổi tập đã kết thúc
        if (request.isCompleteSession() || now.isAfter(session.getEndTime())) {
            session.setStatus(SessionStatus.COMPLETED);
            sessionRepository.save(session);
            log.info("Buổi tập ID {} đã được chuyển trạng thái sang COMPLETED sau khi điểm danh", sessionId);
        }

        log.info("Điểm danh thành công {} học viên cho buổi tập ID {}", request.getItems().size(), sessionId);

        return bookingRepository.findBySessionIdWithMember(sessionId)
                .stream()
                .map(AttendanceRecordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getSessionAttendance(UUID currentUserId, boolean isAdmin, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tập với ID: " + sessionId));

        if (!isAdmin && !session.getTrainer().getId().equals(currentUserId)) {
            throw new BusinessException("Bạn chỉ được phép xem điểm danh buổi tập do chính mình phụ trách!");
        }

        return bookingRepository.findBySessionIdWithMember(sessionId)
                .stream()
                .map(AttendanceRecordResponse::from)
                .toList();
    }
}

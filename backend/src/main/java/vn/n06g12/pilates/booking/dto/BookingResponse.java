package vn.n06g12.pilates.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.booking.entity.Booking;
import vn.n06g12.pilates.booking.entity.BookingStatus;
import vn.n06g12.pilates.pilatesclass.entity.ClassLevel;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private UUID memberId;
    private String memberEmail;
    private String memberName;
    private String memberPhone;

    // Thông tin buổi tập
    private Long sessionId;
    private String className;
    private ClassLevel classLevel;
    private String roomName;
    private String trainerName;
    private Instant startTime;
    private Instant endTime;

    // Gói tập đã dùng
    private Long membershipId;
    private String packageName;

    // Trạng thái booking
    private BookingStatus status;
    private Boolean refunded;
    private String cancelReason;
    private Instant canceledAt;
    private Instant createdAt;

    // Cờ nghiệp vụ hiển thị UI
    private boolean canCancel;
    private boolean isRefundable;

    public static BookingResponse from(Booking b) {
        String memberName = null;
        String memberPhone = null;
        if (b.getMember() != null && b.getMember().getMemberProfile() != null) {
            memberName = b.getMember().getMemberProfile().getFullName();
            memberPhone = b.getMember().getMemberProfile().getPhone();
        }

        String trainerName = null;
        if (b.getSession() != null && b.getSession().getTrainer() != null && b.getSession().getTrainer().getTrainerProfile() != null) {
            trainerName = b.getSession().getTrainer().getTrainerProfile().getFullName();
        }

        Instant now = Instant.now();
        boolean canCancel = b.getStatus() == BookingStatus.CONFIRMED &&
                b.getSession() != null &&
                b.getSession().getStartTime().isAfter(now);

        boolean isRefundable = false;
        if (canCancel && b.getSession() != null) {
            long minutesUntilStart = Duration.between(now, b.getSession().getStartTime()).toMinutes();
            isRefundable = minutesUntilStart >= 120;
        }

        return BookingResponse.builder()
                .id(b.getId())
                .memberId(b.getMember() != null ? b.getMember().getId() : null)
                .memberEmail(b.getMember() != null ? b.getMember().getEmail() : null)
                .memberName(memberName)
                .memberPhone(memberPhone)
                .sessionId(b.getSession() != null ? b.getSession().getId() : null)
                .className(b.getSession() != null && b.getSession().getPilatesClass() != null ? b.getSession().getPilatesClass().getName() : null)
                .classLevel(b.getSession() != null && b.getSession().getPilatesClass() != null ? b.getSession().getPilatesClass().getLevel() : null)
                .roomName(b.getSession() != null && b.getSession().getRoom() != null ? b.getSession().getRoom().getName() : null)
                .trainerName(trainerName)
                .startTime(b.getSession() != null ? b.getSession().getStartTime() : null)
                .endTime(b.getSession() != null ? b.getSession().getEndTime() : null)
                .membershipId(b.getMembership() != null ? b.getMembership().getId() : null)
                .packageName(b.getMembership() != null && b.getMembership().getMembershipPackage() != null ? b.getMembership().getMembershipPackage().getName() : null)
                .status(b.getStatus())
                .refunded(b.getRefunded())
                .cancelReason(b.getCancelReason())
                .canceledAt(b.getCanceledAt())
                .createdAt(b.getCreatedAt())
                .canCancel(canCancel)
                .isRefundable(isRefundable)
                .build();
    }
}

package vn.n06g12.pilates.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.booking.entity.Booking;
import vn.n06g12.pilates.booking.entity.BookingStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordResponse {

    private Long bookingId;
    private UUID memberId;
    private String memberName;
    private String memberEmail;
    private String memberPhone;
    private BookingStatus status;
    private Instant updatedAt;

    public static AttendanceRecordResponse from(Booking b) {
        String name = null;
        String phone = null;
        if (b.getMember() != null && b.getMember().getMemberProfile() != null) {
            name = b.getMember().getMemberProfile().getFullName();
            phone = b.getMember().getMemberProfile().getPhone();
        }

        return AttendanceRecordResponse.builder()
                .bookingId(b.getId())
                .memberId(b.getMember() != null ? b.getMember().getId() : null)
                .memberName(name)
                .memberEmail(b.getMember() != null ? b.getMember().getEmail() : null)
                .memberPhone(phone)
                .status(b.getStatus())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}

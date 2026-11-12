package vn.n06g12.pilates.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.pilatesclass.entity.ClassLevel;
import vn.n06g12.pilates.session.entity.Session;
import vn.n06g12.pilates.session.entity.SessionStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private Long id;
    
    // Class details
    private Long classId;
    private String className;
    private ClassLevel classLevel;
    
    // Trainer details
    private UUID trainerId;
    private String trainerName;
    private String trainerPhone;
    
    // Room details
    private Long roomId;
    private String roomName;
    
    // Timing & Capacity
    private Instant startTime;
    private Instant endTime;
    private Integer capacity;
    private Integer bookedCount;
    private Integer availableCount;
    
    private SessionStatus status;
    private String cancelReason;
    private Instant createdAt;

    public static SessionResponse fromEntity(Session session) {
        String trainerName = "HLV";
        String trainerPhone = "";
        if (session.getTrainer() != null && session.getTrainer().getTrainerProfile() != null) {
            trainerName = session.getTrainer().getTrainerProfile().getFullName();
            trainerPhone = session.getTrainer().getTrainerProfile().getPhone();
        } else if (session.getTrainer() != null) {
            trainerName = session.getTrainer().getEmail();
        }

        int available = Math.max(0, session.getCapacity() - session.getBookedCount());

        return SessionResponse.builder()
                .id(session.getId())
                .classId(session.getPilatesClass().getId())
                .className(session.getPilatesClass().getName())
                .classLevel(session.getPilatesClass().getLevel())
                .trainerId(session.getTrainer().getId())
                .trainerName(trainerName)
                .trainerPhone(trainerPhone)
                .roomId(session.getRoom().getId())
                .roomName(session.getRoom().getName())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .capacity(session.getCapacity())
                .bookedCount(session.getBookedCount())
                .availableCount(available)
                .status(session.getStatus())
                .cancelReason(session.getCancelReason())
                .createdAt(session.getCreatedAt())
                .build();
    }
}

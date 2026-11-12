package vn.n06g12.pilates.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.membership.entity.MemberMembership;
import vn.n06g12.pilates.membership.entity.MembershipStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberMembershipResponse {

    private Long id;
    private UUID memberId;
    private String memberEmail;
    private String memberName;
    private Long packageId;
    private String packageName;
    private Integer totalSessions;
    private Integer remainingSessions;
    private Instant startDate;
    private Instant endDate;
    private Long daysRemaining;
    private MembershipStatus status;
    private boolean isWarningDays;
    private boolean isWarningSessions;
    private Instant createdAt;

    public static MemberMembershipResponse from(MemberMembership m) {
        String name = null;
        if (m.getMember() != null && m.getMember().getMemberProfile() != null) {
            name = m.getMember().getMemberProfile().getFullName();
        }

        long days = 0;
        if (m.getEndDate() != null && m.getEndDate().isAfter(Instant.now())) {
            days = ChronoUnit.DAYS.between(Instant.now(), m.getEndDate());
        }

        return MemberMembershipResponse.builder()
                .id(m.getId())
                .memberId(m.getMember() != null ? m.getMember().getId() : null)
                .memberEmail(m.getMember() != null ? m.getMember().getEmail() : null)
                .memberName(name)
                .packageId(m.getMembershipPackage() != null ? m.getMembershipPackage().getId() : null)
                .packageName(m.getMembershipPackage() != null ? m.getMembershipPackage().getName() : null)
                .totalSessions(m.getMembershipPackage() != null ? m.getMembershipPackage().getTotalSessions() : null)
                .remainingSessions(m.getRemainingSessions())
                .startDate(m.getStartDate())
                .endDate(m.getEndDate())
                .daysRemaining(days)
                .status(m.getStatus())
                .isWarningDays(m.isWarningDays())
                .isWarningSessions(m.isWarningSessions())
                .createdAt(m.getCreatedAt())
                .build();
    }
}

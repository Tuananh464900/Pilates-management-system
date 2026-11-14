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
    private Integer packageCount;
    private Integer renewalCount;
    private Integer totalAccumulatedSessions;
    private Integer usedSessions;
    private Instant startDate;
    private Instant endDate;
    private Long daysRemaining;
    private MembershipStatus status;
    private boolean isWarningDays;
    private boolean isWarningSessions;
    private Instant createdAt;

    public static MemberMembershipResponse from(MemberMembership m) {
        return from(m, 0);
    }

    public static MemberMembershipResponse from(MemberMembership m, int actualPaymentCount) {
        String name = null;
        if (m.getMember() != null && m.getMember().getMemberProfile() != null) {
            name = m.getMember().getMemberProfile().getFullName();
        }

        long days = 0;
        if (m.getEndDate() != null && m.getEndDate().isAfter(Instant.now())) {
            days = ChronoUnit.DAYS.between(Instant.now(), m.getEndDate());
        }

        int pkgSessions = m.getMembershipPackage() != null && m.getMembershipPackage().getTotalSessions() != null
                ? m.getMembershipPackage().getTotalSessions()
                : 1;
        int remaining = m.getRemainingSessions() != null ? m.getRemainingSessions() : 0;

        int estimatedCount = remaining > pkgSessions
                ? (int) Math.ceil((double) remaining / pkgSessions)
                : 1;
        int packageCount = Math.max(actualPaymentCount, estimatedCount);
        int renewalCount = Math.max(0, packageCount - 1);
        int totalAccumulatedSessions = packageCount * pkgSessions;
        int usedSessions = Math.max(0, totalAccumulatedSessions - remaining);

        return MemberMembershipResponse.builder()
                .id(m.getId())
                .memberId(m.getMember() != null ? m.getMember().getId() : null)
                .memberEmail(m.getMember() != null ? m.getMember().getEmail() : null)
                .memberName(name)
                .packageId(m.getMembershipPackage() != null ? m.getMembershipPackage().getId() : null)
                .packageName(m.getMembershipPackage() != null ? m.getMembershipPackage().getName() : null)
                .totalSessions(pkgSessions)
                .remainingSessions(remaining)
                .packageCount(packageCount)
                .renewalCount(renewalCount)
                .totalAccumulatedSessions(totalAccumulatedSessions)
                .usedSessions(usedSessions)
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

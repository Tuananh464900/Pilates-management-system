package vn.n06g12.pilates.membership.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.n06g12.pilates.user.entity.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "member_memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private MembershipPackage membershipPackage;

    @Column(nullable = false)
    private Integer remainingSessions;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE &&
                endDate != null && endDate.isAfter(Instant.now()) &&
                remainingSessions != null && remainingSessions > 0;
    }

    public boolean isWarningDays() {
        if (!isActive()) return false;
        long daysUntilEnd = ChronoUnit.DAYS.between(Instant.now(), endDate);
        return daysUntilEnd <= 7;
    }

    public boolean isWarningSessions() {
        if (!isActive()) return false;
        return remainingSessions <= 2;
    }
}

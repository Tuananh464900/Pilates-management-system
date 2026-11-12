package vn.n06g12.pilates.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.n06g12.pilates.membership.entity.MemberMembership;
import vn.n06g12.pilates.session.entity.Session;
import vn.n06g12.pilates.user.entity.User;

import java.time.Instant;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    /**
     * Lưu chính xác gói tập đã bị trừ lượt để khi hủy buổi hoặc hủy đăng ký hợp lệ,
     * hoàn trả 1 lượt về ĐÚNG gói này (theo yêu cầu AGENTS.md)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id", nullable = false)
    private MemberMembership membership;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(nullable = false)
    @Builder.Default
    private Boolean refunded = false;

    @Column(length = 255)
    private String cancelReason;

    private Instant canceledAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}

package vn.n06g12.pilates.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.membership.entity.Payment;
import vn.n06g12.pilates.membership.entity.PaymentMethod;
import vn.n06g12.pilates.membership.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private String transactionCode;
    private UUID memberId;
    private String memberEmail;
    private String memberName;
    private Long packageId;
    private String packageName;
    private Long membershipId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private Instant paidAt;
    private String note;
    private Instant createdAt;

    public static PaymentResponse from(Payment p) {
        String name = null;
        if (p.getMember() != null && p.getMember().getMemberProfile() != null) {
            name = p.getMember().getMemberProfile().getFullName();
        }

        return PaymentResponse.builder()
                .id(p.getId())
                .transactionCode(p.getTransactionCode())
                .memberId(p.getMember() != null ? p.getMember().getId() : null)
                .memberEmail(p.getMember() != null ? p.getMember().getEmail() : null)
                .memberName(name)
                .packageId(p.getMembershipPackage() != null ? p.getMembershipPackage().getId() : null)
                .packageName(p.getMembershipPackage() != null ? p.getMembershipPackage().getName() : null)
                .membershipId(p.getMembership() != null ? p.getMembership().getId() : null)
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .status(p.getStatus())
                .paidAt(p.getPaidAt())
                .note(p.getNote())
                .createdAt(p.getCreatedAt())
                .build();
    }
}

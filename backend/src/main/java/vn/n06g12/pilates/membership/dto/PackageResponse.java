package vn.n06g12.pilates.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.membership.entity.MembershipPackage;
import vn.n06g12.pilates.membership.entity.PackageStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer totalSessions;
    private Integer durationDays;
    private PackageStatus status;
    private Instant createdAt;

    public static PackageResponse from(MembershipPackage p) {
        return PackageResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .totalSessions(p.getTotalSessions())
                .durationDays(p.getDurationDays())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }
}

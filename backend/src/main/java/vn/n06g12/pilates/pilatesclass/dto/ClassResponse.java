package vn.n06g12.pilates.pilatesclass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.n06g12.pilates.pilatesclass.entity.ClassLevel;
import vn.n06g12.pilates.pilatesclass.entity.ClassStatus;
import vn.n06g12.pilates.pilatesclass.entity.PilatesClass;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassResponse {
    private Long id;
    private String name;
    private String description;
    private Integer defaultCapacity;
    private ClassLevel level;
    private ClassStatus status;
    private Instant createdAt;

    public static ClassResponse fromEntity(PilatesClass entity) {
        return ClassResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .defaultCapacity(entity.getDefaultCapacity())
                .level(entity.getLevel())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

package com.klasavchik.modelHorseProject.dto.show.criteria;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriteriaResponse {
    private Long divisionId;
    private Long showId;
    private boolean canEdit;
    private String lockedReason;        // null если можно редактировать
    private LocalDateTime updatedAt;
    private Long version;
    private List<CriterionItemDto> items;
}

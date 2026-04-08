package com.klasavchik.modelHorseProject.dto.show.judging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreItemDto {
    private Long entryId;
    private Long criterionId;
    private Integer score;
}

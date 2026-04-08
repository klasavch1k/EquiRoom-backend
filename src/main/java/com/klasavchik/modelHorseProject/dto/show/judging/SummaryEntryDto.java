package com.klasavchik.modelHorseProject.dto.show.judging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryEntryDto {
    private Long entryId;
    private List<CriterionAverageDto> scores;
    private Double totalAverage;
}

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
public class JudgingPageResponse {
    private Long classId;
    private String className;
    private Long divisionId;
    private String divisionName;
    private boolean canScore;
    private boolean canView;
    private Long currentJudgeId;
    private List<JudgeDto> judges;
    private List<CriterionDto> criteria;
    private List<JudgingEntryDto> entries;
}

package com.klasavchik.modelHorseProject.dto.show.organizersJudges;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class JudgeShortDto {
    private Long id;
    private Long userId;       // null, если внешний судья
    private String shortName;
    private String bio;        // опционально
}
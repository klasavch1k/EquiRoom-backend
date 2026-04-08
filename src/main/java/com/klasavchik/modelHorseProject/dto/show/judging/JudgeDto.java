package com.klasavchik.modelHorseProject.dto.show.judging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JudgeDto {
    private Long id;
    private String shortName;
    private String name;
}

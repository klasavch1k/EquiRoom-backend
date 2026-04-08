package com.klasavchik.modelHorseProject.dto.show.criteria;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriteriaCopyRequest {

    @NotNull(message = "ID исходного дивизиона обязателен")
    private Long sourceDivisionId;

    private boolean replace;    // true = заменить существующие критерии в целевом дивизионе
}

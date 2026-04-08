package com.klasavchik.modelHorseProject.dto.show.judging;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveScoresRequest {

    @NotNull
    @Valid
    private List<ScoreItemDto> scores;
}

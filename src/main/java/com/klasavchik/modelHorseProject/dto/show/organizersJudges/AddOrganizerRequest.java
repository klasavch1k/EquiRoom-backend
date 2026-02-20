package com.klasavchik.modelHorseProject.dto.show.organizersJudges;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddOrganizerRequest {
    @NotNull
    private Long userId;
}
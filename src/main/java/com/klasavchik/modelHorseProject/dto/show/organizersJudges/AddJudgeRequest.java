package com.klasavchik.modelHorseProject.dto.show.organizersJudges;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Для добавления судьи
@Data
public class AddJudgeRequest {
    private Long userId;  // null для внешнего
    private String bio;   // опционально
}
package com.klasavchik.modelHorseProject.dto.show;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class JudgeShortDto {
    private Long id;
    private Long userId;       // null, если внешний судья
    private String name;       // имя судьи (обязательно)
    private String bio;        // опционально
}
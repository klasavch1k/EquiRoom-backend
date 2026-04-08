package com.klasavchik.modelHorseProject.dto.show.criteria;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriterionItemDto {
    private Long id;           // null для новых записей

    @NotBlank(message = "Название критерия не может быть пустым")
    private String name;

    @Min(value = 1, message = "Максимальный балл должен быть >= 1")
    private Integer maxScore;

    @Min(value = 1, message = "Позиция должна быть >= 1")
    private Integer position;
}

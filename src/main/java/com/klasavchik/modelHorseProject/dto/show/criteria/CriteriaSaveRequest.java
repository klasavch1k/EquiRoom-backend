package com.klasavchik.modelHorseProject.dto.show.criteria;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriteriaSaveRequest {

    @NotNull(message = "Версия обязательна для оптимистичной блокировки")
    private Long version;

    @NotEmpty(message = "Список критериев не может быть пустым (минимум 1)")
    private List<@Valid CriterionItemDto> items;
}

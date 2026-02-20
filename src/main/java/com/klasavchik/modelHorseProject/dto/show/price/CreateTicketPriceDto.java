package com.klasavchik.modelHorseProject.dto.show.price;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTicketPriceDto {
    @NotBlank(message = "Тип обязателен")
    private String type;

    @NotNull(message = "Цена обязательна")
    @Min(value = 0, message = "Цена не может быть отрицательной")
    private Integer price;

    @NotNull(message = "Количество моделей обязательно")
    @Min(value = 1, message = "Минимум 1 модель")
    private Integer includedModels;

    private String description;
}
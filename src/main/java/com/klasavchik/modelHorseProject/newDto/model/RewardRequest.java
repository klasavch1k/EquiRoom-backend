package com.klasavchik.modelHorseProject.newDto.model;

import jakarta.validation.constraints.*;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RewardRequest {

    private Long id; // может быть null при создании

    @NotBlank(message = "Название награды обязательно")
    @Size(max = 100, message = "Название награды не должно превышать 100 символов")
    private String rewardName;

    @NotBlank(message = "Название организации обязательно")
    @Size(max = 100, message = "Название организации не должно превышать 100 символов")
    private String organizationName;

    @NotNull(message = "Год награды обязателен")
    @Min(value = 1900, message = "Год не может быть меньше 1900")
    @Max(value = 2100, message = "Год не может быть больше 2100")
    private Integer year;

    @Size(max = 255, message = "URL изображения слишком длинный")
    private String avatar;
}

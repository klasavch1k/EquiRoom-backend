package com.klasavchik.modelHorseProject.newDto.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateModelRequest {
    
    @NotBlank(message = "Имя модели обязательно")
    @Size(max = 100, message = "Имя не должно превышать 100 символов")
    private String name;

    @Size(max = 255, message = "URL аватара слишком длинный")
    private String avatar;

    @NotBlank(message = "Порода обязательна")
    @Size(max = 100, message = "Название породы не должно превышать 100 символов")
    private String breed;

    @NotBlank(message = "Масть обязательна")
    @Size(max = 100, message = "Название масти не должно превышать 100 символов")
    private String horseColor;

    @Size(max = 20, message = "Информация о продаже слишком длинная")
    private String salesInformation;

    @NotBlank(message = "Имя автора модели обязательно")
    @Size(max = 100, message = "Имя мастера не должно превышать 100 символов")
    private String modelMasterName;

    @NotBlank(message = "Имя автора росписи обязательно")
    @Size(max = 100, message = "Имя мастера не должно превышать 100 символов")
    private String artMasterName;

    @NotNull(message = "Год росписи обязателен")
    @Min(value = 1800, message = "Год росписи не может быть меньше 1800")
    @Max(value = 2100, message = "Год росписи не может быть больше 2100")
    private Integer yearOfPainting;

    @Valid
    @Builder.Default
    private Set<ModelMediaRequest> modelMedia = new HashSet<>();

    @Valid
    @Builder.Default
    private Set<RewardRequest> rewards = new HashSet<>();
}

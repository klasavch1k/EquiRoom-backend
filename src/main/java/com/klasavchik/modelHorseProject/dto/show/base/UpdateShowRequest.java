package com.klasavchik.modelHorseProject.dto.show.base;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShowRequest {

    @Size(min = 3, max = 120, message = "Название от 3 до 120 символов")
    private String name;

    @Size(max = 2000, message = "Описание слишком длинное")
    private String description;

    @FutureOrPresent(message = "Дата начала не может быть в прошлом")
    private LocalDate startDate;

    @FutureOrPresent(message = "Дата окончания не может быть в прошлом")
    private LocalDate endDate;

    // true = включена лотерея среди участников
    private Boolean lotteryEnabled;

    // цена за дополнительную модель (null или 0 = бесплатно)
    private Integer additionalPrice;

    // true = платное участие, false = бесплатное
    private Boolean isPaid;

    // максимум дополнительных фото в одной заявке
    @Min(value = 0, message = "Не может быть отрицательным")
    @Max(value = 10, message = "Слишком много — максимум 10")
    private Integer maxAdditionalPhotos;

    // если в запросе придёт true → хотим удалить текущий баннер
    private Boolean deleteBanner;

    // если в запросе придёт true → хотим удалить текущий регламент
    private Boolean deleteRules;
}
package com.klasavchik.modelHorseProject.newDto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

// дто для карточки модели
public class CardModelResponse {
    private Long id;                 // ID модели
    private String name;             // название лошади
    private String avatar;           // ссылка на аватар
    private String salesInformation; // информация о продаже
    private String modelMasterName;    // автор модели
    private Integer yearOfPainting;  // год росписи
}

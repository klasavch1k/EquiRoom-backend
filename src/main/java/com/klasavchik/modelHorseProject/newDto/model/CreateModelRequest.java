package com.klasavchik.modelHorseProject.newDto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

// дто создания модели или обновления
public class CreateModelRequest {
    private String name;               // Название лошади
    private String avatar;             // Основной аватар лошади
    private String breed;              // Порода
    private String horseColor;         // Масть
    private String salesInformation;   // Информация о продаже
    private String modelMasterName;    // Автор модели
    private String artMasterName;      // Автор росписи
    private Integer yearOfPainting;    // Год росписи

    private List<ModelMediaRequest> modelMedia; // Фото/видео
    private List<RewardRequest> rewards;       // Награды
}

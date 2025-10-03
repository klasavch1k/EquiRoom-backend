package com.klasavchik.modelHorseProject.newDto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

// не используется напрямую, нужен для создания модели
public class RewardRequest {
    private Long id;                 // новый
    private String rewardName;        // Название награды
    private String organizationName;  // Организация
    private Integer year;             // Год
    private String avatar;            // Фото награды (URL)
}

package com.klasavchik.modelHorseProject.newDto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// не используется на прямую, нужен для детпльного возврата информации о модели
public class RewardDto {
    private Integer id;
    private String rewardName;
    private String organizationName;
    private Integer year;
    private String avatar;
}

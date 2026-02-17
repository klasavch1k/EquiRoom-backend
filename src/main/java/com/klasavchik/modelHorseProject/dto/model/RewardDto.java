package com.klasavchik.modelHorseProject.dto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// не используется на прямую, нужен для детпльного возврата информации о модели
public class RewardDto {
    private Long id;
    private String rewardName;
    private String organizationName;
    private Integer year;
    private String avatar;
    private LocalDateTime createdAt;
}

package com.klasavchik.modelHorseProject.newDto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

// возвращает подробную информацию о модели
public class DetailModelResponse {
    private Long id;
    private String name;
    private String avatar;
    private String breed;
    private String horseColor;
    private String salesInformation;
    private String modelMasterName;
    private String artMasterName;
    private Integer yearOfPainting;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MediaDto> media;
    private List<RewardDto> rewards;
}

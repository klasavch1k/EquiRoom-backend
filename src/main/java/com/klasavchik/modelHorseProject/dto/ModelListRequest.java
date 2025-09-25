package com.klasavchik.modelHorseProject.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModelListRequest {
    private Long id;
    private String name;
    private String breed; // порода лошади
    private String image;
}

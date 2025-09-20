package com.klasavchik.modelHorseProject.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.context.annotation.Bean;

@Data
@Builder
public class HorseModelListRequest {
    private Long id;
    private String name;
    private String breed; // порода лошади
    private String avatar;
}

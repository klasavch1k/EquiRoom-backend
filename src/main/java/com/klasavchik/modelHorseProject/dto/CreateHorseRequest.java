package com.klasavchik.modelHorseProject.dto;


import com.klasavchik.modelHorseProject.entity.HorseModelMedia;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class CreateHorseRequest {
    private String name;
    private String breed; //порода лошади
    private String description;
//    private User master;
    private String masterName;
    private List<HorseModelMediaRequest> media;
}


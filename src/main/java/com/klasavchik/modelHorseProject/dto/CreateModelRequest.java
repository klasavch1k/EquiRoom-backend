package com.klasavchik.modelHorseProject.dto;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class CreateModelRequest {
    private String name;
    private String breed; //порода лошади
    private String description;
//    private User master;
    private String masterName;
    private List<ModelMediaRequest> media;
}


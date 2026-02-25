package com.klasavchik.modelHorseProject.dto.show.base;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DivisionStructureDto {
    private Long id;
    private String name;
    private String description;
    private String type;
    private List<SectionStructureDto> sections;
}
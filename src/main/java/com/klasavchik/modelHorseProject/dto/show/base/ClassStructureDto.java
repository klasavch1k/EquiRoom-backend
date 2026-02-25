package com.klasavchik.modelHorseProject.dto.show.base;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassStructureDto {
    private Long id;
    private String name;
    private String description;
    private int totalEntries;      // общее кол-во записей (моделей) в классе
    private int admittedEntries;   // кол-во допущенных
    private int judgedEntries;     // кол-во оцененных
}
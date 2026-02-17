package com.klasavchik.modelHorseProject.dto.show;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CreateClassDto {
    private String name;
    private String description;
}
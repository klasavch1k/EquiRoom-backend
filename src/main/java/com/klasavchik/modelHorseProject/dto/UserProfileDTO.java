package com.klasavchik.modelHorseProject.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String avatar;// Используем как аватар или отображаемое имя
    private Integer following;
    private Integer followers;
    private Integer figurines;
}
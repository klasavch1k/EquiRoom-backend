package com.klasavchik.modelHorseProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSearchDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String nickName;
    private String avatar;
    private Long modelCount;
}
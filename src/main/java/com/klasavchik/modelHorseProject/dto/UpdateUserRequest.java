package com.klasavchik.modelHorseProject.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UpdateUserRequest {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}

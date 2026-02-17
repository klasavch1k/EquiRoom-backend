package com.klasavchik.modelHorseProject.dto.user;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
//    private Role role;
}

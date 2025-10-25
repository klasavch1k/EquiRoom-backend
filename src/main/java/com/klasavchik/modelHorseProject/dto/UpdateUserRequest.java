package com.klasavchik.modelHorseProject.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class UpdateUserRequest {
    private Long id;
    private String firstName;
    private String lastName;
    private String bio;
    private String gender;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String avatar;
    private String nickname;
//    private String password; решено в будующем сделать отдельную вкладку для изменения пароля
}

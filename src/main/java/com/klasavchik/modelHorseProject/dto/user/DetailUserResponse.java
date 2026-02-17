package com.klasavchik.modelHorseProject.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DetailUserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String bio;
    private String avatar;
    private LocalDate dateOfBirth;
    private String email;
    private String phoneNumber;
    private String gender;
    private String nickname;
}

package com.klasavchik.modelHorseProject.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class UpdatePersonInformRequest {
    private String firstName;
    private String lastName;
    private String bio;
    private String gender;
    private LocalDate dateOfBirth;
    private String avatar;
    private String nickname;
}

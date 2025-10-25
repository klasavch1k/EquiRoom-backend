package com.klasavchik.modelHorseProject.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileDTO {
    private Long id;
    private String nickname;
    private String firstName;
    private String lastName;
    private String bio;
    private String avatar;
    private Long following;
    private Long followers;
    private Integer figurines;
    private Boolean isFollowedByCurrentUser;
}
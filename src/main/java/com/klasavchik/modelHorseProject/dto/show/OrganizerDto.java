package com.klasavchik.modelHorseProject.dto.show;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class OrganizerDto {
    private Long userId;
    private String nickname;  // или name/email
    private String role;      // "creator", "co-organizer"
}
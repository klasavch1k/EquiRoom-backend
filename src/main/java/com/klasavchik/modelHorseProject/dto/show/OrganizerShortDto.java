package com.klasavchik.modelHorseProject.dto.show;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OrganizerShortDto {
    private Long userId;
    private String nickname;   // или email, если никнейма нет
    private String role;       // "creator", "co-organizer"
}